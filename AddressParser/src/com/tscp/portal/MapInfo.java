package com.tscp.portal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.teamdci.mapinfo.proxy.AddressValidation;
import com.teamdci.mapinfo.proxy.AddressValidationSoap;
import com.teamdci.mapinfo.proxy.RequestAddress;
import com.teamdci.mapinfo.proxy.ResponseAddress;
import com.teamdci.mapinfo.proxy.ResponseCandidate;
import com.teamdci.mapinfo.proxy.ResponseData;
import com.tscp.address.ParsedAddress;
import com.tscp.address.AddressToken;
import com.tscp.address.euld.Euld;

public class MapInfo {
	private ResponseData results;
	private ParsedAddress inAddress;
	public int tryCount = 0;
	public boolean isExactMatch = false;
	public boolean isError = false;

	/**
	 * CONSTRUCTOR
	 * 
	 * @param inAddress
	 */
	public MapInfo(ParsedAddress inAddress) {
		this.inAddress = inAddress;
		this.results = callMapInfo(inAddress);
		this.isExactMatch = results.isIsExactMatch();
		this.isError = results.getStatus().isIsError();
	}

	/**
	 * FUNCTION: refreshes mapInfo to a new state
	 */
	public void refresh() {
		inAddress.clear();
		results = null;
		tryCount = 0;
		isExactMatch = false;
		isError = false;
	}

	/**
	 * FUNCTION: returns the list of candidates
	 * 
	 * @param limit
	 * @return
	 */
	public ArrayList<ParsedAddress> getCandidates(int limit) {
		ArrayList<ParsedAddress> candList = new ArrayList<ParsedAddress>();
		List<ResponseCandidate> rcList = results.getCandidateList();
		Iterator<ResponseCandidate> rcIterator = rcList.iterator();
		ResponseCandidate rc;
		int count = 1;
		while (rcIterator.hasNext() && count < limit) {
			rc = rcIterator.next();
			System.out.println("@retrieving candidate: " + rc.getAddress());
			candList.add(candToAddress(rc));
			count++;
		}
		return candList;
	}

	/**
	 * FUNCTION: attempt to call mapInfo with another suffix
	 * 
	 * @return
	 */
	public MapInfo tryMoreSuffixes() {
		String[] sfxList = { "ST", "RD", "AVE", "BLVD" };
		if (tryCount < sfxList.length) {
			inAddress.suffix.setValue(sfxList[tryCount]);
			tryCount++;
			return new MapInfo(inAddress);
		} else {
			return null;
		}
	}

	/**
	 * FUNCTION: returns the exact match found by mapInfo
	 * 
	 * @return
	 */
	public ParsedAddress getExactMatch() {
		if (isExactMatch) {
			ResponseAddress ra = results.getAddress();
			return toAddress(ra);
		} else {
			System.out.println("@Cannot return exact match. No exact match was found.");
			return null;
		}
	}

	/**
	 * FUNCTION: returns the Address representation of the response address
	 * 
	 * @param ra
	 * @return
	 */
	private ParsedAddress toAddress(ResponseAddress ra) {
		ParsedAddress address = new ParsedAddress();
		address.stnum = new AddressToken(ra.getHouseNumber());
		address.stname = new AddressToken(ra.getStreetName());
		address.city = new AddressToken(ra.getCity());
		address.state.setValue(ra.getState());
		address.zip = new AddressToken(ra.getZip());
		StringTokenizer st = new StringTokenizer(ra.getUnit(), " ");
		String token;
		while (st.hasMoreTokens()) {
			token = st.nextToken();
			Euld euld = new Euld();
			euld.getEuld().setValue(token);
			euld.setEulv(st.nextToken());
		}
		return address;
	}

	/**
	 * FUNCTION: returns the Address representation of the response candidate
	 * 
	 * @param rc
	 * @return
	 */
	private ParsedAddress candToAddress(ResponseCandidate rc) {
		String addr = rc.getAddress();
		String city = rc.getCity();
		String state = rc.getState();
		String zip = rc.getZip();
		AddressParser myParser = new AddressParser();
		String addr2 = addr + " " + inAddress.euldInfo.toString() + " " + city + " " + state + " " + zip;
		try {
			myParser.setInput(addr2, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ParsedAddress myAddress = myParser.getAddress();
		return myAddress;
	}

	/**
	 * FUNCTION: calls mapInfo with the given address
	 * 
	 * @param inAddress
	 * @return
	 */
	private ResponseData callMapInfo(ParsedAddress inAddress) {
		AddressValidation av = new AddressValidation();
		AddressValidationSoap avs = av.getAddressValidationSoap();
		RequestAddress rqAddress = new RequestAddress();

		StringBuilder addr = new StringBuilder();
		if (inAddress.stnum != null && !inAddress.stnum.isEmpty())
			addr.append(inAddress.stnum).append(" ");
		if (inAddress.prefix != null && !inAddress.prefix.isEmpty())
			addr.append(inAddress.prefix).append(" ");
		if (inAddress.stname != null && !inAddress.stname.isEmpty())
			addr.append(inAddress.stname).append(" ");
		if (inAddress.suffix != null && !inAddress.suffix.isEmpty())
			addr.append(inAddress.suffix).append(" ");
		rqAddress.setAddress1(addr.toString().trim());
		rqAddress.setZip(inAddress.zip.getValue());

		System.out.print("@Calling MapInfo with: " + addr.toString().trim());
		System.out.print(" " + inAddress.zip + "\n");

		ResponseData rs = avs.validateAddress(rqAddress);
		return rs;
	}
}
