package de.hpi.ir.bingo;

import com.google.common.collect.Lists;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class SearchEngineTokenizer {

	public List<String> tokenizeStopStem(Reader reader) {

		Analyzer analyzer = new CustomAnalyzer();
		try {
			TokenStream stream = analyzer.tokenStream("", reader);
			//stream = new EnglishMinimalStemFilter(stream);
			stream.reset();
			//OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);
			List<String> result = Lists.newArrayList();
			while (stream.incrementToken()) {
				String token = stream.getAttribute(CharTermAttribute.class).toString();
				//OffsetAttribute attribute = stream.getAttribute(OffsetAttribute.class);
				result.add(token);
				// TODO get position, return objects
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> tokenizeStopStem(String text) {
		return tokenizeStopStem(new StringReader(text));
	}
}
