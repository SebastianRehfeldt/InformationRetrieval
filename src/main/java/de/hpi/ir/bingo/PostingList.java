package de.hpi.ir.bingo;

import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PostingList {
	private final List<PostingListItem> items = new ArrayList<>();

	public PostingList() {

	}

	public Collection<PostingListItem> getItems() {
		return items;
	}

	public void addItem(PostingListItem item) {
		items.add(item);
	}

	public void addAll(PostingList list) {
		items.addAll(list.items);
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PostingList that = (PostingList) o;
		return Objects.equal(items, that.items);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(items);
	}
}
