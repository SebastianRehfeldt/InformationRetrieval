package de.hpi.ir.bingo;

import org.junit.Test;

import de.hpi.ir.bingo.index.TfidfToken;
import de.hpi.ir.bingo.queries.LinkToQuery;
import de.hpi.ir.bingo.queries.NormalQuery;
import de.hpi.ir.bingo.queries.PhraseQuery;
import de.hpi.ir.bingo.queries.PrefixQuery;
import de.hpi.ir.bingo.queries.QueryResultItem;
import de.hpi.ir.bingo.queries.QueryResultList;
import de.hpi.ir.bingo.queries.TermQuery;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class EqualsTest {

	@Test
	public void testEquals() {
		EqualsVerifier.forClass(PostingList.class).verify();
		EqualsVerifier.forClass(PostingListItem.class)
				.withPrefabValues(IntArrayList.class, IntArrayList.wrap(new int[]{1, 2}), IntArrayList.wrap(new int[]{3})).verify();
		EqualsVerifier.forClass(LinkToQuery.class).verify();
		EqualsVerifier.forClass(NormalQuery.class).verify();
		EqualsVerifier.forClass(PhraseQuery.class).verify();
		EqualsVerifier.forClass(PrefixQuery.class).verify();
		EqualsVerifier.forClass(TermQuery.class).verify();
		EqualsVerifier.forClass(QueryResultList.class).verify();
		EqualsVerifier.forClass(QueryResultItem.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.withPrefabValues(IntArrayList.class, IntArrayList.wrap(new int[]{1, 2}), IntArrayList.wrap(new int[]{3})).verify();
		EqualsVerifier.forClass(Token.class).verify();
		EqualsVerifier.forClass(TfidfToken.class).verify();
		EqualsVerifier.forClass(Token.class).verify();
	}
}
