package de.hpi.ir.bingo;

import org.junit.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class SearchEngineTokenizerTest {

	SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

	@Test
	public void testTokenizeStopStem() throws Exception {
		List<Token> tokens = tokenizer.tokenizeStopStem("add-on - module");
		assertThat(tokens.get(0).text).isEqualTo("addon");
		assertThat(tokens.get(1).text).isEqualTo("add");
		assertThat(tokens.get(2).text).isEqualTo("modul");
	}

	@Test
	public void testTokenizeStopStem2() throws Exception {
		List<Token> tokens = tokenizer.tokenizeStopStem("def add-123 xyz");
		assertThat(tokens.get(0).text).isEqualTo("def");
		assertThat(tokens.get(1).text).isEqualTo("add123");
		assertThat(tokens.get(2).text).isEqualTo("add");
		assertThat(tokens.get(3).text).isEqualTo("123");
		assertThat(tokens.get(4).text).isEqualTo("xyz");
	}

	@Test
	public void testTokenizeStopStem3() throws Exception {
		List<Token> tokens = tokenizer.tokenizeStopStem("add, module");
		assertThat(tokens.get(0).text).isEqualTo("add");
		assertThat(tokens.get(1).text).isEqualTo("modul");
	}

}