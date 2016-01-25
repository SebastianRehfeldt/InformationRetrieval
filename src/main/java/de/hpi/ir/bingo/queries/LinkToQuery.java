package de.hpi.ir.bingo.queries;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.hpi.ir.bingo.PatentData;
import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.PostingListItem;
import de.hpi.ir.bingo.SnippetBuilder;
import de.hpi.ir.bingo.index.Table;
import de.hpi.ir.bingo.index.TfidfToken;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LinkToQuery implements QueryPart {

	private int linkTo;

	public LinkToQuery(int linkTo) {
		this.linkTo = linkTo;
	}

	@Override
	public QueryResultList execute(Table<PostingList> index, Int2ObjectMap<IntList> citations) {
		QueryResultList results = new QueryResultList();
		IntList cites = (IntList) citations.get(linkTo);
		if (cites == null) {
			return new QueryResultList();
		}
		for (Integer cite : cites) {
			results.addItem(new QueryResultItem(new PostingListItem(cite, 0, 0), 0, "linked"));
		}
		return results;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LinkToQuery)) return false;
		LinkToQuery that = (LinkToQuery) o;
		return linkTo == that.linkTo;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(linkTo);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("linkTo", linkTo)
				.toString();
	}
}
