package de.hpi.ir.bingo.queries;

import com.google.common.base.Objects;

public final class TermQuery implements Query {
	private final String term;

	public TermQuery(String term) {
		this.term = term;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TermQuery)) return false;
		TermQuery termQuery = (TermQuery) o;
		return Objects.equal(term, termQuery.term);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(term);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("term", term)
				.toString();
	}
}
