package de.hpi.ir.bingo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Stopwatch;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.hpi.ir.bingo.index.TableMerger;
import de.hpi.ir.bingo.index.TableReader;
import de.hpi.ir.bingo.index.TableUtil;
import de.hpi.ir.bingo.index.TableWriter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;

public final class SearchEngineIndexer {
	private final SearchEngineTokenizer tokenizer = new SearchEngineTokenizer();

	private final String directory;

	public SearchEngineIndexer(String directory) {
		this.directory = directory;
	}

	private static Runnable verbose(Runnable f) {
		return () -> {
			try {
				f.run();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		};
	};

	public void createIndex(String fileName, Serializer<PostingList> serializer) {

		Map<String, PostingList> index = Maps.newHashMap();
		Map<String, PatentData> patents = Maps.newHashMap();
		Int2ObjectMap<IntList> citations = new Int2ObjectOpenHashMap<>();

		AtomicInteger i = new AtomicInteger(0);
		AtomicInteger indexCounter = new AtomicInteger(0);
		Stopwatch stopwatch = Stopwatch.createStarted();

		long totalMemory = Runtime.getRuntime().totalMemory();
		if (totalMemory < 1800 * 1024 * 1024) {
//			throw new RuntimeException("run at least with -Xms2g");
			System.out.printf("you are only indexing with %d MB of heap space!!!\n", totalMemory/(1024*1024));
		}

		int THREADS = 8;
		int QUEUE_SIZE = 3000;
		BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>(QUEUE_SIZE);
		ExecutorService processing = new ThreadPoolExecutor(THREADS, THREADS, 0, TimeUnit.MILLISECONDS, queue);
		BlockingQueue<Runnable> finalizerQueue = new LinkedBlockingDeque<>(QUEUE_SIZE);
		ExecutorService finalizer = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, finalizerQueue);

		PatentHandler.parseXml(fileName, (patent) -> {
			waitForQueue(queue);
			Future<Map<String, PostingListItem>> future = processing.submit(() -> buildIndexForDocument(patent));
			waitForQueue(finalizerQueue);
			finalizer.submit(verbose(() -> {
				Map<String, PostingListItem> docIndex;
				try {
					docIndex = future.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}
				mergeDocIntoMainIndex(index, docIndex);
				patents.put(Integer.toString(patent.getPatentId()), patent);

				IntListIterator iter = patent.getCitations().iterator();
				while (iter.hasNext()) {
					int cited = iter.nextInt();
					IntList list = citations.computeIfAbsent(cited, (a) -> new IntArrayList());
					list.add(patent.getPatentId());
				}
				patent.removeAdditionalData();

				if (i.incrementAndGet() % 1000 == 0) {
					long free = Runtime.getRuntime().freeMemory();
					System.out.println("read: " + i + " available: " + free / 1024 / 1024 + "mb" + " passed: " + stopwatch);
					if (free / (double) totalMemory < 0.25) {
						writeToDisk(indexCounter.incrementAndGet(), false, serializer, index, patents);
						System.out.println("index written to disk!!");
					}
				}
			}));
		});

		processing.shutdown();
		finalizer.shutdown();

		try {
			processing.awaitTermination(1, TimeUnit.DAYS);
			finalizer.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		writeToDisk(indexCounter.incrementAndGet(), false, serializer, index, patents);

		System.out.println("merging: " + stopwatch);
		try {
			mergeIndices(IndexNames.PostingLists, indexCounter.get(), PostingList.class, serializer);
			mergeIndices("patents-temp", indexCounter.get(), PatentData.class, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		citations.values().forEach(list -> list.sort(Comparator.naturalOrder()));
		Output citationOutput = TableUtil.createOutput(Paths.get(directory, IndexNames.Citations));
		TableUtil.getKryo().writeObject(citationOutput, citations);
		citationOutput.close();

		System.out.println("postprocessing index " + stopwatch);
		calculateImportantTerms(serializer);
		System.out.println("done " + stopwatch);
	}

	private void waitForQueue(BlockingQueue<Runnable> queue) {
		try {
			while (queue.remainingCapacity() == 0)
				Thread.sleep(10);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private <T extends TableMerger.Mergeable<T>> void mergeIndices(String indexName, int parts, Class<T> clazz, Serializer<T> serializer) throws IOException {
		List<Path> files = Lists.newArrayList();
		for (int j = 1; j <= parts; j++) {
			files.add(Paths.get(directory, indexName + j));
		}
		Path dest = Paths.get(directory, indexName);
		TableMerger.merge(dest, files, clazz, serializer);

		for (Path file : files) {
			Files.delete(file);
		}
	}

	private void writeToDisk(int counter, boolean createIndexFile, Serializer<PostingList> serializer, Map<String, PostingList> index, Map<String, PatentData> patents) {
		TableWriter<PostingList> indexWriter = new TableWriter<>(Paths.get(directory, IndexNames.PostingLists + counter), createIndexFile, PostingList.class, serializer);
		indexWriter.writeMap(index);
		indexWriter.close();

		TableWriter<PatentData> patentWriter = new TableWriter<>(Paths.get(directory, "patents-temp" + counter), createIndexFile, PatentData.class, null);
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
		List<Token> textTokens = tokenizer.tokenizeStopStem(patent.getText());
		int totalSize = titleTokens.size() + abstractTokens.size();

		int pos = 0;
		pos += addToIndex(patent.getPatentId(), pos, totalSize, titleTokens.size(), abstractTokens.size(), titleTokens, docIndex);
		patent.setAbstractOffset(pos);
		pos += addToIndex(patent.getPatentId(), pos, totalSize, titleTokens.size(), abstractTokens.size(), abstractTokens, docIndex);
		patent.setTextOffset(pos);
		pos += addToIndex(patent.getPatentId(), pos, totalSize, titleTokens.size(), abstractTokens.size(), textTokens, docIndex);
		return docIndex;
	}

	private int addToIndex(int patentId, int offset, int totalSize, int titleWordCount,int abstractWordCount,
						   List<Token> tokens, Map<String, PostingListItem> docIndex) {
		int position = offset;

		for (Token word : tokens) {
			PostingListItem item = docIndex.get(word.text);
			if (item == null) {
				item = new PostingListItem(patentId, totalSize, (short) titleWordCount, (short) abstractWordCount);
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
	private void calculateImportantTerms(Serializer<PostingList> serializer) {
		int totalDocumentCount = TableUtil.getTableIndex(Paths.get(directory, "patents-temp")).getSize();
		TableReader<PostingList> postingReader = new TableReader<>(Paths.get(directory, IndexNames.PostingLists), PostingList.class, serializer);
		Map<String, Double> idf = Maps.newHashMap();
		Map.Entry<String, PostingList> token = postingReader.readNext();
		while (token != null) {
			double idfValue = Math.log(totalDocumentCount / (double) token.getValue().getDocumentCount());
			idf.put(token.getKey(), idfValue);
			Map.Entry<String, PostingList> next = postingReader.readNext();
			Verify.verify(next == null || token.getKey().compareTo(next.getKey()) < 0);
			token = next;
		}
		postingReader.close();

		TableReader<PatentData> patentReader = new TableReader<>(Paths.get(directory, "patents-temp"), PatentData.class, null);
		TableWriter<PatentData> patentWriter = new TableWriter<>(Paths.get(directory, IndexNames.Patents), true, PatentData.class, null);
		Map.Entry<String, PatentData> patent = patentReader.readNext();
		while (patent != null) {
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
