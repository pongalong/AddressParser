package com.tscp.address.token;

import com.tscp.address.dictionary.ilec.AddressDictionary;

public class Token {
	private static final AddressDictionary addressDictionary = new AddressDictionary();
	public int position = -1;
	public Token prev;
	public Token next;
	public String value = "";
	public String suggestionType;
	private TokenType type = TokenType.UNKNOWN;
	private BlockType blockType = BlockType.UNKNOWN;

	public Token(String value) {
		this.value = value;
	}

	public void clear() {
		position = -1;
		prev = null;
		next = null;
		value = null;
		suggestionType = null;
		type = TokenType.UNKNOWN;
		blockType = BlockType.UNKNOWN;
	}

	/**
	 * FUNCTION: evaluates and returns the tokens type
	 * 
	 * @return 0 = numeric 1 = character 2 = alpha numeric
	 */
	public TokenType getType() {
		if (this.type == TokenType.UNKNOWN) {
			if (value.matches("\\d+")) {
				type = TokenType.NUMERIC;
			} else if (value.matches("\\D+")) {
				type = TokenType.CHARACTER;
			} else {
				type = TokenType.ALPHANUMBERIC;
			}
		}
		return this.type;
	}

	/**
	 * FUNCTION: evaluates and returns the tokens blockType
	 * 
	 * @return
	 */
	public BlockType getBlockType() {
		if (blockType != BlockType.UNKNOWN) {
			return blockType;
		} else {
			blockType = BlockType.UNKNOWN;
			if (prev != null) {
				if (prev.getBlockType() == BlockType.EULD) {
					blockType = BlockType.EULV;
					return blockType;
				}
			}
			if (this.getType() == TokenType.NUMERIC) {
				if ((value.matches("\\d{5}") || value.matches("\\d{5}['-]\\d{4}")) && next == null) {
					blockType = BlockType.ZIP;
					return blockType;
				}
				blockType = BlockType.STNUM;
				return blockType;
			}
			if (this.getType() == TokenType.CHARACTER) {
				if (addressDictionary.getSuffixDictionary().containsKey(value)) {
					blockType = BlockType.SUFFIX;
					return blockType;
				}
				if (addressDictionary.getPrefixDictionary().containsKey(value)) {
					blockType = BlockType.PREFIX;
					return blockType;
				}
				if (addressDictionary.getEuldDictionary().containsKey(value)) {
					blockType = BlockType.EULD;
					return blockType;
				}
				if (!value.matches("[a-zA-Z]{2}")) {
					blockType = BlockType.UNKNOWN;
					return blockType;
				}
				if (addressDictionary.getStateDictionary().containsKey(value)) {
					blockType = BlockType.STATE;
					return blockType;
				}
			}
			if (this.getType() == TokenType.ALPHANUMBERIC) {
				if (this.prev != null) {
					if (this.prev.getBlockType() == BlockType.EULD) {
						blockType = BlockType.EULV;
						return blockType;
					}
				} else {
					blockType = BlockType.STNUM;
					return blockType;
				}
			}
			return blockType;
		}
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	public void setBlockType(BlockType blockType) {
		this.blockType = blockType;
	}

	/**
	 * FUNCTION: returns the string representation of all tokens after this
	 * 
	 * @return
	 * 
	 *         public String nString() { String nConcat = ""; if
	 *         (type.equals("character")) nConcat = value; if (next != null) { if
	 *         (next.type.equals("character") &&
	 *         !next.getBlockType().equals("suffix") &&
	 *         !next.getBlockType().equals("prefix") &&
	 *         !next.getBlockType().equals("euld") &&
	 *         !next.getBlockType().equals("eulv") &&
	 *         !next.getBlockType().equals("state")) { nConcat = nConcat + " " +
	 *         next.nString(); } } return nConcat; }
	 */

	/**
	 * FUNCTION: returns the string representation of all tokens before this
	 * 
	 * @return public String pString() { String pConcat = ""; if
	 *         (type.equals("character")) pConcat = value; if (prev != null) { if
	 *         (prev.type.equals("character") &&
	 *         !prev.getBlockType().equals("suffix") &&
	 *         !prev.getBlockType().equals("prefix") &&
	 *         !prev.getBlockType().equals("euld") &&
	 *         !prev.getBlockType().equals("eulv") &&
	 *         !prev.getBlockType().equals("state")) { pConcat = prev.pString() +
	 *         " " + pConcat; } } return pConcat; }
	 */

	/**
	 * FUNCTION: returns the string representation of tokens after this within the
	 * given range
	 * 
	 * @param range
	 * @return public String nString(int range) { String nConcat = ""; if
	 *         (type.equals("character")) nConcat = value; if (range > 0) { if
	 *         (next != null) { if (next.type.equals("character") &&
	 *         !next.getBlockType().equals("suffix") &&
	 *         !next.getBlockType().equals("prefix") &&
	 *         !next.getBlockType().equals("euld") &&
	 *         !next.getBlockType().equals("eulv") &&
	 *         !next.getBlockType().equals("state")) { nConcat = nConcat + " " +
	 *         this.next.nString(range - 1); } } } return nConcat; }
	 */

	/**
	 * FUNCTION: returns the string representation of tokens before this within
	 * the given range
	 * 
	 * @param range
	 * @return public String pString(int range) { String pConcat = ""; if
	 *         (type.equals("character")) pConcat = value; if (range > 0) { if
	 *         (next != null) { if (next.type.equals("character") &&
	 *         !next.getBlockType().equals("suffix") &&
	 *         !next.getBlockType().equals("prefix") &&
	 *         !next.getBlockType().equals("euld") &&
	 *         !next.getBlockType().equals("eulv") &&
	 *         !next.getBlockType().equals("state")) { pConcat =
	 *         this.prev.pString(range - 1) + " " + pConcat; } } } return pConcat;
	 *         }
	 */
}
