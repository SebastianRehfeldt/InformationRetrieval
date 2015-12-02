package de.hpi.ir.bingo;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Charsets;

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

	private Table<PostingList> index;
	private Table<PatentData> patentIndex;
	private final SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();
	private final SnippetBuilder snippetBuilder = new SnippetBuilder();

	public SearchEngineBingo() { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	void index(String directory) {
		String fileName = "res/testData.xml";
		//String fileName = "compressed_patents/ipg150106.fixed.zip";
		new SearchEngineIndexer().createIndex(fileName, directory, "index", PostingList.NORMAL_SERIALIZER);
	}

	@Override
	boolean loadIndex(String directory) {
		index = Table.open(Paths.get(directory, "index"), PostingList.class, PostingList.NORMAL_SERIALIZER);
		patentIndex = Table.open(Paths.get(directory, "patents"), PatentData.class, null);
		return true;
	}

	@Override
	void compressIndex(String directory) {
		TableReader<PostingList> reader = new TableReader<>(Paths.get(directory, "index"), PostingList.class,
				PostingList.NORMAL_SERIALIZER);
		TableWriter<PostingList> writer = new TableWriter<>(Paths.get(directory, "compressed-index"), true,
				PostingList.class, PostingList.COMPRESSING_SERIALIZER);
		Map.Entry<String, PostingList> posting;
		while ((posting = reader.readNext()) != null) {
			writer.put(posting.getKey(), posting.getValue());
		}
		reader.close();
		writer.close();
	}

	@Override
	boolean loadCompressedIndex(String directory) {
		index = Table.open(Paths.get(directory, "compressed-index"), PostingList.class, PostingList.COMPRESSING_SERIALIZER);
		patentIndex = Table.open(Paths.get(directory, "patents"), PatentData.class, null);
		return true;
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf_IGNORED) {
		this.topK = topK;
		this.prf = prf;

		System.out.println(query);

		Query queryObject = QueryParser.parse(query);
		System.out.println(queryObject);

		List<QueryResultItem> result = queryObject.execute(index, patentIndex);

		ArrayList<String> searchResult = new ArrayList<>();

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
			searchResult.add(resultItem.getPatentId() + " " + title + " --- " + snippet);
		}

		return searchResult;
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