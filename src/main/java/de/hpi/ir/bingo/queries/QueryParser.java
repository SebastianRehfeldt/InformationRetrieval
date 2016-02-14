package de.hpi.ir.bingo.queries;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.hpi.ir.bingo.SearchEngineTokenizer;
import de.hpi.ir.bingo.Settings;
import de.hpi.ir.bingo.Token;

public final class QueryParser {

	private static final String queryPartRegex = "(AND|OR|NOT)|#([0-9]+)|LinkTo:([0-9]+)|\"(.+?)\"|([^ ]+)";
	private static final Pattern queryPartPattern = Pattern.compile(queryPartRegex);

	private static final SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

	public static Query parse(String query) {

		Matcher m = queryPartPattern.matcher(query);
		List<QueryPart> queryParts = Lists.newArrayList();

		boolean isBoolean = false;
		QueryOperator queryOperator = QueryOperator.DEFAULT;
		int prf = 0;
		while (m.find()) {
			String operator = m.group(1);
			if (operator != null) {
				isBoolean = true;
				queryOperator = QueryOperator.valueOf(operator);
			}
			String prfString = m.group(2);
			if (prfString != null) {
				prf = Integer.parseInt(prfString);
			}
			String linkToId = m.group(3);
			if (linkToId != null) {
				isBoolean = true; // linkTo queries should be ranked by a boolean relevance model
				int linkTo = Integer.parseInt(linkToId);
				queryParts.add(new LinkToQuery(linkTo, queryOperator));
			}
			String phrase = m.group(4);
			if (phrase != null) {
				List<QueryPart> phraseParts = parsePhraseParts(phrase);
				queryParts.add(new PhraseQuery(phraseParts, queryOperator));
			}
			String term = m.group(5);
			if (term != null) {
				queryParts.addAll(parsePart(term, queryOperator));
			}

			if (operator == null) {
				queryOperator = QueryOperator.DEFAULT;
			}
		}

		if (isBoolean) {
			return new BooleanQuery(queryParts);
		} else {
			return new NormalQuery(queryParts, prf);
		}
	}

	private static List<QueryPart> parsePart(String term, QueryOperator queryOperator) {
		if (term.endsWith("*")) {
			return ImmutableList.of(new PrefixQuery(term.substring(0, term.length() - 1), queryOperator));
		} else {
			List<QueryPart> result = Lists.newArrayList();
			for (Token token : tokenizer.tokenizeStopStem(term)) {
				result.add(new TermQuery(token.text, queryOperator));
			}
			return result;
		}
	}

	private static List<QueryPart> parsePhraseParts(String phrase) {
		List<QueryPart> parts = Arrays.stream(phrase.split(" ")).map(term -> parsePart(term, QueryOperator.DEFAULT)).flatMap((Collection::stream)).collect(Collectors.toList());
		return parts;
	}

}
