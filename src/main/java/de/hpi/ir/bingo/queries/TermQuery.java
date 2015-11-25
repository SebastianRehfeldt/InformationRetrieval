package de.hpi.ir.bingo.queries;

import com.google.common.base.Objects;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.index.Table;

public final class TermQuery implements QueryPart {
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
				.addValue(term)
				.toString();
	}

	@Override
	public QueryResultList execute(Table<PostingList> index) {
		PostingList postingList = index.get(term);
		return postingList == null ? new QueryResultList() : new QueryResultList(postingList);
	}
}
