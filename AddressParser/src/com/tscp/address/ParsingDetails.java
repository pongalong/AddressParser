package com.tscp.address;

import java.util.ArrayList;

public class ParsingDetails {
	private CandidateList mapInfo = new CandidateList();
	private ArrayList<String> unknown = new ArrayList<String>();
	private ArrayList<String> suggestions = new ArrayList<String>();
	private ArrayList<String> errors = new ArrayList<String>();

	public CandidateList getMapInfo() {
		return mapInfo;
	}

	public void setMapInfo(CandidateList mapInfo) {
		this.mapInfo = mapInfo;
	}

	public ArrayList<String> getUnknown() {
		return unknown;
	}

	public void setUnknown(ArrayList<String> unknown) {
		this.unknown = unknown;
	}

	public ArrayList<String> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(ArrayList<String> suggestions) {
		this.suggestions = suggestions;
	}

	public ArrayList<String> getErrors() {
		return errors;
	}

	public void setErrors(ArrayList<String> errors) {
		this.errors = errors;
	}

	public void clear() {
		// TODO Clear all the lists
	}

}
