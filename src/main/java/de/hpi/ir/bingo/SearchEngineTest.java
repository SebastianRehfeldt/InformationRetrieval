package de.hpi.ir.bingo;

import java.util.ArrayList;

/**
 * @author: Bingo
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 *
 * You can run your search engine using this file You can use/change this file during the
 * development of your search engine. Any changes you make here will be ignored for the final test!
 */
public class SearchEngineTest {

	private static final boolean COMPRESS = false;

	public static void main(String args[]) throws Exception {

		SearchEngineBingo myEngine = new SearchEngineBingo();

		long start = System.currentTimeMillis();

		if(COMPRESS)
			myEngine.compressIndex(""); //String directory
		else
			myEngine.index("");

		long time = System.currentTimeMillis() - start;

		System.out.print("Indexing Time:\t" + time + "\tms\n");

		// myEngine.loadIndex(String directory)

		if (COMPRESS)
			myEngine.loadCompressedIndex("");
		else
			myEngine.loadIndex("");

		String query = "write";

		ArrayList<String> results = new ArrayList<>();

		start = System.currentTimeMillis();

		results = myEngine.search(query, 0, 0); //topK, prf

		time = System.currentTimeMillis() - start;

		System.out.print("Searching Time:\t" + time + "\tms\n");

		System.out.println(results);
	}

}
