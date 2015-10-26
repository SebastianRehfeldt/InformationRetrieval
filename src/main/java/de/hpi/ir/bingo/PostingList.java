package de.hpi.ir.bingo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PostingList {
    List<PostingListItem> items = new ArrayList<>();

    Collection<PostingListItem> getItems() {
        return items;
    }

    void addItem(PostingListItem item) {
        items.add(item);
    }

    public void addAll(PostingList list) {
        items.addAll(list.items);
    }
}
