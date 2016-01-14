package de.hpi.ir.bingo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import de.hpi.ir.bingo.index.Table;
import de.hpi.ir.bingo.index.TableReader;
import de.hpi.ir.bingo.index.TableWriter;
import de.hpi.ir.bingo.queries.Query;
import de.hpi.ir.bingo.queries.QueryParser;
import de.hpi.ir.bingo.queries.QueryResultItem;


/**
 * @author: Bingo
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015 <p/> This is your
 * file! implement your search engine here! <p/> Describe your search engine briefly: -
 * multi-threaded? - stemming? - stopword removal? - index algorithm? - etc. <p/> Keep in mind to
 * include your implementation decisions also in the pdf file of each assignment
 */
public class SearchEngineBingo extends SearchEngine {

	public static final double LOG2 = Math.log(2.0);
	private Table<PostingList> index;
	private Table<PatentData> patentIndex;
	private final SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();
	private final SnippetBuilder snippetBuilder = new SnippetBuilder();

	public SearchEngineBingo() {
		super();
	}

	@Override
	void index() {
		//String fileName = "res/testData.xml";
		//String fileName = "compressed_patents/ipg150106.fixed.zip";
		String fileName = "k:/data/patentData.zip";
		new SearchEngineIndexer(teamDirectory).createIndex(fileName, "index", PostingList.NORMAL_SERIALIZER);
	}

	@Override
	boolean loadIndex() {
		index = Table.open(Paths.get(teamDirectory, "index"), PostingList.class, PostingList.NORMAL_SERIALIZER);
		patentIndex = Table.open(Paths.get(teamDirectory, "patents"), PatentData.class, null);
		return true;
	}

	@Override
	void compressIndex() {
		System.out.println("compressing index");
		TableReader<PostingList> reader = new TableReader<>(Paths.get(teamDirectory, "index"), PostingList.class,
				PostingList.NORMAL_SERIALIZER);
		TableWriter<PostingList> writer = new TableWriter<>(Paths.get(teamDirectory, "compressed-index"), true,
				PostingList.class, PostingList.COMPRESSING_SERIALIZER);
		Map.Entry<String, PostingList> posting;
		while ((posting = reader.readNext()) != null) {
			writer.put(posting.getKey(), posting.getValue());
		}
		reader.close();
		writer.close();
	}

	void printIndexStats(String directory) {
		System.out.println("writing stats");
		TableReader<PostingList> reader = new TableReader<>(Paths.get(directory, "index"), PostingList.class,
				PostingList.NORMAL_SERIALIZER);
		Map.Entry<String, PostingList> posting;
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get("stats.txt"), Charsets.UTF_8);
			while ((posting = reader.readNext()) != null) {
				writer.write(posting.getKey() + "|||" + posting.getValue().getDocumentCount() + "\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		reader.close();
	}

	@Override
	boolean loadCompressedIndex() {
		index = Table.open(Paths.get(teamDirectory, "compressed-index"), PostingList.class, PostingList.COMPRESSING_SERIALIZER);
		patentIndex = Table.open(Paths.get(teamDirectory, "patents"), PatentData.class, null);
		return true;
	}

	class SearchResult {
		int patentId;
		String title;
		String snippet;

		public SearchResult(int patentId, String title, String snippet) {
			this.patentId = patentId;
			this.title = title;
			this.snippet = snippet;
		}

		@Override
		public String toString() {
			return "" + patentId + " " + title + "\n" + snippet;
		}
	}

	@Override
	ArrayList<String> search(String query, int topK) {
		List<SearchResult> result = searchWithSearchResult(query, topK);
		return new ArrayList<>(getTitles(result));
	}

	public List<String> getTitles(List<SearchResult> result) {
		return result.stream().map(r -> r.title).collect(Collectors.toList());
	}

	List<SearchResult> searchWithSearchResult(String query, int topK) {
		System.out.println(query);

		Query queryObject = QueryParser.parse(query);
		System.out.println(queryObject);

		List<QueryResultItem> result = queryObject.execute(index, patentIndex);

		List<SearchResult> searchResult = new ArrayList<>();

		//find for each postinglistitem the patent and retrieve the title of this patent
		List<QueryResultItem> topResult = result.subList(0, Math.min(topK, result.size()));
		for (QueryResultItem resultItem : topResult) {
			PatentData patentData = patentIndex.get(Integer.toString(resultItem.getPatentId()));
			assert patentData != null;
			String title = patentData.getTitle();
			String snippet = resultItem.getSnippet();
			if (snippet == null) { // snippets might already be created if prf>0
				snippet = snippetBuilder.createSnippet(patentData, resultItem.getItem());
			}
			searchResult.add(new SearchResult(patentData.getPatentId(), title, snippet));
		}
		return searchResult;
	}
	
	
	// returns the normalized discounted cumulative gain at a particular rank position 'p'
	@Override
	Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int p) {
		return computeNdcg((List<String>)goldRanking, (List<String>)ranking, p);
	}

	// better signature with List and double instead of ArrayList and Double
	double computeNdcg(List<String> goldRanking, List<String> ranking, int p) {
		Map<String, Double> gains = Maps.newHashMap();
		double perfectDcg = 0;
		for (int i = 0; i < goldRanking.size(); i++) {
			double gain = 1.0 + Math.floor(10 * Math.pow(0.5, 0.1 * (i+1)));
			gains.putIfAbsent(goldRanking.get(i), gain);
			if (i < ranking.size()) {
				if (i == 0) {
					perfectDcg += gain;
				} else {
					perfectDcg += gain / ((Math.log(i + 1) / LOG2));
				}
			}
		}
		double dcg = 0;
		for (int i = 0; i < Math.min(p, ranking.size()); i++) {
			String title = ranking.get(i);
			Double gain = gains.get(title);
			if (gain != null) {
				if (i == 0) {
					dcg += gain;
				} else {
					dcg += gain / ((Math.log(i + 1) / LOG2));
				}
			}
		}
		return dcg/perfectDcg;
	}

	//printing
	void printPatentTitles() {
		String fileName = "res/testData.xml";

		PatentHandler.parseXml(fileName, (patent) -> {
			System.out.printf("%d: %s\n", patent.getPatentId(), patent.getTitle());
			System.out.println(tokenizer.tokenizeStopStem(patent.getAbstractText()));
		});
	}

	void writeIndexTerms() throws IOException {
		Writer writer = Files.newBufferedWriter(Paths.get("indexterms.txt"), Charsets.UTF_8);
		TableReader<PostingList> reader = new TableReader<>(Paths.get("compressed-index"), PostingList.class, PostingList.COMPRESSING_SERIALIZER);
		Entry<String, PostingList> entry;
		while ((entry = reader.readNext()) != null) {
			writer.write(entry.getKey() + "\n");
		}
		writer.close();
	}
}