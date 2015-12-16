package de.hpi.ir.bingo;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.hpi.ir.bingo.evaluation.WebFile;

/**
 * @author: Bingo
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 * <p/>
 * You can run your search engine using this file You can use/change this file during the
 * development of your search engine. Any changes you make here will be ignored for the final test!
 */
public class SearchEngineTest {

	private static final boolean CREATE_INDEX = true;
	private static final boolean COMPRESS = true;
	private static final boolean READ_COMPRESSED = true;

	public static void main(String args[]) throws Exception {

		SearchEngineBingo myEngine = new SearchEngineBingo();

		long start = System.currentTimeMillis();

		if (CREATE_INDEX)
			myEngine.index("");
		if (COMPRESS)
			myEngine.compressIndex(""); //String directory

		long time = System.currentTimeMillis() - start;

		System.out.print("Indexing Time:\t" + time + "\tms\n");

		// myEngine.loadIndex(String directory)

		if (READ_COMPRESSED)
			myEngine.loadCompressedIndex("");
		else
			myEngine.loadIndex("");

		WebFile webFile = new WebFile();


		Writer resultWriter = Files.newBufferedWriter(Paths.get("queryresults.txt"), Charsets.UTF_8);
		List<String> queries = ImmutableList.of("add-on module", "digital signature", "data processing", "\"a scanning\"");
		//List<String> queries = ImmutableList.of("rootki* OR \"mobile devic*\"");
		int topK = 5;

		for (String query : queries) {
			start = System.currentTimeMillis();

			List<SearchEngineBingo.SearchResult> results = myEngine.searchWithSearchResult(query, topK); //topK, prf
			List<String> titles = myEngine.getTitles(results);

			List<String> goldStandard = webFile.getGoogleRanking(query);


			time = System.currentTimeMillis() - start;

			System.out.print("Searching Time:\t" + time + "\tms\n");

			resultWriter.write("\"" + query + "\"\n");
			double ndcg = myEngine.computeNdcg(goldStandard, titles, topK);
			resultWriter.write("NDCG: " + ndcg + "\n");
			if (results.isEmpty()) {
				System.out.println("No results found");
			} else {
				for (SearchEngineBingo.SearchResult result : results) {
					double gain = myEngine.computeNdcg(goldStandard, ImmutableList.of(result.title), 1);
					System.out.println(result + "\nGain:" + gain + "\n");
					resultWriter.write(result + "\nGain:" + gain + "\n\n");
				}
			}

			resultWriter.write("\n");
		}
		resultWriter.close();

		//myEngine.writeIndexTerms();
	}

}
