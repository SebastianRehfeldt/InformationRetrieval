package de.hpi.ir.bingo;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;

import de.hpi.ir.bingo.evaluation.CachedWebfile;
import de.hpi.ir.bingo.evaluation.WebFile;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author: Bingo
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 * <p/>
 * You can run your search engine using this file You can use/change this file during the
 * development of your search engine. Any changes you make here will be ignored for the final test!
 */
public class SearchEngineTest {

	private static final boolean CREATE_INDEX = false;
	private static final boolean COMPRESS = false;
	private static final boolean READ_COMPRESSED = true;

	public static void main(String args[]) throws Exception {

		SearchEngineBingo myEngine = new SearchEngineBingo();

		long start = System.currentTimeMillis();

		if (CREATE_INDEX)
			myEngine.index();
		if (COMPRESS)
			myEngine.compressIndex(); //String directory

		//myEngine.printIndexStats(DIRECTORY);
		//System.exit(0);

		long time = System.currentTimeMillis() - start;

		System.out.print("Indexing Time:\t" + time + "\tms\n");

		// myEngine.loadIndex(String directory)

		if (READ_COMPRESSED)
			myEngine.loadCompressedIndex();
		else
			myEngine.loadIndex();

		CachedWebfile webFile = new CachedWebfile();


		Writer resultWriter = Files.newBufferedWriter(Paths.get("queryresults.txt"), Charsets.UTF_8);
		//List<String> queries = ImmutableList.of("add-on module", "digital signature", "data processing", "\"a scanning\"");
		List<String> queries = ImmutableList.of(
				"responsive applications for android tablet", "cloud computing security issues",
				"cloud NOT smart", "\"3-D miniatures\"", "healthcare AND services");
		int topK = 5;

		for (String query : queries) {
			start = System.currentTimeMillis();

			List<SearchEngineBingo.SearchResult> results = myEngine.searchWithSearchResult(query, topK); //topK, prf
			List<String> patentIds = myEngine.getIds(results);
			time = System.currentTimeMillis() - start;

			System.out.print("Searching Time:\t" + time + "\tms\n");

			List<String> goldStandard = webFile.getGoogleRanking(query);

			resultWriter.write("\"" + query + "\"\n");
			double ndcg = myEngine.computeNdcg(goldStandard, patentIds, topK);
			System.out.printf("NDCG: %.3f\n", ndcg);
			resultWriter.write(String.format("NDCG: %.3f\n", ndcg));
			if (results.isEmpty()) {
				System.out.println("No results found");
			} else {
				for (SearchEngineBingo.SearchResult result : results) {
					double gain = myEngine.computeNdcg(goldStandard, ImmutableList.of(""+result.patentId), 1);
					System.out.println(result + "\nGain:" + gain + "\n");
					resultWriter.write(result + "\nGain:" + gain + "\n\n");
				}
			}

			resultWriter.write("\n");
			System.out.println();
		}
		resultWriter.close();

		//myEngine.writeIndexTerms();
	}

}
