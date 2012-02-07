package com.tscp.address.token;


public class TokenList {
	private Token first;
	private Token last;

	public TokenList() {
		this.first = null;
		this.last = null;
	}

	public TokenList(Token token) {
		token.position = 0;
		this.first = token;
		this.last = token;
	}

	public Token getFirst() {
		return this.first;
	}

	public Token getLast() {
		return this.last;
	}

	public void add(Token token) {
		if (this.first == null) {
			token.position = 0;
			first = token;
			last = token;
		} else {
			this.last.next = token;
			token.position = last.position + 1;
			token.prev = this.last;
			this.last = token;
		}
	}

	public void remove(Token token) {
		if (token == first && first.next != null) {
			first = first.next;
			first.prev = null;
			first.position = 0;
			updateIndecies(first);
		} else if (token == last && last.prev != null) {
			last = last.prev;
			last.next = null;
		} else {
			token.prev.next = token.next;
			token.next.prev = token.prev;
			updateIndecies(token.prev);
			token.next = null;
			token.prev = null;
		}
	}

	public void addBefore(Token inToken, Token indexToken) {
		if (indexToken == first) {
			inToken.next = indexToken;
			indexToken.prev = inToken;
			first = inToken;
			first.position = 0;
			updateIndecies(first);
		} else {
			inToken.next = indexToken;
			inToken.prev = indexToken.prev;
			indexToken.prev.next = inToken;
			indexToken.prev = inToken;
			inToken.position = inToken.prev.position + 1;
			updateIndecies(inToken);
		}
	}

	public void addAfter(Token inToken, Token indexToken) {
		if (indexToken == last) {
			inToken.prev = indexToken;
			indexToken.next = inToken;
			last = inToken;
			last.position = last.prev.position + 1;
		} else {
			inToken.next = indexToken.next;
			inToken.prev = indexToken;
			indexToken.next.prev = inToken;
			indexToken.next = inToken;
			inToken.position = inToken.prev.position + 1;
			updateIndecies(inToken);
		}
	}

	public void mergeSimilar() {
		Token currentToken = first;
		Token nextToken;
		while (currentToken != null) {
			nextToken = currentToken.next;
			if (nextToken != null) {
				if (currentToken.getBlockType() == nextToken.getBlockType()) {
					merge(currentToken, nextToken);
					continue;
				}
			}
			currentToken = nextToken;
		}
	}

	private void merge(Token token1, Token token2) {
		token1.value = token1.value + " " + token2.value;
		token1.next = token2.next;
		if (token2.next != null) {
			token2.next.prev = token1;
			token2.next = null;
		}
		token2.prev = null;
		updateIndecies(token1);
	}

	private void updateIndecies(Token indexToken) {
		Token token = indexToken.next;
		while (token != null) {
			token.position = token.prev.position + 1;
			token = token.next;
		}
	}

	public void print() {
		Token currentToken = first;
		while (currentToken != null) {
			System.out.println(currentToken.value + " | " + currentToken.position + " | " + currentToken.getBlockType());
			currentToken = currentToken.next;
		}
	}

	public void clear() {
		while (first != null) {
			Token nextToken = first.next;
			first.prev = null;
			if (first.next == null) {
				first.clear();
				first = null;
			} else {
				first.clear();
				first = nextToken;
			}
		}
	}

}
