package de.hpi.ir.bingo.index;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class TfidfToken {
	private final String text;
	private final Double tfidf;

	public TfidfToken(String text, Double tfidf) {
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
		TfidfToken token = (TfidfToken) o;
		return Objects.equal(text, token.text) &&
				Objects.equal(tfidf, token.tfidf);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(text, tfidf);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("text", text)
				.add("tfidf", tfidf)
				.toString();
	}
}
