package de.hpi.ir.bingo.queries;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.index.Table;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

public final class PhraseQuery implements QueryPart {
	private final List<QueryPart> parts;

	public PhraseQuery(List<QueryPart> parts) {
		this.parts = parts;
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
		return Objects.equal(parts, that.parts);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(parts);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.addValue(parts)
				.toString();
	}
}
