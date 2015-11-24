package de.hpi.ir.bingo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.hpi.ir.bingo.index.Token;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatentData implements Serializable {
	private static final long MAXIMPORTANTTERMS = 7;
	private final int patentId;
	private final String title;
	private final String abstractText;
	private final List<Token> importantTerms;

	private PatentData() {
		this(-1, "", "");
	}

	public PatentData(int patentId, String title, String abstractText) {
		this(patentId, title, abstractText, Lists.newArrayList());
	}

	private PatentData(int patentId, String title, String abstractText, List<Token> importantTerms) {
		this.patentId = patentId;
		this.title = title;
		this.abstractText = abstractText;
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

	public List<Token> getImportantTerms(){
		return importantTerms;
	}

	public void calculateImportantTerms(Map<String, Double> idf) {
		Verify.verify(importantTerms.isEmpty());
		SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();
		// title added multiple times to make it more important...
		List<String> tokens = tokenizer.tokenizeStopStem(title + " " + title + " " + title + " " + abstractText);
		Map<String, Integer> tf = Maps.newHashMap();
		for (String token : tokens) {
			if (tf.containsKey(token)) {
				tf.put(token, tf.get(token) + 1);
			} else {
				tf.put(token, 1);
			}
		}
		List<Token> tfidf = Lists.newArrayList();
		for (Map.Entry<String, Integer> entry : tf.entrySet()) {
			String key = entry.getKey();
			Double value = entry.getValue() * idf.get(key);
			tfidf.add(new Token(key, value));
		}
		Comparator<Token> c = Comparator.comparing(Token::getTfidf);
		tfidf.sort(c.reversed());
		List<Token> terms = tfidf.stream().limit(MAXIMPORTANTTERMS).collect(Collectors.toList());
		importantTerms.addAll(terms);
	}

	public static class PatentDataSerializer extends Serializer<PatentData> {
		public void write(Kryo kryo, Output output, PatentData data) {
			output.writeInt(data.patentId);
			output.writeString(data.title);
			output.writeString(data.abstractText);
			output.writeInt(data.importantTerms.size());
			for (Token token : data.importantTerms) {
				output.writeString(token.getText());
				output.writeDouble(token.getTfidf());
			}
		}

		public PatentData read(Kryo kryo, Input input, Class<PatentData> type) {
			int patentId = input.readInt();
			String title = input.readString();
			String abstractText = input.readString();
			int size = input.readInt();
			List<Token> importantTerms = Lists.newArrayList();
			for (int i = 0; i < size; i++) {
				String s = input.readString();
				Double v = input.readDouble();
				importantTerms.add(new Token(s, v));
			}
			return new PatentData(patentId, title, abstractText, importantTerms);
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
