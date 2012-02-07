package com.tscp.address.dictionary.tscp;

import com.tscp.address.dictionary.Dictionary;

public class ListCodeDictionary {
	private Dictionary suffixDictionary = new Dictionary(1, "codes/suffix.properties");
	private Dictionary prefixDictionary = new Dictionary(1, "codes/prefix.properties");
	private Dictionary euldDictionary = new Dictionary(1, "codes/euld.properties");
	private Dictionary stateDictionary = new Dictionary(1, "codes/state.properties");

	public static final String defaultSuffix = "1539";
	public static final String defaultPrefix = "880";
	public static final String defaultEuld = "166000";
	public static final String defaultState = "";

	public Dictionary getSuffixDictionary() {
		return suffixDictionary;
	}

	public void setSuffixDictionary(Dictionary suffixDictionary) {
		this.suffixDictionary = suffixDictionary;
	}

	public Dictionary getPrefixDictionary() {
		return prefixDictionary;
	}

	public void setPrefixDictionary(Dictionary prefixDictionary) {
		this.prefixDictionary = prefixDictionary;
	}

	public Dictionary getEuldDictionary() {
		return euldDictionary;
	}

	public void setEuldDictionary(Dictionary euldDictionary) {
		this.euldDictionary = euldDictionary;
	}

	public Dictionary getStateDictionary() {
		return stateDictionary;
	}

	public void setStateDictionary(Dictionary stateDictionary) {
		this.stateDictionary = stateDictionary;
	}

}
