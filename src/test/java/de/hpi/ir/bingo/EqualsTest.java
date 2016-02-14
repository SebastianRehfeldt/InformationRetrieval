package de.hpi.ir.bingo;

import org.junit.Test;

import de.hpi.ir.bingo.index.TfidfToken;
import de.hpi.ir.bingo.queries.LinkToQuery;
import de.hpi.ir.bingo.queries.NormalQuery;
import de.hpi.ir.bingo.queries.PhraseQuery;
import de.hpi.ir.bingo.queries.PrefixQuery;
import de.hpi.ir.bingo.queries.QueryResultList;
import de.hpi.ir.bingo.queries.TermQuery;
import nl.jqno.equalsverifier.EqualsVerifier;

public class EqualsTest {

	@Test
	public void testEquals() {
		EqualsVerifier.forClass(PostingList.class).verify();
		EqualsVerifier.forClass(LinkToQuery.class).verify();
		EqualsVerifier.forClass(NormalQuery.class).verify();
		EqualsVerifier.forClass(PhraseQuery.class).verify();
		EqualsVerifier.forClass(PrefixQuery.class).verify();
		EqualsVerifier.forClass(TermQuery.class).verify();
		EqualsVerifier.forClass(QueryResultList.class).verify();
		EqualsVerifier.forClass(Token.class).verify();
		EqualsVerifier.forClass(TfidfToken.class).verify();
		EqualsVerifier.forClass(Token.class).verify();
	}
}
