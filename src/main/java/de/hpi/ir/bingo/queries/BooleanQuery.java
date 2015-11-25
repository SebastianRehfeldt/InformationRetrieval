package de.hpi.ir.bingo.queries;

import java.util.List;

import com.google.common.base.Objects;

public final class BooleanQuery implements Query {
	private final List<Query> parts;
	private final QueryOperators queryOperator;

	public BooleanQuery(List<Query> parts, QueryOperators queryOperator) {
		this.parts = parts;
		this.queryOperator = queryOperator;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BooleanQuery)) return false;
		BooleanQuery that = (BooleanQuery) o;
		return Objects.equal(parts, that.parts) &&
				queryOperator == that.queryOperator;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(parts, queryOperator);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("parts", parts)
				.add("queryOperator", queryOperator)
				.toString();
	}
}
