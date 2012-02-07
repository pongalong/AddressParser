package com.tscp.address.euld;

import com.tscp.address.AddressToken;

public class Euld {
	private AddressToken euld;
	private String eulv;

	public Euld() {
		// do nothing
	}

	public Euld(AddressToken euld, String eulv) {
		this.euld = euld;
		this.eulv = eulv;
	}

	public AddressToken getEuld() {
		return euld;
	}

	public void setEuld(AddressToken euld) {
		this.euld = euld;
	}

	public String getEulv() {
		return eulv;
	}

	public void setEulv(String eulv) {
		this.eulv = eulv;
	}

	@Override
	public String toString() {
		return euld.getValue() + " " + eulv;
	}

}
