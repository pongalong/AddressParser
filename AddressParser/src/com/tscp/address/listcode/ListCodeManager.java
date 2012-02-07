package com.tscp.address.listcode;

import com.tscp.address.AddressToken;
import com.tscp.address.ParsedAddress;
import com.tscp.address.dictionary.Dictionary;
import com.tscp.address.dictionary.tscp.ListCodeDictionary;
import com.tscp.address.euld.Euld;

public class ListCodeManager {
	private ListCodeDictionary listCodeDictionary = new ListCodeDictionary();

	public void getListCodes(ParsedAddress parsedAddress) {
		if (listCodeDictionary == null) {
			listCodeDictionary = new ListCodeDictionary();
		}
		for (Euld euld : parsedAddress.euldInfo) {
			euld.getEuld().setListCode(lookupCode(ListName.EULD, euld.getEuld()));
		}
		parsedAddress.prefix.setListCode(lookupCode(ListName.PREFIX, parsedAddress.prefix));
		parsedAddress.suffix.setListCode(lookupCode(ListName.SUFFIX, parsedAddress.suffix));
		parsedAddress.state.setListCode(lookupCode(ListName.STATE, parsedAddress.state));
	}

	private ListCode lookupCode(ListName listName, AddressToken token) {
		String key = token.getValue();
		ListCode listCode = new ListCode();
		String result = null;
		String defaultValue = new String();
		Dictionary dictionary = null;
		switch (listName) {
		case EULD:
			dictionary = listCodeDictionary.getEuldDictionary();
			defaultValue = ListCodeDictionary.defaultEuld;
			break;
		case PREFIX:
			dictionary = listCodeDictionary.getPrefixDictionary();
			defaultValue = ListCodeDictionary.defaultPrefix;
			break;
		case SUFFIX:
			dictionary = listCodeDictionary.getSuffixDictionary();
			defaultValue = ListCodeDictionary.defaultState;
			break;
		case STATE:
			dictionary = listCodeDictionary.getStateDictionary();
			defaultValue = ListCodeDictionary.defaultState;
			break;
		}
		if (dictionary != null && key != null) {
			result = dictionary.getProperty(key);
		} else {
			result = null;
		}
		if (result == null) {
			listCode.setValue(defaultValue);
		} else {
			listCode.setValue(result);
		}
		return listCode;
	}
}
