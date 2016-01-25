package de.hpi.ir.bingo.queries;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.index.Table;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

public interface QueryPart {
	QueryResultList execute(Table<PostingList> index, Int2ObjectMap<IntList> citations);
}
