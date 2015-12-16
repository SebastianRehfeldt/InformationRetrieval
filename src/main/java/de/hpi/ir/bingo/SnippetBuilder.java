package de.hpi.ir.bingo;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.List;

public class SnippetBuilder {

	private static final int WINDOWS_SIZE = 6;
	private static final int MAX_SNIPPET_AFTER_END = 50;
	SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

	public String createSnippet(PatentData patent, PostingListItem item) {
		List<Token> abstractToken = tokenizer.tokenizeStopStem(patent.getAbstractText());
		int abstractOffset = patent.getAbstractOffset();
		int start = 0;
		IntArrayList positions = item.getPositions();
		while (start < positions.size() && positions.get(start) < abstractOffset)
			start++; // skip title

		if (start == positions.size()) {
			int end = patent.getAbstractText().indexOf('.');
			boolean addElipses = false;
			if (end > 50) {
				end = 50;
				addElipses = true;
			}
			return patent.getAbstractText().substring(0, end).trim() + (addElipses ? "..." : "");
		}

		int bestStart = 0;
		int bestEnd = 0;
		int end = start;
		while (end < positions.size()) {
			while (end < positions.size() && positions.get(end) - positions.get(start) <= WINDOWS_SIZE) {
				end++;
			}
			if (bestEnd - bestStart < end - start) {
				bestStart = start;
				bestEnd = end;
			}
			// window found
			while (end < positions.size() && positions.get(end) - positions.get(start) > WINDOWS_SIZE) {
				start++;
			}
		}
		Token startToken = abstractToken.get(positions.getInt(bestStart) - abstractOffset);
		Token endToken = abstractToken.get(positions.getInt(bestEnd - 1) - abstractOffset);

		int snippetStart = 1 + Math.max(Math.max(patent.getAbstractText().lastIndexOf('.', startToken.begin),
				patent.getAbstractText().lastIndexOf('?', startToken.begin)),
				patent.getAbstractText().lastIndexOf(';', startToken.begin));

		int snippetEnd = Math.min(Math.min(indexOrSize(patent.getAbstractText(), '.', endToken.end),
				indexOrSize(patent.getAbstractText(), '?', endToken.end)),
				indexOrSize(patent.getAbstractText(), ';', endToken.end));

		if (snippetEnd == -1) {
			snippetEnd = patent.getAbstractText().length();
		}

		boolean addElipsis = false;
		if (snippetEnd - endToken.end > MAX_SNIPPET_AFTER_END) {
			snippetEnd = endToken.end + MAX_SNIPPET_AFTER_END;
			addElipsis = true;
		}

		return patent.getAbstractText().substring(snippetStart, snippetEnd).trim() + (addElipsis ? "..." : "");
	}

	private int indexOrSize(String text, char character, int fromIndex) {
		int index = text.indexOf(character, fromIndex);
		return index >= 0 ? index : text.length();
	}
}
