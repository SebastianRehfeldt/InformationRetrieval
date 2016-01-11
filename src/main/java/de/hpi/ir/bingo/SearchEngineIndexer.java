package de.hpi.ir.bingo;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.esotericsoftware.kryo.Serializer;
import com.sun.org.apache.regexp.internal.RE;

import de.hpi.ir.bingo.index.TableMerger;
import de.hpi.ir.bingo.index.TableReader;
import de.hpi.ir.bingo.index.TableUtil;
import de.hpi.ir.bingo.index.TableWriter;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchEngineIndexer {
	private final SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

	public void createIndex(String fileName, String directory, String indexName, Serializer<PostingList> serializer) {

		Map<String, PostingList> index = Maps.newHashMap();
		Map<String, PatentData> patents = Maps.newHashMap();

		AtomicInteger i = new AtomicInteger();
		AtomicInteger indexCounter = new AtomicInteger(3);
		Stopwatch stopwatch = Stopwatch.createStarted();

		PatentHandler.parseXml(fileName, (patent) -> {
			Map<String, PostingListItem> docIndex = buildIndexForDocument(patent);
			mergeDocIndexIntoMainIndex(index, docIndex);
			patents.put(Integer.toString(patent.getPatentId()), patent);

			if (i.incrementAndGet()%1000 == 0) {
				long free = Runtime.getRuntime().freeMemory();
				System.out.println("read: " + i + " available: " + free/1024/1024 + "mb" + " passed: " + stopwatch);
				if(free < 1000L*1024L*1024L) {
					writeToDisk(directory, indexName, indexCounter.incrementAndGet(), serializer, index, patents);
					System.out.println("index written to disk!!");
				}
			}
		});

		writeToDisk(directory, indexName, indexCounter.incrementAndGet(), serializer, index, patents);

		// merge!
		try {
			mergeIndices(directory, indexName, indexCounter.get(), PostingList.class, serializer);
			mergeIndices(directory, "patents-temp", indexCounter.get(), PatentData.class, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		calculateImportantTerms(directory, indexName, serializer);
	}

	private <T extends TableMerger.Mergeable<T>> void mergeIndices(String directory, String indexName, int parts, Class<T> clazz, Serializer<T> serializer) throws IOException {
		Queue<Path> files = new ArrayDeque<>();
		for (int j = 1; j <= parts; j++) {
			files.add(Paths.get(directory, indexName + j));
		}
		while (files.size() > 1) {
			Path a = files.poll();
			Path b = files.poll();
			parts++;
			Path dest = Paths.get(directory, indexName + parts);
			TableMerger.merge(dest, a, b, clazz ,serializer);
			Files.delete(a);
			Files.delete(b);
			files.add(dest);
		}
		Files.move(files.poll(), Paths.get(directory, indexName));
	}

	private void writeToDisk(String directory, String indexName, int counter, Serializer<PostingList> serializer, Map<String, PostingList> index, Map<String, PatentData> patents) {
		TableWriter<PostingList> indexWriter = new TableWriter<>(Paths.get(directory, indexName + counter), false, PostingList.class, serializer);
		indexWriter.writeMap(index);
		indexWriter.close();

		TableWriter<PatentData> patentWriter = new TableWriter<>(Paths.get(directory, "patents-temp" + counter), false, PatentData.class, null);
		patentWriter.writeMap(patents);
		patentWriter.close();

		index.clear();
		patents.clear();
		Runtime.getRuntime().gc();
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
		List<Token> claimTokens = tokenizer.tokenizeStopStem(patent.getClaimText());
		int totalSize = titleTokens.size() + abstractTokens.size();

		int pos = 0;
		pos += addToIndex(patent.getPatentId(), pos, totalSize, titleTokens.size(), titleTokens, docIndex);
		patent.setAbstractOffset(pos);
		pos += addToIndex(patent.getPatentId(), pos, totalSize, titleTokens.size(), abstractTokens, docIndex);
		patent.setClaimOffset(pos);
		pos += addToIndex(patent.getPatentId(), pos, totalSize, titleTokens.size(), claimTokens, docIndex);
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
