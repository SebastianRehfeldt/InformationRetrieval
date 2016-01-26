package de.hpi.ir.bingo.queries;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.hpi.ir.bingo.PatentData;
import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.SnippetBuilder;
import de.hpi.ir.bingo.index.Table;
import de.hpi.ir.bingo.index.TfidfToken;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

public final class NormalQuery implements Query {
	private static final int PRF_EXTENSION_SIZE = 5;

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
		QueryResultList resultList = parts.get(0).execute(index, citations);
		resultList.calculateTfidfScores(1.0, patents.getSize());
		for (int i = 1; i < parts.size(); i++) {
			QueryResultList resultList2 = parts.get(i).execute(index, citations);
			resultList2.calculateTfidfScores(1.0, patents.getSize());
			resultList = resultList.combine(resultList2);
		}

		List<QueryResultItem> result = Lists.newArrayList(resultList.getItems());
		result.sort(QueryResultList.SCORE_COMPARATOR);

		if (prf > 0) {
			List<String> topToken = getPrfToken(result, patents);
			System.out.println("extend query with: " + topToken);
			for (String stringDoubleEntry : topToken) {
				QueryResultList resultList2 = new TermQuery(stringDoubleEntry, null).execute(index, citations);
				resultList2.calculateTfidfScores(0.1, patents.getSize());
				resultList = resultList.combine(resultList2);
			}
			result = Lists.newArrayList(resultList.getItems());
			result.sort(QueryResultList.SCORE_COMPARATOR);
		}
		return result;
	}

	private List<String> getPrfToken(List<QueryResultItem> result, Table<PatentData> patents) {
		Map<String, Double> importantTokens = Maps.newHashMap();
		for (QueryResultItem resultItem : result.subList(0, Math.min(prf, result.size()))) {
			PatentData patentData = patents.get(Integer.toString(resultItem.getPatentId()));
			String snippet = new SnippetBuilder().createSnippet(patentData, resultItem.getItem());
			resultItem.setSnippet(snippet);
			assert patentData != null;
			for (TfidfToken token : patentData.getImportantTerms()) {
				String key = token.getText();
				if(!snippet.contains(key)) {
					continue;
				}
				if (importantTokens.containsKey(key)) {
					importantTokens.put(key, importantTokens.get(key) + token.getTfidf());
				} else {
					importantTokens.put(key, token.getTfidf());
				}
			}
		}
		// extend query
		Comparator<Map.Entry<String, Double>> c = Map.Entry.comparingByValue();
		List<Map.Entry<String, Double>> entries = Lists.newArrayList(importantTokens.entrySet());
		entries.sort(c.reversed());
		return entries.subList(0, Math.min(PRF_EXTENSION_SIZE, entries.size())).stream().map(Map.Entry::getKey).collect(Collectors.toList());
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
