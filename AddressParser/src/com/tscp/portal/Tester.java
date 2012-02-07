package com.tscp.portal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.tscp.address.AddressUtil;
import com.tscp.address.ParsedAddress;

public class Tester {
	private static BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
	private static AddressParser addressParser = new AddressParser();
	private static ParsedAddress parsedAddress;
	private static String command;
	private static String lastCommand;

	// TEST ADDRESSES
	// "769 East El Sur Duarte CA 91010";
	// "12342 flying mist la apt 7 bldg front random words go here fontana ca 92336";

	public static void main(String[] args) throws IOException {
		boolean running = true;
		while (running) {
			System.out.print("Enter an address: ");
			command = userInput.readLine();
			if (command.equals("exit")) {
				System.out.println("System exiting...");
				running = false;
				break;
			} else if (command.equals("repeat")) {
				parseAddress(lastCommand);
			} else {
				lastCommand = command;
				parseAddress(command);
			}
		}
		System.out.println("\n\n");
	}

	private static void parseAddress(String address) throws IOException {
		addressParser.setInput(address, true);
		parsedAddress = addressParser.getAddress();
		AddressUtil.print(parsedAddress);
		System.out.print("\nUnknown Blocks: ");
		AddressUtil.printList(parsedAddress.parsedAddressDetails.getUnknown());
		System.out.print("\nPossible Errors: ");
		AddressUtil.printList(parsedAddress.parsedAddressDetails.getErrors());
		System.out.print("\nSuggestions: ");
		AddressUtil.printList(parsedAddress.parsedAddressDetails.getSuggestions());
		System.out.println("\n\nClose Matches from MapInfo:");
		parsedAddress.parsedAddressDetails.getMapInfo().print();
	}

}
