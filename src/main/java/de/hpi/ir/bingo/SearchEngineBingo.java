package de.hpi.ir.bingo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.hpi.ir.bingo.index.Table;
import de.hpi.ir.bingo.index.TableReader;
import de.hpi.ir.bingo.index.TableWriter;

import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;


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
	private Table<PatentData> patentIndex;
	private final SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

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
	ArrayList<String> search(String query, int topK, int prf) {
		this.topK = topK;
		this.prf = prf;
		List<PostingListItem> result;

		System.out.println(query);

		query = query.replace("*", "xxx");
		List<String> processedQuery = tokenizer.tokenizeStopStem(new StringReader(query));
		System.out.println(processedQuery);

		if (query.startsWith("'") && query.endsWith("'")) {
			System.out.println("Phrase query");
			result = getPostingListForPhraseQuery(processedQuery);
		} else if (query.contains("OR")) {
			result = getPostingListForQuery(processedQuery, QueryOperators.OR);
			System.out.println("OR query");
		} else if (query.contains("NOT")) {
			System.out.println("NOT query");
			result = getPostingListForQuery(processedQuery, QueryOperators.NOT);
		} else if (query.contains("AND")) {
			System.out.println("AND query");
			result = getPostingListForQuery(processedQuery, QueryOperators.AND);
		} else {
			System.out.println("Normal query");
			result = getRankedPostingLists(processedQuery);
		}


		ArrayList<String> titles = new ArrayList<>();
		//find for each postinglistitem the patent and retrieve the title of this patent
		for (PostingListItem patent : result.subList(0, Math.min(topK, result.size()))) {
			PatentData patentData = patentIndex.get(Integer.toString(patent.getPatentId()));
			assert patentData != null;
			String title = patentData.getTitle();
//			if(query.startsWith("'") && query.endsWith("'")){
//				String phrase = query.substring(1, query.length()-1).toLowerCase();
//				if(patentData.getAbstractText().toLowerCase().contains(phrase)){
//					titles.add(title);
//				}
//			}
//			else{
//				titles.add(title);
//			}
			titles.add(patent.getPatentId() + " " + title);

		}

		return titles;
	}

	private List<PostingListItem> getRankedPostingLists(List<String> processedQuery) {
		List<Entry<Double, PostingListItem>> result = Lists.newArrayList();

		{
			PostingList postingList = index.get(processedQuery.get(0));
			if (postingList == null) return Collections.emptyList();
			for (PostingListItem item : postingList.getItems()) {
				result.add(Maps.immutableEntry(tfidf(postingList, item), item));
			}
		}

		for (String searchWord : processedQuery.subList(1, processedQuery.size())) {
			PostingList postingList = index.get(searchWord);
			if (postingList == null) return Collections.emptyList();

			List<PostingListItem> items = postingList.getItems();
			List<Entry<Double, PostingListItem>> newResult = Lists.newArrayList();
			int i1 = 0, i2 = 0;
			while (i1 < result.size() && i2 < items.size()) {
				PostingListItem p1 = result.get(i1).getValue();
				PostingListItem p2 = items.get(i2);
				if (p1.getPatentId() < p2.getPatentId()) {
					i1++;
				} else if (p1.getPatentId() == p2.getPatentId()) {
					PostingListItem and = p1.merge(p2);
					double tfidf = tfidf(postingList, p2);
					newResult.add(Maps.immutableEntry(result.get(i1).getKey() * tfidf, and));
					i1++;
					i2++;
				} else {
					i2++;
				}
			}
			result = newResult;
		}
		result.sort(Map.Entry.comparingByKey());
		List<PostingListItem> resultItems = result.stream().map(Entry::getValue).collect(Collectors.toList());
		return resultItems;
	}

	private double tfidf(PostingList postingList, PostingListItem item) {
		return item.getTermFrequency() * Math.log(patentIndex.getSize() / (double) postingList.getItems().size());
	}

	private enum QueryOperators {
		AND, OR, NOT
	}

	private List<PostingListItem> getPostingListForQuery(List<String> processedQuery, QueryOperators operator) {
		PostingList postingList = null;
		for (String searchWord : processedQuery) {
			PostingList items;
			if (searchWord.endsWith("xxx")) {
				List<Entry<String, PostingList>> prefixResult = index.getWithPrefix(searchWord.substring(0, searchWord.length() - 3));
				items = new PostingList();
				for (Entry<String, PostingList> entry : prefixResult) {
					items = items.or(entry.getValue());
				}
			} else {
				items = index.get(searchWord);
				if (items == null) {
					items = new PostingList();
				}
			}
			if (postingList == null) {
				postingList = items;
			} else {
				switch (operator) {
					case AND:
						postingList = postingList.and(items);
						break;
					case OR:
						postingList = postingList.or(items);
						break;
					case NOT:
						postingList = postingList.not(items);
						break;
				}
			}
		}
		if (postingList == null) {
			return Collections.emptyList();
		}
		return postingList.getItems();
	}

	private List<PostingListItem> getPostingListForPhraseQuery(List<String> query) {
		PostingList postingList = index.get(query.get(0));
		if (postingList == null) return Collections.emptyList();

		for (int i = 1; i < query.size(); i++) {
			PostingList list = index.get(query.get(i));
			if (list == null) {
				return Collections.emptyList();
			}
			postingList = postingList.combinePhrase(list);
		}
		List<PostingListItem> items = postingList.getItems();
		items.sort(Comparator.comparing((item) -> item.getPositions().size()));
		return items;
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