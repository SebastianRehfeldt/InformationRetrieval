package de.hpi.ir.bingo.queries;

import java.util.Collections;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import de.hpi.ir.bingo.PatentData;
import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.index.Table;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

public final class BooleanQuery implements Query {


	private final List<QueryPart> parts;

	public BooleanQuery(List<QueryPart> parts) {
		this.parts = parts;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BooleanQuery)) return false;
		BooleanQuery that = (BooleanQuery) o;
		return Objects.equal(parts, that.parts);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(parts);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(parts)
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
				QueryPart queryPart = parts.get(i);
				QueryResultList resultList2 = queryPart.execute(index, citations);
				resultList2.calculateTfidfScores(1.0, patents.getSize());

				switch (queryPart.getOperator()) {
					case AND:
						resultList = resultList.and(resultList2);
						break;
					case OR:
						resultList = resultList.or(resultList2);
						break;
					case NOT:
						resultList = resultList.not(resultList2);
						break;
					default:
						resultList = resultList.and(resultList2);
				}
			}
			List<QueryResultItem> result = Lists.newArrayList(resultList.getItems());
			result.sort(QueryResultList.ID_COMPARATOR);
			return result;
	}
}
