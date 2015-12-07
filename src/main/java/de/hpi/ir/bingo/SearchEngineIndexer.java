package de.hpi.ir.bingo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.esotericsoftware.kryo.Serializer;

import de.hpi.ir.bingo.index.TableReader;
import de.hpi.ir.bingo.index.TableUtil;
import de.hpi.ir.bingo.index.TableWriter;

import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		//writeIndexTerms(index);

		TableWriter<PostingList> indexWriter = new TableWriter<>(Paths.get(directory, indexName), true, PostingList.class, serializer);
		indexWriter.writeMap(index);
		indexWriter.close();

		TableWriter<PatentData> patentWriter = new TableWriter<>(Paths.get(directory, "patents-temp"), true, PatentData.class, null);
		patentWriter.writeMap(patents);
		patentWriter.close();

		calculateImportantTerms(directory, indexName, serializer);
	}

	private void printIndex(Map<String, PostingList> index) {
		System.out.println("--------------     Index     -------------");
		List<String> keys = Lists.newArrayList(index.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			System.out.print("\"" + key + "\"");
 			/*for(PostingListItem postingListItem : index.get(key).getItems()){
 				System.out.print(postingListItem.toString()+" ");
 			}*/
			System.out.println("");
		}
		System.out.println("--------------     Index End    -------------");
	}

	//index creation
	private Map<String, PostingListItem> buildIndexForDocument(PatentData patent) {
		Map<String, PostingListItem> docIndex = new HashMap<>();
		List<Token> titleTokens = tokenizer.tokenizeStopStem(patent.getTitle());
		List<Token> abstractTokens = tokenizer.tokenizeStopStem(patent.getAbstractText());
		int totalSize = titleTokens.size() + abstractTokens.size();

		int pos = 0;
		pos += addToIndex(patent.getPatentId(), pos, totalSize, titleTokens.size(), titleTokens, docIndex);
		patent.setAbstractOffset(pos);
		pos += addToIndex(patent.getPatentId(), pos, totalSize, titleTokens.size(), abstractTokens, docIndex);
		return docIndex;
	}

	private int addToIndex(int patentId, int offset, int totalSize, int titleWordCount,
			               List<Token> tokens, Map<String, PostingListItem> docIndex) {
		int position = offset;

		for (Token word : tokens) {
			PostingListItem item = docIndex.get(word.text);
			if (item == null) {
				item = new PostingListItem(patentId, totalSize, titleWordCount);
				docIndex.put(word.text, item);
			}
			item.addPosition(position++);
		}
		return tokens.size();
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

	/**
	 * Reads the postinglists to calculate idf values. Reads the patent index and calculates
	 * the important terms according to their idf values.
	 */
	private void calculateImportantTerms(String directory, String indexName, Serializer<PostingList> serializer) {
		int totalDocumentCount = TableUtil.getTableIndex(Paths.get(directory, "patents-temp")).getSize();
		TableReader<PostingList> postingReader = new TableReader<>(Paths.get(directory, indexName), PostingList.class, serializer);
		Map<String, Double> idf = Maps.newHashMap();
		Map.Entry<String, PostingList> token;
		while((token = postingReader.readNext()) != null) {
			double idfValue = Math.log(totalDocumentCount / (double) token.getValue().getDocumentCount());
			idf.put(token.getKey(), idfValue);
		}
		postingReader.close();

		TableReader<PatentData> patentReader = new TableReader<>(Paths.get(directory, "patents-temp"), PatentData.class, null);
		TableWriter<PatentData> patentWriter = new TableWriter<>(Paths.get(directory, "patents"), true, PatentData.class, null);
		Map.Entry<String, PatentData> patent;
		while((patent = patentReader.readNext()) != null) {
			patent.getValue().calculateImportantTerms(idf);
			patentWriter.put(patent.getKey(), patent.getValue());
		}
		patentReader.close();
		patentWriter.close();
	}
}
