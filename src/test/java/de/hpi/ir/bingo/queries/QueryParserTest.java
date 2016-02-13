package de.hpi.ir.bingo.queries;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class QueryParserTest {

	@Test
	public void testParse() throws Exception {
		assertThat(QueryParser.parse("foo")).isEqualTo(new NormalQuery(ImmutableList.of(new TermQuery("foo")), 0));
	}

	@Test
	public void testParsePrefix() throws Exception {
		assertThat(QueryParser.parse("foo*")).isEqualTo(new NormalQuery(ImmutableList.of(new PrefixQuery("foo", QueryOperator.DEFAULT)), 0));
	}

	@Test
	public void testParseDash() throws Exception {
		assertThat(QueryParser.parse("foo-bar")).isEqualTo(new NormalQuery(ImmutableList.of(
				new TermQuery("fooxbar")/*, new TermQuery("foo"), new TermQuery("bar")*/), 0));
	}

	@Test
	public void testParsePhrase() throws Exception {
		assertThat(QueryParser.parse("\"foo bar\"")).isEqualTo(new NormalQuery(ImmutableList.of(
				new PhraseQuery(ImmutableList.of(new TermQuery("foo"), new TermQuery("bar")), QueryOperator.DEFAULT)
		), 0));
	}

	@Test
	public void testParsePrf() throws Exception {
		assertThat(QueryParser.parse("foo #1")).isEqualTo(new NormalQuery(ImmutableList.of(new TermQuery("foo")), 1));
	}

	@Test
	public void testComplexParse1() throws Exception {
		assertThat(QueryParser.parse("mobile OR \"data processing\"")).isEqualTo(new BooleanQuery(ImmutableList.of(
				new TermQuery("mobil"),
				new PhraseQuery(ImmutableList.of(new TermQuery("data"), new TermQuery("process")), QueryOperator.OR)

		)));
	}

	@Test
	public void testComplexParse2() throws Exception {
		assertThat(QueryParser.parse("data NOT info*")).isEqualTo(new BooleanQuery(ImmutableList.of(
				new TermQuery("data"),
				new PrefixQuery("info", QueryOperator.NOT)
		)));
	}

	@Test
	public void testComplexParse3() throws Exception {
		assertThat(QueryParser.parse("\"data proces*\" NOT processing")).isEqualTo(new BooleanQuery(ImmutableList.of(
				new PhraseQuery(ImmutableList.of(new TermQuery("data"), new PrefixQuery("proces", QueryOperator.DEFAULT)), QueryOperator.DEFAULT),
				new TermQuery("process", QueryOperator.NOT)
		)));
	}

	@Test
	public void testComplexParse4() throws Exception {
		assertThat(QueryParser.parse("\"data proces*\" #4")).isEqualTo(new NormalQuery(ImmutableList.of(
				new PhraseQuery(ImmutableList.of(new TermQuery("data"), new PrefixQuery("proces", QueryOperator.DEFAULT)), QueryOperator.DEFAULT)
		), 4));
	}

	@Test
	public void testComplexParse5() throws Exception {
		assertThat(QueryParser.parse("\"data processing\" mobile #2")).isEqualTo(new NormalQuery(ImmutableList.of(
				new PhraseQuery(ImmutableList.of(new TermQuery("data"), new TermQuery("process")), QueryOperator.DEFAULT),
				new TermQuery("mobil")
		), 2));
	}
}