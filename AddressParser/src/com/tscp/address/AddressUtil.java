package com.tscp.address;

import java.util.List;

import com.tscp.address.euld.Euld;

public class AddressUtil {

	public static void print(ParsedAddress parsedAddress) {
		System.out.println("---------------------------------------------");
		if (!parsedAddress.stnum.isEmpty())
			System.out.format(" num:\t\t%s\n", parsedAddress.stnum);
		if (!parsedAddress.prefix.isEmpty())
			System.out.format(" pfx:\t\t%s code: %s\n", fillStr(parsedAddress.prefix), parsedAddress.prefix.getListCode()
					.getValue());
		if (!parsedAddress.stname.isEmpty())
			System.out.format(" name:\t\t%s\n", fillStr(parsedAddress.stname));
		if (!parsedAddress.suffix.isEmpty())
			System.out.format(" sfx:\t\t%s code: %s\n", fillStr(parsedAddress.suffix), parsedAddress.suffix.getListCode()
					.getValue());
		for (Euld euld : parsedAddress.euldInfo) {
			System.out.format(" euld:\t\t%s code: %s\n", fillStr(euld.getEulv() + " " + euld.getEulv()), euld.getEuld()
					.getListCode().getValue());
		}
		if (!parsedAddress.city.isEmpty())
			System.out.format(" city:\t\t%s\n", parsedAddress.city);
		if (!parsedAddress.zip.isEmpty())
			System.out.format(" zip:\t\t%s\n", parsedAddress.zip);
		if (!parsedAddress.state.isEmpty())
			System.out.format(" state:\t\t%s code: %s\n", fillStr(parsedAddress.state), parsedAddress.state.getListCode()
					.getValue());
	}

	public static void printList(List<String> list) {
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				System.out.println(" " + (i + 1) + ": " + list.get(i));
			}
		} else {
			System.out.print("NONE");
		}
	}

	private static String fillStr(String inStr) {
		StringBuilder myStr;
		if (inStr != null) {
			myStr = new StringBuilder(inStr);
			int diff = 15 - inStr.length();
			while (diff > 0) {
				myStr.append(" ");
				diff--;
			}
			return myStr.toString();
		} else {
			return fillStr("null");
		}
	}

	private static String fillStr(AddressToken token) {
		return fillStr(token.getValue());
	}

}
