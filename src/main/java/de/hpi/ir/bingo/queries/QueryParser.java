package de.hpi.ir.bingo.queries;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import de.hpi.ir.bingo.SearchEngineTokenizer;

public class QueryParser {

	private static final String queryPartRegex = "(AND|OR|NOT)|#([0-9]+)|\"(.+?)\"|([^ ]+)";
	private static final Pattern queryPartPattern = Pattern.compile(queryPartRegex);

	private static final SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

	public static Query parse(String query) {

		Matcher m = queryPartPattern.matcher(query);
		List<QueryPart> queryParts = Lists.newArrayList();
		QueryOperators queryOperator = null;
		int prf = 0;
		while (m.find()) {
			String operator = m.group(1);
			if (operator != null) {
				queryOperator = QueryOperators.valueOf(operator);
			}
			String prfString = m.group(2);
			if (prfString != null) {
				prf = Integer.parseInt(prfString);
			}
			String phrase = m.group(3);
			if (phrase != null) {
				List<QueryPart> phraseParts = parseParts(phrase);
				queryParts.add(new PhraseQuery(phraseParts));
			}
			String term = m.group(4);
			if (term != null) {
				queryParts.add(parsePart(term));
			}
		}

		if (queryOperator == null) {
			return new NormalQuery(queryParts, prf);
		} else {
			return new BooleanQuery(queryParts, queryOperator);
		}
	}

	private static QueryPart parsePart(String term) {
		if (term.endsWith("*")) {
			return new PrefixQuery(term.substring(0, term.length()-1));
		} else {
			return new TermQuery(tokenizer.tokenizeStopStem(term).get(0).text);
		}
	}

	private static List<QueryPart> parseParts(String phrase) {
		List<QueryPart> parts = Arrays.stream(phrase.split(" ")).map(QueryParser::parsePart).collect(Collectors.toList());
		return parts;
	}

}
