package de.hpi.ir.bingo.queries;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import de.hpi.ir.bingo.PatentData;
import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.PostingListItem;
import de.hpi.ir.bingo.index.Table;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

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
	public List<QueryResultItem> execute(Table<PostingList> index, Table<PatentData> patents, Int2ObjectMap<IntList> citations) {
			if (parts.isEmpty()) {
				return Collections.emptyList();
			}
			QueryResultList resultList = parts.get(0).execute(index, citations);
			resultList.calculateTfidfScores(1.0, patents.getSize());
			for (int i = 1; i < parts.size(); i++) {
				QueryResultList resultList2 = parts.get(i).execute(index, citations);
				resultList2.calculateTfidfScores(1.0, patents.getSize());

				switch (queryOperator) {
					case AND:
						resultList = resultList.and(resultList2);
						break;
					case OR:
						resultList = resultList.or(resultList2);
						break;
					case NOT:
						resultList = resultList.not(resultList2);
						break;
				}
			}
			List<QueryResultItem> result = Lists.newArrayList(resultList.getItems());
			result.sort(QueryResultList.SCORE_COMPARATOR);
			return result;
	}
}
