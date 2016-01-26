package de.hpi.ir.bingo.queries;

import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.index.Table;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

public final class TermQuery implements QueryPart {
	private final String term;
	private final QueryOperator queryOperator;

	TermQuery(String term) {
		this(term, QueryOperator.DEFAULT);
	}

	public TermQuery(String term, QueryOperator queryOperator) {
		this.term = term;
		this.queryOperator = queryOperator;
	}

	@Override
	public QueryOperator getOperator() {
		return queryOperator;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TermQuery)) return false;
		TermQuery that = (TermQuery) o;
		return Objects.equals(term, that.term) && Objects.equals(queryOperator, that.queryOperator);
	}

	@Override
	public int hashCode() {
		return Objects.hash(term, queryOperator);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue((queryOperator != QueryOperator.DEFAULT ? queryOperator + " " : "") + term)
				.toString();
	}

	@Override
	public QueryResultList execute(Table<PostingList> index, Int2ObjectMap<IntList> citations) {
		PostingList postingList = index.get(term);
		return postingList == null ? new QueryResultList() : new QueryResultList(postingList);
	}
}
