package de.hpi.ir.bingo.queries;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import de.hpi.ir.bingo.PatentData;
import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.PostingListItem;
import de.hpi.ir.bingo.index.Table;

enum QueryOperators {
	AND, OR, NOT
}

public final class BooleanQuery implements Query {


	private final List<QueryPart> parts;
	private final QueryOperators queryOperator;

	public BooleanQuery(List<QueryPart> parts, QueryOperators queryOperator) {
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
				.addValue(parts)
				.add("queryOperator", queryOperator)
				.toString();
	}

	@Override
	public List<QueryResultItem> execute(Table<PostingList> index, Table<PatentData> patents) {
		if (parts.isEmpty()) {
			return Collections.emptyList();
		}
		QueryResultList postingList = parts.get(0).execute(index);
		postingList.calculateTfidfScores(1.0, patents.getSize());
		for (int i = 1; i < parts.size(); i++) {
			QueryResultList postingList2 = parts.get(i).execute(index);
			postingList2.calculateTfidfScores(1.0, patents.getSize());

			switch (queryOperator) {
				case AND:
					postingList = postingList.and(postingList2);
					break;
				case OR:
					postingList = postingList.or(postingList2);
					break;
				case NOT:
					postingList = postingList.not(postingList2);
					break;
			}
		}
		List<QueryResultItem> result = Lists.newArrayList(postingList.getItems());
		result.sort(QueryResultList.SCORE_COMPARATOR);
		return result;
	}
}
