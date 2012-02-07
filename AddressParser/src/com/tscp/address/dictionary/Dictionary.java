package com.tscp.address.dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import com.tscp.address.AddressToken;
import com.tscp.address.euld.Euld;

/* ***********************************************************************************
 * Created By: JPONG
 * Properties subclass that also contains the reverse HashMap of the <key,value> pairs
 *********************************************************************************** */
public class Dictionary extends Properties {

	private static final long serialVersionUID = 1L;
	private static final String WHITESPACE = " \t\r\n";
	private Properties reverseDictionary;
	private int direction;

	public Dictionary(int direction) {
		this.direction = direction;
		if (direction == 2) {
			reverseDictionary = new Properties();
		}
	}

	public Dictionary(int direction, String filename) {
		this.direction = direction;
		if (direction == 2) {
			reverseDictionary = new Properties();
		}
		this.load(filename);
	}

	public boolean is2Way() {
		return direction == 2;
	}

	public void load(String fileName) {
		InputStream inputStream;
		try {
			inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
			super.load(inputStream);
			inputStream.close();
		} catch (IOException ioe) {
			System.out.println("Exception while loading properties file");
			ioe.printStackTrace();
		}
		if (direction == 2) {
			Enumeration<Object> eValues = super.elements();
			Enumeration<Object> eKeys = super.keys();
			while (eKeys.hasMoreElements()) {
				reverseDictionary.setProperty((String) eValues.nextElement(), (String) eKeys.nextElement());
			}
		}
	}

	public String getCity(String value) {
		return value.substring(0, value.indexOf(":"));
	}

	public String getState(String value) {
		return value.substring(value.indexOf(":") + 1);
	}

	public String translate(String key) {
		key = key.toUpperCase();
		return super.getProperty(key, key);
	}

	public void translate(AddressToken token) {
		String key = token.getValue().toUpperCase();
		token.setValue(super.getProperty(key, key));
	}

	public void translate(Euld euld) {
		String key = euld.getEuld().getValue().toUpperCase();
		euld.getEuld().setValue((super.getProperty(key, key)));
		key = euld.getEulv().toUpperCase();
		euld.setEulv(super.getProperty(key, key));
	}

	public boolean containsKeyword(String block) {
		StringTokenizer st = new StringTokenizer(block, WHITESPACE);
		while (st.hasMoreTokens()) {
			if (this.containsKey(st.nextToken()))
				return true;
		}
		return false;
	}

	public boolean containsKey(String key) {
		if (direction == 2) {
			return (super.containsKey(key) || reverseDictionary.containsKey(key));
		} else {
			return super.containsKey(key);
		}
	}

	public boolean isShort(String key) {
		if (direction == 2)
			return super.containsKey(key);
		else
			return false;
	}

	public boolean isLong(String key) {
		if (direction == 2)
			return reverseDictionary.containsKey(key);
		else
			return false;
	}

	public String getShort(String key) {
		if (direction == 2)
			return reverseDictionary.getProperty(key);
		else
			return null;
	}

	public String getLong(String key) {
		if (direction == 2)
			return super.getProperty(key);
		else
			return null;
	}
}