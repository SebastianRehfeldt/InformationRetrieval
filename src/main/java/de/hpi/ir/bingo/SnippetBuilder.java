package de.hpi.ir.bingo;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.List;

public class SnippetBuilder {

	private static final int WINDOWS_SIZE = 6;
	SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

	public String createSnippet(PatentData patent, PostingListItem item) {
		List<Token> abstractToken = tokenizer.tokenizeStopStem(patent.getAbstractText());
		int abstractOffset = patent.getAbstractOffset();
		int start = 0;
		IntArrayList positions = item.getPositions();
		while (start < positions.size() && positions.get(start) < abstractOffset) start++; // skip title

		if (start == positions.size()) {
			return "";
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

		int snippetStart = Math.max(patent.getAbstractText().lastIndexOf('.', startToken.begin),
				patent.getAbstractText().lastIndexOf('?', startToken.begin)) + 1;

		int snippetEnd = Math.min(indexOrSize(patent.getAbstractText(), '.', endToken.end),
				indexOrSize(patent.getAbstractText(), '?', endToken.end));

		if (snippetEnd == -1) {
			snippetEnd = patent.getAbstractText().length();
		}

		return patent.getAbstractText().substring(snippetStart, snippetEnd).trim();
	}

	private int indexOrSize(String text, char character, int fromIndex) {
		int index = text.indexOf(character, fromIndex);
		return index >= 0 ? index : text.length();
	}
}
