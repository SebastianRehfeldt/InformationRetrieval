package de.hpi.ir.bingo.queries;

import com.google.common.base.Objects;

public final class PrefixQuery implements Query {
	private final String prefix;

	public PrefixQuery(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PrefixQuery)) return false;
		PrefixQuery that = (PrefixQuery) o;
		return Objects.equal(prefix, that.prefix);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(prefix);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("prefix", prefix)
				.toString();
	}
}
