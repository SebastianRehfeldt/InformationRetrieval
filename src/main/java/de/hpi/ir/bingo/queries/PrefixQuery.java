package de.hpi.ir.bingo.queries;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.index.Table;

public final class PrefixQuery implements QueryPart {
	private final String prefix;

	public PrefixQuery(String prefix) {
		this.prefix = prefix;
	}


	@Override
	public QueryResultList execute(Table<PostingList> index) {
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
