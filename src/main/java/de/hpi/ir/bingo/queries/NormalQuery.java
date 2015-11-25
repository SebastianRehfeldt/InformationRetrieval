package de.hpi.ir.bingo.queries;

import java.util.List;

import com.google.common.base.Objects;

public final class NormalQuery implements Query {
	private final List<Query> parts;
	private final int prf;

	public NormalQuery(List<Query> parts, int prf) {
		this.parts = parts;
		this.prf = prf;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NormalQuery)) return false;
		NormalQuery that = (NormalQuery) o;
		return prf == that.prf &&
				Objects.equal(parts, that.parts);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(parts, prf);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("parts", parts)
				.add("prf", prf)
				.toString();
	}
}
