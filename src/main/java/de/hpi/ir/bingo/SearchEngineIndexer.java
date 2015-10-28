package de.hpi.ir.bingo;

import com.google.common.collect.Maps;

import de.hpi.ir.bingo.index.TableWriter;

import java.io.StringReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchEngineIndexer {
	private SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

	public void createIndex(String fileName, String directory) {

		Map<String, PostingList> index = Maps.newHashMap();
		Map<String, PatentData> patents = Maps.newHashMap();

		PatentHandler.parseXml(fileName, (patent) -> {
			Map<String, PostingListItem> docIndex = buildIndexForDocument(patent);
			mergeDocIndexIntoMainIndex(index, docIndex);
			patents.put(Integer.toString(patent.getPatentId()), patent);
		});

		TableWriter<PostingList> indexWriter = new TableWriter<>(Paths.get(directory, "index"), true);
		indexWriter.writeMap(index);
		indexWriter.close();

		TableWriter<PatentData> patentWriter = new TableWriter<>(Paths.get(directory, "patents"), true);
		patentWriter.writeMap(patents);
		patentWriter.close();
	}

	//index creation
	private Map<String, PostingListItem> buildIndexForDocument(PatentData patent) {
		List<String> tokens = tokenizer.tokenizeStopStem(new StringReader(patent.getAbstractText()));
		Map<String, PostingListItem> docIndex = new HashMap<>();
		int position = 0;

		for (String word : tokens) {
			position++;
			if (docIndex.containsKey(word)) {
				docIndex.get(word).addPosition(position);
			} else {
				docIndex.put(word, new PostingListItem(patent.getPatentId(), position));
			}
		}
		return docIndex;
	}

	private void mergeDocIndexIntoMainIndex(Map<String, PostingList> index, Map<String, PostingListItem> docIndex) {
		for (String word : docIndex.keySet()) {
			if (index.containsKey(word)) {
				index.get(word).addItem(docIndex.get(word));
			} else {
				PostingList postingList = new PostingList();
				postingList.addItem(docIndex.get(word));
				index.put(word, postingList);
			}
		}
	}
}
