package de.hpi.ir.bingo.queries;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.QPDecoderStream;

import de.hpi.ir.bingo.SearchEngineTokenizer;
import de.hpi.ir.bingo.Token;

public class QueryParser {

	private static final String queryPartRegex = "(AND|OR|NOT)|#([0-9]+)|LinkTo:([0-9]+)|\"(.+?)\"|([^ ]+)";
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
			String linkToId = m.group(3);
			if (linkToId != null) {
				int linkTo = Integer.parseInt(linkToId);
				queryParts.add(new LinkToQuery(linkTo));
			}
			String phrase = m.group(4);
			if (phrase != null) {
				List<QueryPart> phraseParts = parseParts(phrase);
				queryParts.add(new PhraseQuery(phraseParts));
			}
			String term = m.group(5);
			if (term != null) {
				queryParts.addAll(parsePart(term));
			}
		}

		if (queryOperator == null) {
			return new NormalQuery(queryParts, prf);
		} else {
			return new BooleanQuery(queryParts, queryOperator);
		}
	}

	private static List<QueryPart> parsePart(String term) {
		if (term.endsWith("*")) {
			return ImmutableList.of(new PrefixQuery(term.substring(0, term.length()-1)));
		} else {
			List<QueryPart> result = Lists.newArrayList();
			for (Token token : tokenizer.tokenizeStopStem(term)) {
				result.add(new TermQuery(token.text));
			}
			return result;
		}
	}

	private static List<QueryPart> parseParts(String phrase) {
		List<QueryPart> parts = Arrays.stream(phrase.split(" ")).map(QueryParser::parsePart).flatMap((Collection::stream)).collect(Collectors.toList());
		return parts;
	}

}
