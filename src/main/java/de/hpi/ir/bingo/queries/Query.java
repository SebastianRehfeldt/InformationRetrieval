package de.hpi.ir.bingo.queries;

import java.util.List;

import de.hpi.ir.bingo.PatentData;
import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.PostingListItem;
import de.hpi.ir.bingo.index.Table;

public interface Query {
	List<QueryResultItem> execute(Table<PostingList> index, Table<PatentData> patents);
}

