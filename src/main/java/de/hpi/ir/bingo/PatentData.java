package de.hpi.ir.bingo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.hpi.ir.bingo.index.TableMerger;
import de.hpi.ir.bingo.index.TfidfToken;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatentData implements Serializable, TableMerger.Mergeable<PatentData> {
	private static final long MAXIMPORTANTTERMS = 7;
	private final int patentId;
	private final String title;
	private final String abstractText;
	private final String claimText;
	private int abstractOffset;
	private int claimOffset;
	private final List<TfidfToken> importantTerms;

	private PatentData() {
		this(-1, "", "", "");
	}

	public PatentData(int patentId, String title, String abstractText, String claimText) {
		this(patentId, title, abstractText, claimText, -1, -1, Lists.newArrayList());
	}

	private PatentData(int patentId, String title, String abstractText, String claimText, int abstractOffset, int claimOffset, List<TfidfToken> importantTerms) {
		this.patentId = patentId;
		this.title = title;
		this.abstractText = abstractText;
		this.claimText = claimText;
		this.abstractOffset = abstractOffset;
		this.claimOffset = claimOffset;
		this.importantTerms = importantTerms;
	}

	public int getPatentId() {
		return patentId;
	}

	public String getTitle() {
		return title;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public List<TfidfToken> getImportantTerms(){
		return importantTerms;
	}

	public void calculateImportantTerms(Map<String, Double> idf) {
		Verify.verify(importantTerms.isEmpty());
		SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();
		// title added multiple times to make it more important...
		List<Token> tokens = tokenizer.tokenizeStopStem(title + " " + title + " " + title + " " + abstractText);
		Map<String, Integer> tf = Maps.newHashMap();
		for (Token token : tokens) {
			if (tf.containsKey(token.text)) {
				tf.put(token.text, tf.get(token.text) + 1);
			} else {
				tf.put(token.text, 1);
			}
		}
		List<TfidfToken> tfidfToken = Lists.newArrayList();
		for (Map.Entry<String, Integer> entry : tf.entrySet()) {
			String key = entry.getKey();
			Double value = entry.getValue() * idf.get(key);
			tfidfToken.add(new TfidfToken(key, value));
		}
		Comparator<TfidfToken> c = Comparator.comparing(TfidfToken::getTfidf);
		tfidfToken.sort(c.reversed());
		List<TfidfToken> terms = tfidfToken.stream().limit(MAXIMPORTANTTERMS).collect(Collectors.toList());
		importantTerms.addAll(terms);
	}

	public int getAbstractOffset() {
		return abstractOffset;
	}

	public void setAbstractOffset(int abstractOffset) {
		this.abstractOffset = abstractOffset;
	}

	@Override
	public PatentData mergedWith(PatentData other) {
		throw new NotImplementedException(); // shouldnt be neccessary
	}

	public String getClaimText() {
		return claimText;
	}

	public void setClaimOffset(int claimOffset) {
		this.claimOffset = claimOffset;
	}

	public static class PatentDataSerializer extends Serializer<PatentData> {
		public void write(Kryo kryo, Output output, PatentData data) {
			output.writeInt(data.patentId);
			output.writeString(data.title);
			output.writeString(data.abstractText);
			output.writeInt(data.abstractOffset);
			output.writeInt(data.claimOffset);
			output.writeInt(data.importantTerms.size());
			for (TfidfToken token : data.importantTerms) {
				output.writeString(token.getText());
				output.writeDouble(token.getTfidf());
			}
		}

		public PatentData read(Kryo kryo, Input input, Class<PatentData> type) {
			int patentId = input.readInt();
			String title = input.readString();
			String abstractText = input.readString();
			int abstractOffset = input.readInt();
			int claimOffset = input.readInt();
			int size = input.readInt();
			List<TfidfToken> importantTerms = Lists.newArrayList();
			for (int i = 0; i < size; i++) {
				String s = input.readString();
				Double v = input.readDouble();
				importantTerms.add(new TfidfToken(s, v));
			}
			return new PatentData(patentId, title, abstractText, "", abstractOffset, claimOffset, importantTerms);
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("patentId", patentId)
				.add("title", title)
				.toString();
	}
}
