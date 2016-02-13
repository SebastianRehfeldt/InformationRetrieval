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
import java.util.regex.Pattern;

public final class SearchEngineTokenizer {

	private final Analyzer analyzer = new CustomAnalyzer();

	private List<Token> tokenizeStopStem(Reader reader) {

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
		return tokenizeStopStem(text, true);
	}

	//private static final Pattern PATTERN = Pattern.compile("\\b(\\w+)-(\\w+)\\b");
	private static final Pattern PATTERN = Pattern.compile("(\\w)-(\\w)");

	public List<Token> tokenizeStopStem(String text, boolean replaceDashes) {
		if(replaceDashes)
			//text = PATTERN.matcher(text).replaceAll("$1$2 $1 $2"); // changes "add-on" to "addon", "add", "on"
			text = PATTERN.matcher(text).replaceAll("$1X$2"); // changes "add-on" to "addXon"
		return tokenizeStopStem(new StringReader(text));
	}
}
