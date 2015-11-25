package de.hpi.ir.bingo.queries;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.index.Table;

public interface QueryPart {
	QueryResultList execute(Table<PostingList> index);
}
