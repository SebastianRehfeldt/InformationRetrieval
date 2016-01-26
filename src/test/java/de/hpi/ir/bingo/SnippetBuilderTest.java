package de.hpi.ir.bingo;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class SnippetBuilderTest {

	SnippetBuilder snippetBuilder = new SnippetBuilder();
	PatentData patent = new PatentData(1, "foo bar test",
			"foo bar mobile device. bar foo bad liebenwerda. hamster foo mobile", "",null);

	@Before
	public void setup() {
		patent.setAbstractOffset(3);
		patent.setTextOffset(14);
	}

	@Test
	public void testCreateSnippet() throws Exception {
		PostingListItem item = new PostingListItem(1, new int[]{9, 10}, 14,(short)10, (short)0);//bad liebenwerda
		String snippet = snippetBuilder.createSnippet(patent, item);
		assertThat(snippet).isEqualTo("bar foo bad liebenwerda");
	}

	@Test
	public void testCreateSnippetWithClaim() throws Exception {
		PostingListItem item = new PostingListItem(1, new int[]{20}, 14,(short)10, (short)0);//bad liebenwerda
		String snippet = snippetBuilder.createSnippet(patent, item);
		assertThat(snippet).isEqualTo("foo bar mobile device");
	}

	@Test
	public void testCreateSnippetFirstSentence() throws Exception {
		PostingListItem item = new PostingListItem(1, new int[]{5, 6, 13}, 14,(short)10, (short)0); //mobile device
		String snippet = snippetBuilder.createSnippet(patent, item);
		assertThat(snippet).isEqualTo("foo bar mobile device");
	}

	@Test
	public void testCreateSnippetLastSentence() throws Exception {
		PostingListItem item = new PostingListItem(1, new int[]{11}, 14,(short)10, (short)0);
		String snippet = snippetBuilder.createSnippet(patent, item);
		assertThat(snippet).isEqualTo("hamster foo mobile");
	}


	@Test
	public void testCreateEmptySnippet() throws Exception {
		PostingListItem item = new PostingListItem(1, new int[]{2}, 14,(short)10, (short)0);
		String snippet = snippetBuilder.createSnippet(patent, item);
		assertThat(snippet).isEqualTo("foo bar mobile device");
	}
}