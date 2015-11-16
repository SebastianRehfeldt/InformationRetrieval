package de.hpi.ir.bingo;

import java.io.StringReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Serializer;
import com.google.common.collect.Maps;

import de.hpi.ir.bingo.index.TableWriter;

public class SearchEngineIndexer {
	private final SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

	public void createIndex(String fileName, String directory, String indexName, Serializer<PostingList> serializer) {

		Map<String, PostingList> index = Maps.newHashMap();
		Map<String, PatentData> patents = Maps.newHashMap();

		PatentHandler.parseXml(fileName, (patent) -> {
			Map<String, PostingListItem> docIndex = buildIndexForDocument(patent);
			mergeDocIndexIntoMainIndex(index, docIndex);
			patents.put(Integer.toString(patent.getPatentId()), patent);
		});
		
		//printIndex(index);

		TableWriter<PostingList> indexWriter = new TableWriter<>(Paths.get(directory, indexName), true, PostingList.class, serializer);
		indexWriter.writeMap(index);
		indexWriter.close();

		TableWriter<PatentData> patentWriter = new TableWriter<>(Paths.get(directory, "patents"), true, PatentData.class, null);
		patentWriter.writeMap(patents);
		patentWriter.close();
	}

	private void printIndex(Map<String, PostingList> index) {
		System.out.println("--------------     Index     -------------");
		for(String key : index.keySet()){
 			System.out.print("\""+ key+"\": ");
 			/*for(PostingListItem postingListItem : index.get(key).getItems()){
 				System.out.print(postingListItem.toString()+" ");
 			}*/
 			System.out.println("");
 		}
		System.out.println("--------------     Index End    -------------");
	}

	//index creation
	private Map<String, PostingListItem> buildIndexForDocument(PatentData patent) {
		List<String> tokens = tokenizer.tokenizeStopStem(new StringReader(patent.getTitle() + " " + patent.getAbstractText()));
		Map<String, PostingListItem> docIndex = new HashMap<>();
		int position = 0;

		for (String word : tokens) {
			position++;
			if (docIndex.containsKey(word)) {
				docIndex.get(word).addPosition(position);
			} else {
				docIndex.put(word, new PostingListItem(patent.getPatentId(), position, tokens.size()));
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
