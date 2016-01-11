package de.hpi.ir.bingo;

import org.junit.Before;
import org.junit.Test;

import jdk.nashorn.internal.objects.annotations.Setter;

import static org.junit.Assert.*;
import static com.google.common.truth.Truth.assertThat;

public class SnippetBuilderTest {

	SnippetBuilder snippetBuilder = new SnippetBuilder();
	PatentData patent = new PatentData(1, "foo bar test",
			"foo bar mobile device. bar foo bad liebenwerda. hamster foo mobile", "");

	@Before
	public void setup() {
		patent.setAbstractOffset(3);
	}

	@Test
	public void testCreateSnippet() throws Exception {
		PostingListItem item = new PostingListItem(1, new int[]{9, 10}, 14,10);//bad liebenwerda
		String snippet = snippetBuilder.createSnippet(patent, item);
		assertThat(snippet).isEqualTo("bar foo bad liebenwerda");
	}

	@Test
	public void testCreateSnippetFirstSentence() throws Exception {
		PostingListItem item = new PostingListItem(1, new int[]{5, 6, 13}, 14,10); //mobile device
		String snippet = snippetBuilder.createSnippet(patent, item);
		assertThat(snippet).isEqualTo("foo bar mobile device");
	}

	@Test
	public void testCreateSnippetLastSentence() throws Exception {
		PostingListItem item = new PostingListItem(1, new int[]{11}, 14,10);
		String snippet = snippetBuilder.createSnippet(patent, item);
		assertThat(snippet).isEqualTo("hamster foo mobile");
	}


	@Test
	public void testCreateEmptySnippet() throws Exception {
		PostingListItem item = new PostingListItem(1, new int[]{2}, 14,10);
		String snippet = snippetBuilder.createSnippet(patent, item);
		assertThat(snippet).isEqualTo("foo bar mobile device");
	}
}