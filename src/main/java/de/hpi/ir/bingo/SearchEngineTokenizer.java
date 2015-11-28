package de.hpi.ir.bingo;

import com.google.common.collect.Lists;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class SearchEngineTokenizer {

	private final Analyzer analyzer = new CustomAnalyzer();

	public List<Token> tokenizeStopStem(Reader reader) {

		try {
			TokenStream stream = analyzer.tokenStream("", reader);
			stream.reset();
			stream.addAttribute(OffsetAttribute.class);
			List<Token> result = Lists.newArrayList();
			while (stream.incrementToken()) {
				String token = stream.getAttribute(CharTermAttribute.class).toString();
				OffsetAttribute attribute = stream.getAttribute(OffsetAttribute.class);
				result.add(new Token(token, attribute.startOffset(), attribute.endOffset()));
			}
			stream.close();
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Token> tokenizeStopStem(String text) {
		return tokenizeStopStem(new StringReader(text));
	}
}
