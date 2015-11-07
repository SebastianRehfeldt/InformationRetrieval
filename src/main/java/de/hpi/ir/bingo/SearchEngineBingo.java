package de.hpi.ir.bingo;

import de.hpi.ir.bingo.index.Table;
import de.hpi.ir.bingo.index.TableReader;
import de.hpi.ir.bingo.index.TableWriter;

import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author: Bingo
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015 <p/> This is your
 * file! implement your search engine here! <p/> Describe your search engine briefly: -
 * multi-threaded? - stemming? - stopword removal? - index algorithm? - etc. <p/> Keep in mind to
 * include your implementation decisions also in the pdf file of each assignment
 */
public class SearchEngineBingo extends SearchEngine { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName

	private Table<PostingList> index;
	private Table<PatentData> titleIndex;
	private SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

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
		titleIndex = Table.open(Paths.get(directory, "patents"), PatentData.class, null);
		return true;
	}

	@Override
	void compressIndex(String directory) {
		TableReader<PostingList> reader = new TableReader<>(Paths.get(directory, "index"), PostingList.class,
				PostingList.NORMAL_SERIALIZER);
		TableWriter<PostingList> writer = new TableWriter<>(Paths.get(directory, "compressed-index"), true,
				PostingList.class, PostingList.COMPRESSING_SERIALIZER);
		Map.Entry<String, PostingList> posting;
		while((posting = reader.readNext()) != null) {
			writer.put(posting.getKey(), posting.getValue());
		}
		reader.close();
		writer.close();
	}

	@Override
	boolean loadCompressedIndex(String directory) {
		index = Table.open(Paths.get(directory, "compressed-index"), PostingList.class, PostingList.COMPRESSING_SERIALIZER);
		titleIndex = Table.open(Paths.get(directory, "patents"), PatentData.class, null);
		return true;
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf) {
		PostingList postingList = new PostingList();
		Set<String> titles = new HashSet<>();
		List<String> processedQuery = tokenizer.tokenizeStopStem(new StringReader(query));
		String title = "";

		//phrase Query
		System.out.println(query);
		if(query.startsWith("'") && query.endsWith("'")){
			postingList = getPostingListForPhraseQuery(processedQuery);
		}
		else{
			//find the postinglist for each search term and concatenate the lists
			for (String searchWord : processedQuery) {
				PostingList items = index.get(searchWord);
				if (items != null) {
					postingList.addAll(items);
				}
			}
		}
		

		//find for each postinglistitem the patent and retrieve the title of this patent
		for (PostingListItem patent : postingList.getItems()) {
			PatentData patentData = titleIndex.get(Integer.toString(patent.getPatentId()));
			assert patentData != null;
			title = patentData.getTitle();
			titles.add(title);
		}

		return new ArrayList<>(titles);
	}

	private PostingList getPostingListForPhraseQuery(List<String> query) {
		PostingList postingList = index.get(query.get(0));
		
		for(int i=1;i<query.size();i++){
			postingList = postingList.union(index.get(query.get(i)));
		}
		
		return postingList;
	}

	//printing
	void printPatentTitles() {
		String fileName = "res/testData.xml";

		PatentHandler.parseXml(fileName, (patent) -> {
			System.out.printf("%d: %s\n", patent.getPatentId(), patent.getTitle());
			System.out.println(tokenizer.tokenizeStopStem(new StringReader(patent.getAbstractText())));
		});
	}

	void printIndex() {
		System.out.println(index.toString());
	}
}