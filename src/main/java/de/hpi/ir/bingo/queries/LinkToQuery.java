package de.hpi.ir.bingo.queries;


import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.PostingListItem;
import de.hpi.ir.bingo.index.Table;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

public final class LinkToQuery implements QueryPart {

	private int linkTo;
	private final QueryOperator queryOperator;

	public LinkToQuery(int linkTo, QueryOperator queryOperator) {
		this.linkTo = linkTo;
		this.queryOperator = queryOperator;
	}

	@Override
	public QueryOperator getOperator() {
		return queryOperator;
	}

	@Override
	public QueryResultList execute(Table<PostingList> index, Int2ObjectMap<IntList> citations) {
		QueryResultList results = new QueryResultList();
		IntList cites = (IntList) citations.get(linkTo);
		if (cites == null) {
			return new QueryResultList();
		}
		for (Integer cite : cites) {
			results.addItem(new QueryResultItem(new PostingListItem(cite, 0, (short) 0, (short) 0), 0, "linked"));
		}
		return results;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LinkToQuery)) return false;
		LinkToQuery that = (LinkToQuery) o;
		return linkTo == that.linkTo && Objects.equals(queryOperator, that.queryOperator);
	}

	@Override
	public int hashCode() {
		return Objects.hash(linkTo);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(queryOperator != QueryOperator.DEFAULT ? queryOperator + " " : " ")
				.add("linkTo", linkTo)
				.toString();
	}
}
