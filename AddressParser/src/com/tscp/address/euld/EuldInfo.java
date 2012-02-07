package com.tscp.address.euld;

import java.util.ArrayList;

/**
 * CLASS: EuldInfo Represents EULD information
 * 
 * @author jpong
 * 
 */
public class EuldInfo extends ArrayList<Euld> {
	private static final long serialVersionUID = -7910258248615510829L;

	public void addEuld(Euld euld) {
		super.add(euld);
	}

	public String toString() {
		StringBuilder euldString = new StringBuilder();
		for (Euld euld : this) {
			euldString.append(euld.toString()).append(" ");
		}
		return euldString.toString();
	}

}