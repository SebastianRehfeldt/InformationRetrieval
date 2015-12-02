package de.hpi.ir.bingo;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;

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


		Writer resultWriter = Files.newBufferedWriter(Paths.get("queryresults.txt"), Charsets.UTF_8);
		List<String> queries = ImmutableList.of("access control #2", "computers #2" ,"data processing #2", "web servers #2", "vulnerability information #2", "computer-readable media #2");
		//List<String> queries = ImmutableList.of("rootki* OR \"mobile devic*\"");

		for (String query : queries) {
			start = System.currentTimeMillis();

			List<String> results = myEngine.search(query, 10, 2); //topK, prf

			time = System.currentTimeMillis() - start;

			System.out.print("Searching Time:\t" + time + "\tms\n");

			resultWriter.write("\"" + query + "\"\n");
			if (results.isEmpty()) {
				System.out.println("No results found");
			} else {
				for (String result : results) {
					System.out.println(result);
					resultWriter.write(result + "\n");
				}
			}
			resultWriter.write("\n");
		}
		resultWriter.close();

		//myEngine.writeIndexTerms();
	}

}
