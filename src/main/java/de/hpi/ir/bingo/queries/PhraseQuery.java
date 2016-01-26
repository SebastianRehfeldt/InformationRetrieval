package de.hpi.ir.bingo.queries;

import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.index.Table;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

public final class PhraseQuery implements QueryPart {
	private final List<QueryPart> parts;
	private final QueryOperator queryOperator;

	public PhraseQuery(List<QueryPart> parts, QueryOperator queryOperator) {
		this.parts = parts;
		this.queryOperator = queryOperator;
	}

	@Override
	public QueryOperator getOperator() {
		return queryOperator;
	}

	@Override
	public QueryResultList execute(Table<PostingList> index, Int2ObjectMap<IntList> citations) {
		QueryResultList postingList = parts.get(0).execute(index, citations);

		for (int i = 1; i < parts.size(); i++) {
			QueryResultList list = parts.get(i).execute(index, citations);
			postingList = postingList.combinePhrase(list);
		}
		return postingList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PhraseQuery)) return false;
		PhraseQuery that = (PhraseQuery) o;
		return Objects.equals(parts, that.parts) && Objects.equals(queryOperator, that.queryOperator);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parts, queryOperator);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue((queryOperator != QueryOperator.DEFAULT ? queryOperator + " " : " ") + parts)
				.toString();
	}
}
