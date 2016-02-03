package de.hpi.ir.bingo.queries;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.index.Table;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

public final class PrefixQuery implements QueryPart {
	private final String prefix;
	private final QueryOperator queryOperator;

	public PrefixQuery(String prefix, QueryOperator queryOperator) {
		this.prefix = prefix;
		this.queryOperator = queryOperator;
	}

	@Override
	public QueryResultList execute(Table<PostingList> index, Int2ObjectMap<IntList> citations) {
		List<Map.Entry<String, PostingList>> prefixResult = index.getWithPrefix(prefix);
		if (prefixResult.isEmpty()) {
			return new QueryResultList();
		}
		QueryResultList resultList = new QueryResultList(prefixResult.get(0).getValue());
		for (int i = 1; i < prefixResult.size(); i++) {
			QueryResultList other = new QueryResultList(prefixResult.get(i).getValue());
			resultList = resultList.or(other);
		}
		return resultList;
	}

	public QueryOperator getOperator() {
		return queryOperator;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PrefixQuery)) return false;
		PrefixQuery that = (PrefixQuery) o;
		return Objects.equals(prefix, that.prefix) && Objects.equals(queryOperator, that.queryOperator);
	}

	@Override
	public int hashCode() {
		return Objects.hash(prefix, queryOperator);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(queryOperator != QueryOperator.DEFAULT ? queryOperator + " " : null)
				.add("prefix", prefix)
				.omitNullValues()
				.toString();
	}

}
