package com.tscp.address;

import java.util.ArrayList;

/**
 * CLASS: CandidateList Represents the list of candidates returned by MapInfo
 * 
 * @author jpong
 * 
 */
public class CandidateList {
	public ArrayList<ParsedAddress> candidate;

	public CandidateList() {
		// do nothing
	}

	public void clear() {
		candidate.clear();
	}

	public int getNumCandidates() {
		if (candidate != null) {
			return candidate.size();
		} else {
			return 0;
		}
	}

	public void add(ParsedAddress cand) {
		if (candidate == null) {
			candidate = new ArrayList<ParsedAddress>();
		}
		candidate.add(cand);
	}

	public void setCandidates(ArrayList<ParsedAddress> list) {
		candidate = list;
	}

	public void print() {
		if (candidate != null) {
			for (int i = 0; i < candidate.size(); i++) {
				System.out.println(i + 1 + ": " + candidate.get(i).toString());
			}
		} else {
			System.out.println("None");
		}
	}
}