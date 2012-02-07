package com.tscp.address.dictionary.ilec;

import com.tscp.address.dictionary.Dictionary;

public class AddressDictionary {
	private Dictionary euldDictionary = new Dictionary(2, "dictionary/euld.properties");
	private Dictionary suffixDictionary = new Dictionary(2, "dictionary/suffix.properties");
	private Dictionary prefixDictionary = new Dictionary(2, "dictionary/prefix.properties");
	private Dictionary stateDictionary = new Dictionary(2, "dictionary/states.properties");

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
