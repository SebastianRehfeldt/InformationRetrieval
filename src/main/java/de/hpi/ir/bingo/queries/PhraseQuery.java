package de.hpi.ir.bingo.queries;

import java.util.List;

import com.google.common.base.Objects;

public final class PhraseQuery implements Query {
	private final List<Query> parts;

	public PhraseQuery(List<Query> parts) {
		this.parts = parts;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PhraseQuery)) return false;
		PhraseQuery that = (PhraseQuery) o;
		return Objects.equal(parts, that.parts);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(parts);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("parts", parts)
				.toString();
	}
}
