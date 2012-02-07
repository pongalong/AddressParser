package com.tscp.address;

import java.util.ArrayList;

import com.tscp.address.dictionary.ilec.AddressDictionary;
import com.tscp.address.euld.Euld;
import com.tscp.address.euld.EuldInfo;

public class ParsedAddress implements Cloneable {
	public String unparsedInput;
	public AddressToken prefix = new AddressToken();
	public AddressToken suffix = new AddressToken();
	public AddressToken state = new AddressToken();
	public AddressToken stnum = new AddressToken();
	public AddressToken stname = new AddressToken();
	public AddressToken city = new AddressToken();
	public AddressToken zip = new AddressToken();
	public EuldInfo euldInfo = new EuldInfo();

	public ParsingDetails parsedAddressDetails = new ParsingDetails();

	private static AddressDictionary addressDictionary;

	public void clear() {
		// TODO clear all the values
		parsedAddressDetails = null;
		stnum = null;
		stname = null;
		prefix = null;
		suffix = null;
		city = null;
		state = null;
		zip = null;
		unparsedInput = null;
	}

	public ParsedAddress translate(int ILEC) {
		addressDictionary = new AddressDictionary();
		ParsedAddress outAddress;
		try {
			outAddress = (ParsedAddress) this.clone();
		} catch (CloneNotSupportedException e) {
			outAddress = this;
			e.printStackTrace();
		}
		switch (ILEC) {
		case 3: // ATT
			addressDictionary.getSuffixDictionary().load("ATT/ATT_thoroughfare.properties");
			addressDictionary.getEuldDictionary().load("ATT/ATT_location.properties");
			break;
		case 5: // VERIZON
			break;
		}

		for (Euld euld : euldInfo) {
			addressDictionary.getEuldDictionary().translate(euld);
		}
		addressDictionary.getSuffixDictionary().translate(this.suffix);
		addressDictionary.getPrefixDictionary().translate(this.prefix);
		return outAddress;
	}

	public void addUnknown(String block) {
		parsedAddressDetails.getUnknown().add(block);
	}

	public void addError(String error) {
		parsedAddressDetails.getErrors().add(error);
	}

	public void addSuggestion(String suggestion) {
		parsedAddressDetails.getSuggestions().add(suggestion);
	}

	public void setCandidates(ArrayList<ParsedAddress> candidates) {
		parsedAddressDetails.getMapInfo().setCandidates(candidates);
	}

	@Override
	public ParsedAddress clone() throws CloneNotSupportedException {
		return (ParsedAddress) super.clone();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(stnum.toString()).append(" ");
		sb.append(prefix.toString()).append(" ");
		sb.append(stname.toString()).append(" ");
		sb.append(suffix.toString()).append(" ");
		for (Euld euld : euldInfo) {
			sb.append(euld.toString()).append(" ");
		}
		sb.append(city.toString()).append(" ");
		sb.append(state.toString()).append(" ");
		sb.append(zip.toString());
		return sb.toString();
	}

}
