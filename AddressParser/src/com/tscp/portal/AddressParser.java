package com.tscp.portal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.tscp.address.AddressToken;
import com.tscp.address.ParsedAddress;
import com.tscp.address.dictionary.Dictionary;
import com.tscp.address.euld.Euld;
import com.tscp.address.listcode.ListCodeManager;
import com.tscp.address.token.BlockType;
import com.tscp.address.token.Token;
import com.tscp.address.token.TokenList;
import com.tscp.address.token.TokenType;

import db.ConnectionInfo;
import db.SPArgs;
import db.StoredProc;

public class AddressParser {
	private static final String WHITESPACE = " |\t|\r|\n";
	private static final Dictionary zipDirectory = new Dictionary(1, "dictionary/zip.properties");
	private TokenList tokenList = new TokenList();
	private Token cityToken;
	private Token stateToken;
	private Token zipToken;
	private ParsedAddress parsedAddress = new ParsedAddress();
	private MapInfo mapInfo;
	private ListCodeManager listCodeManager = new ListCodeManager();

	public ParsedAddress getAddress() {
		return this.parsedAddress;
	}

	/**
	 * FUNCTION: sets the input address and manually opens connections to jserv
	 * and k11mvno NOTE: for testing purposes only
	 * 
	 * @param inAddress
	 * @param verify
	 * @throws IOException
	 */
	public void setInput(String inAddress, boolean verify) throws IOException {
		refresh();
		parsedAddress = new ParsedAddress();
		parsedAddress.unparsedInput = inAddress;
		inAddress = normalizeAddress(inAddress);

		Token newToken;
		for (String token : inAddress.split(WHITESPACE)) {
			if (token.trim().equals(""))
				continue;
			newToken = new Token(token);
			tokenList.add(newToken);
		}

		parse();

		if (verify) {
			try {
				tryMapInfo(parsedAddress.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * FUNCTION: refreshes the parser to a new state
	 */
	public void refresh() {
		tokenList.clear();
		parsedAddress.clear();
		zipToken = null;
		cityToken = null;
		stateToken = null;
		if (mapInfo != null) {
			mapInfo.refresh();
		}
	}

	/**
	 * FUNCTION: capitalizes, removes periods, removes commas
	 * 
	 * @param inAddress
	 * @return
	 */
	private String normalizeAddress(String inAddress) {
		return inAddress.replace(".", "").replace(",", "").toUpperCase();
	}

	private void tryMapInfo(ParsedAddress inAddress) {
		mapInfo = new MapInfo(inAddress);
		if (mapInfo.isError) {
			for (int i = 1; mapInfo.isError && mapInfo.tryCount < 4; i++) {
				System.out.println("@MapInfo found no results, attempting new request.");
				mapInfo = mapInfo.tryMoreSuffixes();
				mapInfo.tryCount = i;
			}
			if (mapInfo.isError && mapInfo.tryCount >= 4) {
				System.out.println("@No results found with other street suffixes.");
			}
		}
		if (mapInfo.isExactMatch) {
			System.out.println("@MapInfo found an exact match!");
			return;
		}
		if (!mapInfo.isExactMatch) {
			System.out.println("@MapInfo could not find an exact match.");
		}
		if (mapInfo != null) {
			parsedAddress.setCandidates(mapInfo.getCandidates(5));
		}
	}

	/**
	 * FUNCTION: parses the given address NOTE: contains the flow of the logic
	 */
	private void parse() {
		findStateZipSuffix();
		tokenList.mergeSimilar();
		findStateZipSuffix();
		tokenList.mergeSimilar();
		findCity();
		findStreetName();

		Token token = tokenList.getFirst();
		while (token != null) {
			if (token.getBlockType() == BlockType.STNUM)
				parsedAddress.stnum = new AddressToken(token.value);
			if (token.getBlockType() == BlockType.STNAME)
				parsedAddress.stname = new AddressToken(token.value);
			if (token.getBlockType() == BlockType.PREFIX) {
				parsedAddress.prefix.setValue(token.value);
			}
			if (token.getBlockType() == BlockType.SUFFIX) {
				parsedAddress.suffix.setValue(token.value);
			}
			if (token.getBlockType() == BlockType.CITY)
				parsedAddress.city = new AddressToken(token.value);
			if (token.getBlockType() == BlockType.STATE)
				parsedAddress.state.setValue(token.value);
			if (token.getBlockType() == BlockType.ZIP)
				parsedAddress.zip = new AddressToken(token.value);
			if (token.getBlockType() == BlockType.UNKNOWN)
				parsedAddress.addUnknown(token.value);
			if (token.getBlockType() == BlockType.SUGGESTION)
				parsedAddress.addSuggestion(token.suggestionType + ": " + token.value);
			if (token.getBlockType() == BlockType.EULD) {
				Euld euld = new Euld();
				euld.setEulv(token.value);
				euld.setEuld(new AddressToken(token.next.value));
			}
			token = token.next;
		}
		listCodeManager.getListCodes(parsedAddress);
	}

	/**
	 * FUNCTION: sets state and zip token after analyzing the TokenList NOTE: the
	 * state and zip should be the last instance of each
	 */
	private void findStateZipSuffix() {
		Token sToken = null;
		Token sfxToken = null;
		int sIndex = -1;

		Token token = tokenList.getFirst();
		while (token != null) {
			if (token.getBlockType() == BlockType.ZIP) {
				zipToken = token;
			}
			if (token.getBlockType() == BlockType.STATE) {
				if (token.position > sIndex) {
					if (sToken != null)
						sToken.setBlockType(BlockType.UNKNOWN);
					sToken = token;
					sIndex = token.position;
				}
			}
			if (token.getBlockType() == BlockType.SUFFIX) {
				if (sfxToken != null) {
					token.setBlockType(BlockType.UNKNOWN);
				} else {
					sfxToken = token;
				}
			}
			token = token.next;
		}
		stateToken = sToken;
	}

	/**
	 * FUNCTION: sets the street name of the TokenList NOTE: the street name
	 * should be the earliest instance of each
	 */
	private void findStreetName() {
		Token snToken = null;
		int snIndex = 99;

		Token token = tokenList.getFirst();
		while (token != null) {
			if (token.getBlockType() == BlockType.UNKNOWN) {
				if (token.position < snIndex) {
					if (snToken != null)
						snToken.setBlockType(BlockType.UNKNOWN);
					snToken = token;
					snIndex = token.position;
					snToken.setBlockType(BlockType.STNAME);
				}
			}
			token = token.next;
		}
	}

	/**
	 * FUNCTION: sets the city using zip.properties as a reference if the city is
	 * not in the list then insert it or modify tokens as needed. NOTE: reverts
	 * back to findCityWithZip() if it fails
	 */
	private void findCity() {
		if (zipToken == null) {
			return;
		}

		String zip = zipToken.value;
		System.out.println(zip);
		String lookupValue = zipDirectory.getProperty(zip);
		System.out.println(lookupValue);
		String lookupCity = zipDirectory.getCity(lookupValue).toUpperCase();
		String lookupState = zipDirectory.getState(lookupValue).toUpperCase();

		System.out.println(">Searching zip.properties for city with zip code [" + zip + "]...");
		// Match the zip code to a token that contains the city
		if (lookupCity != null) {
			Token cToken = null;
			System.out.println(">Found [" + lookupCity + "] using zip [" + zip + "]");

			// Determine which token possibly contains the city
			if (cityToken == null) {
				if (stateToken == null) {
					// If stateToken does not exist, create one and use the token behind
					// it as the city
					stateToken = new Token(lookupState);
					stateToken.setBlockType(BlockType.STATE);
					stateToken.setType(TokenType.CHARACTER);
					tokenList.addBefore(stateToken, zipToken);
					cToken = stateToken.prev;
				} else {
					// The stateToken exists. The city should be behind it.
					cToken = stateToken.prev;
				}
			} else {
				cToken = cityToken;
			}
			System.out.println(">Testing queried city against entered value [" + cToken.value + "]");

			// Check if the queried state and stateToken value match
			if (!lookupState.equals(stateToken.value)) {
				System.out.println(">The state [" + stateToken.value + "] is mismatched with zip [" + zip + "]");
				Token newStateToken = new Token(lookupState);
				newStateToken.setBlockType(BlockType.SUGGESTION);
				newStateToken.suggestionType = "state";
				tokenList.add(newStateToken);
				parsedAddress.addError("The state [" + stateToken.value + "] is mismatched with zip [" + zip + "]");
			}
			// Checks where the city should be inserted into the AddressList
			if (!cToken.value.equals(lookupCity)) {
				int strIndex = cToken.value.indexOf(lookupCity);
				Token newToken = new Token(lookupCity);
				newToken.setBlockType(BlockType.CITY);
				newToken.setType(TokenType.CHARACTER);
				if (strIndex >= 0) { // postal_city exists within the string
					if (strIndex == 0) { // City is at the front
						tokenList.addBefore(newToken, cToken);
						cToken.value = cToken.value.substring(lookupCity.length()).trim(); // used
																																								// to
																																								// have
																																								// +1
																																								// here
						cToken.setBlockType(BlockType.UNKNOWN);
						cToken.setType(TokenType.CHARACTER);
					} else if (strIndex == cToken.value.length() - lookupCity.length()) { // City
																																								// is
																																								// at
																																								// the
																																								// back
						tokenList.addAfter(newToken, cToken);
						cToken.value = cToken.value.substring(0, cToken.value.length() - lookupCity.length()).trim();
						cToken.setBlockType(BlockType.UNKNOWN);
						cToken.setType(TokenType.CHARACTER);
					} else { // City is in the middle
						String[] newTokens = cToken.value.split(lookupCity);
						Token half1 = new Token(newTokens[0].trim());
						Token half2 = new Token(newTokens[1].trim());
						half1.setBlockType(BlockType.UNKNOWN);
						half2.setBlockType(BlockType.UNKNOWN);
						half1.setType(TokenType.CHARACTER);
						half2.setType(TokenType.CHARACTER);
						tokenList.addBefore(half1, cToken);
						tokenList.addAfter(newToken, half1);
						tokenList.addAfter(half2, newToken);
					}
					this.cityToken = newToken;
				} else { // We have a complete mismatch
					// if (cToken.getType().equals(TokenType.CHARACTER) &&
					// !cToken.getBlockType().equals(BlockType.EULV) &&
					// !cToken.getBlockType().equals(Token.euld)) {
					if (cToken.getType() == TokenType.CHARACTER && cToken.getBlockType() != BlockType.EULV
							&& cToken.getBlockType() != BlockType.EULD) {
						newToken.setBlockType(BlockType.SUGGESTION);
						newToken.suggestionType = "city";
						newToken.setType(TokenType.CHARACTER);
						tokenList.addBefore(newToken, cToken);
						parsedAddress.addError("Could not find city [" + cToken.value + "] with zip [" + zip + "]");
						cToken.setBlockType(BlockType.CITY);
					} else {
						newToken.setBlockType(BlockType.CITY);
						tokenList.addBefore(newToken, cToken);
						parsedAddress.addError("No city specified.\nUsing city [" + newToken.value + "]");
						Token suggestionToken = new Token(lookupCity);
						suggestionToken.setBlockType(BlockType.SUGGESTION);
						suggestionToken.suggestionType = "city";
						suggestionToken.setType(TokenType.CHARACTER);
						tokenList.addBefore(suggestionToken, cToken);
					}
				}
			} else { // No modifications needed, just set the attribute
				System.out.println(">[" + lookupCity + "] matches with [" + cToken.value + "]");
				cToken.setBlockType(BlockType.CITY);
			}
		} else {
			System.out
					.println(">No results found in zip.properties file.\n Using database table PB_CITY_ABBREV - WARNING SLOW -");
			try {
				findCity_K11();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * FUNCTION: sets the city using PB_CITY_ABBREV in JSERV as a reference. -- if
	 * the city is not in the list then insert it or modify tokens as needed.
	 * NOTE: No longer used. Use findCityWithZip instead.
	 */
	private void findCity_JSERV() throws SQLException, IOException {
		String city = null;
		Token cToken;
		Token nToken;

		// Determine which token may contain the city
		if (stateToken != null) {
			cToken = stateToken.prev;
		} else if (zipToken != null) {
			cToken = zipToken.prev;
		} else {
			cToken = tokenList.getLast();
		}

		String cTokenVal = cToken.value;
		System.out.println(">Querying PB_CITY_ABBREV with: " + cTokenVal);

		// Query PB_CITY_ABBREV for the city
		Connection connjserv = getConnection("db/jservdbconfig.properties");
		StoredProc sp = new StoredProc(connjserv, "AddressParser");
		SPArgs spArgs = new SPArgs();
		sp.setConn(connjserv);
		spArgs.put("sp", "ep_get_citynameinstr");
		spArgs.put("arg1", cTokenVal);
		ResultSet rs = sp.exec(spArgs);
		try {
			while (rs.next()) {
				city = rs.getString("CITY_FULLNAME");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		sp.close(rs);
		closeConnection(connjserv);

		if (city != null) {
			if (!cTokenVal.equals(city)) {
				int strIndex = cTokenVal.indexOf(city);
				nToken = new Token(city);
				nToken.setType(TokenType.CHARACTER);
				nToken.setBlockType(BlockType.CITY);
				this.cityToken = nToken;

				if (strIndex >= 0) {
					if (strIndex == 0) {
						tokenList.addBefore(nToken, cToken);
						cToken.value = cTokenVal.substring(city.length() + 1);
						cToken.setBlockType(BlockType.UNKNOWN);
						cToken.setType(TokenType.CHARACTER);
					} else if (strIndex == cTokenVal.length() - city.length()) {
						tokenList.addAfter(nToken, cToken);
						cToken.value = cTokenVal.substring(0, cTokenVal.length() - city.length());
						cToken.setBlockType(BlockType.UNKNOWN);
						cToken.setType(TokenType.CHARACTER);
					} else {
						String[] newTokens = cTokenVal.split(city);
						Token half1 = new Token(newTokens[0].trim());
						Token half2 = new Token(newTokens[1].trim());
						half1.setBlockType(BlockType.UNKNOWN);
						half2.setBlockType(BlockType.UNKNOWN);
						half1.setType(TokenType.CHARACTER);
						half2.setType(TokenType.CHARACTER);
						tokenList.addBefore(half1, cToken);
						tokenList.addAfter(nToken, cToken);
						tokenList.addAfter(half2, cToken.next);
						tokenList.remove(cToken);
					}
				} else { // We have a complete mismatch
					// if (cToken.getType().equals(TokenType.CHARACTER)) {
					if (cToken.getType() == TokenType.CHARACTER) {
						nToken.setBlockType(BlockType.SUGGESTION);
						nToken.suggestionType = "city";
						nToken.setType(TokenType.CHARACTER);
						tokenList.addBefore(nToken, cToken);
						parsedAddress.addError("Could not find city: " + cTokenVal + " in the provided addreess.");
						cToken.setBlockType(BlockType.CITY);
					} else {
						nToken.setBlockType(BlockType.CITY);
						tokenList.addBefore(nToken, cToken);
						parsedAddress.addError("No city specified.\nUsing city: " + nToken.value);
						Token suggestionToken = new Token(city);
						suggestionToken.setBlockType(BlockType.SUGGESTION);
						suggestionToken.suggestionType = "city";
						suggestionToken.setType(TokenType.CHARACTER);
						tokenList.add(suggestionToken);
					}
				}
			} else { // The query and city token match
				cToken.setBlockType(BlockType.CITY);
			}
		} else {
			System.out.println(">No match found for: " + cTokenVal + " in pb_city_abbrev");
			cToken.setBlockType(BlockType.UNKNOWN);
			parsedAddress.addUnknown(cTokenVal);
		}
		parsedAddress.city = new AddressToken(city);
	}

	/**
	 * FUNCTION: sets the city using SYSCA in k11 as a reference. -- if the city
	 * is not in the list then insert it or modify tokens as needed. NOTE: No
	 * longer used. Use findCityWithProp instead;
	 */
	private void findCity_K11() throws SQLException, IOException {
		if (zipToken != null) {
			String zip = zipToken.value;
			String postal_city = null;
			String state = null;
			ArrayList<String> recommend = new ArrayList<String>();

			System.out.println(">Querying SYSCSA with: " + zip);
			Connection connk11 = getConnection("db/k11dbconfig.properties");
			StoredProc sp = new StoredProc(connk11, "AddressParser");
			SPArgs spArgs = new SPArgs();
			ResultSet rs;
			spArgs.put("sp", "ep_get_cityzip");
			spArgs.put("arg1", zip);
			rs = sp.exec(spArgs);
			try {
				if (rs != null) {
					while (rs.next()) {
						postal_city = rs.getString("POSTAL_CITY");
						state = rs.getString("STATE");
						recommend.add(postal_city.toUpperCase());
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			sp.close(rs);
			closeConnection(connk11);

			if (postal_city != null) {
				// Guesses what token in the AddressList may contain the city
				Token cToken = null;
				if (cityToken == null && stateToken != null) {
					// If stateToken exists grab the previous token
					cToken = stateToken.prev;
				} else if (cityToken == null && stateToken == null) {
					// If stateToken does not exist, create and insert one. Then take the
					// token behind it.
					stateToken = new Token(state);
					stateToken.setBlockType(BlockType.STATE);
					stateToken.setType(TokenType.CHARACTER);
					tokenList.addBefore(stateToken, zipToken);
					cToken = stateToken.prev;
				} else if (cityToken != null) {
					cToken = cityToken; // cityToken exists, use it
				} else {
					// If all else fails, use the token behind the zip
					cToken = zipToken.prev;
				}
				// System.out.println("current city candidate: " + cToken.value);
				String cityName = null;
				// Check the returned query values with the candidate cityToken
				for (String city : recommend) {
					if (cToken.value.contains(city)) {
						// System.out.println("found city: " + city);
						cityName = city;
					}
				}
				// Checks if the candidate cityToken needs to be modified.
				// If it does, break it up into new tokens and insert the
				// cityToken in the proper place.
				if (cityName != null && !cToken.value.equals(cityName)) {
					int strIndex = cToken.value.indexOf(cityName);
					if (strIndex == 0) { // City is at the front
						Token newCToken = new Token(cityName);
						newCToken.setBlockType(BlockType.CITY);
						newCToken.setType(TokenType.CHARACTER);
						tokenList.addBefore(newCToken, cToken);
						cToken.value = cToken.value.substring(cityName.length() + 1).trim();
						cToken.setBlockType(BlockType.UNKNOWN);
						cToken.setType(TokenType.CHARACTER);
					} else if (strIndex == cToken.value.length() - cityName.length()) { // City
																																							// is
																																							// at
																																							// the
																																							// back
						Token newCToken = new Token(cityName);
						newCToken.setBlockType(BlockType.CITY);
						newCToken.setType(TokenType.CHARACTER);
						tokenList.addAfter(newCToken, cToken);
						cToken.value = cToken.value.substring(0, cToken.value.length() - cityName.length()).trim();
						cToken.setBlockType(BlockType.UNKNOWN);
						cToken.setType(TokenType.CHARACTER);
					} else { // City is in the middle
						Token newCToken = new Token(cityName);
						newCToken.setBlockType(BlockType.CITY);
						newCToken.setType(TokenType.CHARACTER);
						String[] newTokens = cToken.value.split(cityName);
						Token half1 = new Token(newTokens[0].trim());
						Token half2 = new Token(newTokens[1].trim());
						half1.setBlockType(BlockType.UNKNOWN);
						half2.setBlockType(BlockType.UNKNOWN);
						half1.setType(TokenType.CHARACTER);
						half2.setType(TokenType.CHARACTER);
						tokenList.addBefore(half1, cToken);
						tokenList.addAfter(newCToken, cToken);
						tokenList.addAfter(newCToken, cToken.next);
						tokenList.remove(cToken);
					}
				} else { // No modifications needed, just set the attribute
					if (cToken.value.equals(cityName)) {
						cToken.setBlockType(BlockType.CITY);
					} else {
						// Nothing was found at all! Create a cityToken and insert it into
						// the AddressList
						Token newCToken = new Token(cityName);
						newCToken.setBlockType(BlockType.CITY);
						newCToken.setType(TokenType.CHARACTER);
						tokenList.addBefore(newCToken, stateToken);
						cToken.setBlockType(BlockType.UNKNOWN);
					}
				}
			} else {
				System.out.println(">No results found in database table SYSCSA.\n using database table PB_CITY_ABBREV");
				try {
					findCity_JSERV();
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * FUNCTION: gets a connection from the given server
	 * 
	 * @param server
	 * @return
	 * @throws IOException
	 */
	private Connection getConnection(String server) throws IOException {
		ConnectionInfo connInfo = new ConnectionInfo(server);
		Connection conn = null;
		try {
			conn = connInfo.getConnection();
		} catch (Exception e) {
			System.out.println("Error initializing ConnInfo " + e.getMessage());
		}
		return conn;
	}

	/**
	 * FUNCTION: closes the given connection
	 * 
	 * @param conn
	 */
	private void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException sql_ex) {
				System.out.println("error while closing connection");
			}
		}
		conn = null;
	}

}
