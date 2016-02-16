package de.hpi.ir.bingo.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.hpi.ir.bingo.PatentData;
import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.Settings;
import de.hpi.ir.bingo.SnippetBuilder;
import de.hpi.ir.bingo.index.Table;
import de.hpi.ir.bingo.index.TfidfToken;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

public final class NormalQuery implements Query {
	private final List<QueryPart> parts;
	private final int prf;

	public NormalQuery(List<QueryPart> parts, int prf) {
		this.parts = parts;
		this.prf = prf;
	}

	@Override
	public List<QueryResultItem> execute(Table<PostingList> index, Table<PatentData> patents, Int2ObjectMap<IntList> citations) {
		if (parts.isEmpty()) {
			return Collections.emptyList();
		}
		QueryResultList previusResult = null;
		QueryResultList finalResult = parts.get(0).execute(index, citations);
		finalResult.calculateTfidfScores(1.0, patents.getSize());
		if (parts.get(0) instanceof TermQuery) {
			previusResult = finalResult;
		}
		for (int i = 1; i < parts.size(); i++) {
			QueryResultList currentResult = parts.get(i).execute(index, citations);
			currentResult.calculateTfidfScores(1.0, patents.getSize());
			finalResult = finalResult.combine(currentResult, 1);
			if (parts.get(i) instanceof TermQuery && Settings.HIGH_QUALITY) { // check if terms occur next to each other
				if (previusResult != null) {
					QueryResultList combinedResult = previusResult.combinePhrase(currentResult);
					combinedResult.calculateTfidfScores(0.1, patents.getSize());
					finalResult = finalResult.combineOnlyScore(combinedResult);
				}
				previusResult = currentResult;
			} else {
				previusResult = null;
			}
		}

		if (prf > 0) {
			QueryResultItem[] result = finalResult.getItems().toArray(new QueryResultItem[finalResult.getItems().size()]);
			Arrays.parallelSort(result, QueryResultList.SCORE_COMPARATOR);
			List<String> topToken = getPrfToken(Arrays.asList(result), patents);
			System.out.println("extend query with: " + topToken);
			for (String stringDoubleEntry : topToken) {
				QueryResultList resultList2 = new TermQuery(stringDoubleEntry, null).execute(index, citations);
				resultList2.calculateTfidfScores(0.01, patents.getSize());
				finalResult = finalResult.combine(resultList2, 0.0001);
			}
		}
		QueryResultItem[] result = finalResult.getItems().toArray(new QueryResultItem[finalResult.getItems().size()]);
		Arrays.parallelSort(result, QueryResultList.SCORE_COMPARATOR);
		return Arrays.asList(result);
	}

	private List<String> getPrfToken(List<QueryResultItem> result, Table<PatentData> patents) {
		Map<String, Double> importantTokens = Maps.newHashMap();
		Set<String> queryterms = Sets.newHashSet();
		for (QueryPart part : parts) {
			if (part instanceof TermQuery) {
				queryterms.add(((TermQuery)part).getTerm());
			}
		}
		for (QueryResultItem resultItem : result.subList(0, Math.min(prf, result.size()))) {
			PatentData patentData = patents.get(Integer.toString(resultItem.getPatentId()));
			assert patentData != null;
			String snippet = new SnippetBuilder().createSnippet(patentData, resultItem.getItem());
			resultItem.setSnippet(snippet);
			for (TfidfToken token : patentData.getImportantTerms()) {
				String key = token.getText();
				if(!snippet.contains(key)) {
					continue;
				}
				if (queryterms.contains(key)) {
					continue;
				}
				importantTokens.compute(key, (k, v) -> (v != null ? v : 0) + token.getTfidf());
			}
		}
		// extend query
		Comparator<Map.Entry<String, Double>> c = Map.Entry.comparingByValue();
		List<Map.Entry<String, Double>> entries = Lists.newArrayList(importantTokens.entrySet());
		entries.sort(c.reversed());
		return entries.subList(0, Math.min(Settings.PRF_EXTENSION_SIZE, entries.size())).stream().map(Map.Entry::getKey).collect(Collectors.toList());
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NormalQuery)) return false;
		NormalQuery that = (NormalQuery) o;
		return prf == that.prf &&
				Objects.equal(parts, that.parts);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(parts, prf);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(parts)
				.add("prf", prf)
				.toString();
	}
}
