package com.tscp.address;

import com.tscp.address.listcode.ListCode;

public class AddressToken implements AddressTokenModel {
	private String value;
	private ListCode listCode = new ListCode();

	public AddressToken() {
		// do nothing
	}

	public AddressToken(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ListCode getListCode() {
		return listCode;
	}

	public void setListCode(ListCode code) {
		this.listCode = code;
	}

	public boolean isEmpty() {
		return value == null || value.isEmpty();
	}

	@Override
	public String toString() {
		return getValue();
	}

	public void clear() {
		value = null;
		listCode = null;
	}
}
