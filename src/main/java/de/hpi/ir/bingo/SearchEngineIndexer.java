package de.hpi.ir.bingo;

import com.google.common.base.Stopwatch;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.esotericsoftware.kryo.Serializer;

import de.hpi.ir.bingo.index.TableMerger;
import de.hpi.ir.bingo.index.TableReader;
import de.hpi.ir.bingo.index.TableUtil;
import de.hpi.ir.bingo.index.TableWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchEngineIndexer {
	private final SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

	private final String directory;

	public SearchEngineIndexer(String directory) {
		this.directory = directory;
	}

	public void createIndex(String fileName, String indexName, Serializer<PostingList> serializer) {

		Map<String, PostingList> index = Maps.newHashMap();
		Map<String, PatentData> patents = Maps.newHashMap();

		AtomicInteger i = new AtomicInteger(0);
		AtomicInteger indexCounter = new AtomicInteger(0);
		Stopwatch stopwatch = Stopwatch.createStarted();

		long totalMemory = Runtime.getRuntime().totalMemory();
		if (Runtime.getRuntime().freeMemory() < 1800*1024*1024) {
			throw new RuntimeException("run at least with -Xms2g");
		}
		PatentHandler.parseXml(fileName, (patent) -> {
			Map<String, PostingListItem> docIndex = buildIndexForDocument(patent);
			mergeDocIntoMainIndex(index, docIndex);
			patents.put(Integer.toString(patent.getPatentId()), patent);

			if (i.incrementAndGet()%1000 == 0) {
				long free = Runtime.getRuntime().freeMemory();
				System.out.println("read: " + i + " available: " + free/1024/1024 + "mb" + " passed: " + stopwatch);
				if(free / (double)totalMemory < 0.25) {
					writeToDisk(indexName, indexCounter.incrementAndGet(), serializer, index, patents);
					System.out.println("index written to disk!!");
				}
			}
		});

		writeToDisk(indexName, indexCounter.incrementAndGet(), serializer, index, patents);

		// merge!
		try {
			mergeIndices(indexName, indexCounter.get(), PostingList.class, serializer);
			mergeIndices("patents-temp", indexCounter.get(), PatentData.class, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		calculateImportantTerms(indexName, serializer);
	}

	private <T extends TableMerger.Mergeable<T>> void mergeIndices(String indexName, int parts, Class<T> clazz, Serializer<T> serializer) throws IOException {
		Queue<Path> files = new ArrayDeque<>();
		for (int j = 1; j <= parts; j++) {
			files.add(Paths.get(directory, indexName + j));
		}
		Queue<Path> nextGen = new ArrayDeque<>();
		while (files.size() > 1) {
			Path a = files.poll();
			Path b = files.poll();
			parts++;
			boolean lastMerge = files.size() == 0 && nextGen.size() == 0;
			Path dest = Paths.get(directory, indexName + (lastMerge ? "" : parts));
			System.out.println("merge: " + a + " + " + b + " to " + dest);
			TableMerger.merge(dest, a, b, lastMerge, clazz , serializer);
			Files.delete(a);
			Files.delete(b);
			nextGen.add(dest);
			if (files.size() == 1) {
				nextGen.add(files.poll());
			}
			if (files.size() == 0) {
				files.addAll(nextGen);
				nextGen.clear();
			}
		}
	}

	private void writeToDisk(String indexName, int counter, Serializer<PostingList> serializer, Map<String, PostingList> index, Map<String, PatentData> patents) {
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

	private void mergeDocIntoMainIndex(Map<String, PostingList> index, Map<String, PostingListItem> docIndex) {
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
	private void calculateImportantTerms(String indexName, Serializer<PostingList> serializer) {
		System.out.println("postprocessing index");
		int totalDocumentCount = TableUtil.getTableIndex(Paths.get(directory, "patents-temp")).getSize();
		TableReader<PostingList> postingReader = new TableReader<>(Paths.get(directory, indexName), PostingList.class, serializer);
		Map<String, Double> idf = Maps.newHashMap();
		Map.Entry<String, PostingList> token = postingReader.readNext();
		while(token != null) {
			double idfValue = Math.log(totalDocumentCount / (double) token.getValue().getDocumentCount());
			idf.put(token.getKey(), idfValue);
			Map.Entry<String, PostingList> next = postingReader.readNext();
			Verify.verify(next == null || token.getKey().compareTo(next.getKey()) < 0);
			token = next;
		}
		postingReader.close();

		TableReader<PatentData> patentReader = new TableReader<>(Paths.get(directory, "patents-temp"), PatentData.class, null);
		TableWriter<PatentData> patentWriter = new TableWriter<>(Paths.get(directory, "patents"), true, PatentData.class, null);
		Map.Entry<String, PatentData> patent = patentReader.readNext();
		while(patent != null) {
			patent.getValue().calculateImportantTerms(idf);
			patentWriter.put(patent.getKey(), patent.getValue());
			Map.Entry<String, PatentData> next = patentReader.readNext();
			Verify.verify(next == null || patent.getKey().compareTo(next.getKey()) < 0);
			patent = next;
		}
		patentReader.close();
		patentWriter.close();
		try {
			Files.delete(Paths.get(directory, "patents-temp"));
			Files.delete(Paths.get(directory, "patents-temp.index"));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
