package de.hpi.ir.bingo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * {@link Analyzer} for English.
 */
public final class CustomAnalyzer extends StopwordAnalyzerBase {
	private final CharArraySet stemExclusionSet;

	final static CharArraySet DEFAULT_STOP_SET;

	static {
		List<CharSequence> stopWords = Arrays.asList(
				"a", "an", "and", "are", "as", "at", "be", "but", "by",
				"for", "if", "in", "into", "is", "it",
				"no", "not", "of", "on", "or", "such",
				"that", "the", "their", "then", "there", "these",
				"they", "this", "to", "was", "will", "with",
				// additional:
				"1", "2", "compris", "claim", "wherein", "3", "4", "5", "6", "7", "from", "8",
				"9", "10", "one", "includ", "11", "further", "12", "have", "least",
				"13", "first", "14", "second", "15", "which", "between", "16", "each",
				"use", "17", "accord", "has", "be", "18", "plural", "when", "form",
				"base", "configur", "than", "19", "20", "said", "more", "two", "can"
		);

		CharArraySet stopSet = new CharArraySet(stopWords, false);
		DEFAULT_STOP_SET = CharArraySet.unmodifiableSet(stopSet);
	}

	public CustomAnalyzer() {
		super(DEFAULT_STOP_SET);
		this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.EMPTY_SET);
	}

	/**
	 * Creates a {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} which tokenizes
	 * all the text in the provided {@link Reader}.
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		final Tokenizer source;
		source = new StandardTokenizer();
		TokenStream result = new StandardFilter(source);
		result = new EnglishPossessiveFilter(result);
		result = new LowerCaseFilter(result);
		result = new StopFilter(result, stopwords);
		if (!stemExclusionSet.isEmpty())
			result = new SetKeywordMarkerFilter(result, stemExclusionSet);
		result = new SnowballFilter(result, new EnglishStemmer());
		return new TokenStreamComponents(source, result);
	}
}

