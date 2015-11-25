package de.hpi.ir.bingo.queries;

import de.hpi.ir.bingo.PostingList;

interface Query {
	default PostingList execute() {
		return null;
	}
}
