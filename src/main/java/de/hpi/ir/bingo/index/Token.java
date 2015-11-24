package de.hpi.ir.bingo.index;

import com.google.common.base.Objects;

public class Token {
	private final String text;
	private final Double tfidf;

	public Token(String text, Double tfidf) {
		this.text = text;
		this.tfidf = tfidf;
	}

	public String getText() {
		return text;
	}

	public Double getTfidf() {
		return tfidf;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Token token = (Token) o;
		return Objects.equal(text, token.text) &&
				Objects.equal(tfidf, token.tfidf);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(text, tfidf);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("text", text)
				.add("tfidf", tfidf)
				.toString();
	}
}
