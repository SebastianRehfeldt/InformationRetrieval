package de.hpi.ir.bingo;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;

import de.hpi.ir.bingo.evaluation.CachedWebfile;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

/**
 * @author: Bingo
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 * <p/>
 * You can run your search engine using this file You can use/change this file during the
 * development of your search engine. Any changes you make here will be ignored for the final test!
 */
public class SearchEngineTest {


	public static void main(String args[]) throws Exception {
		//System.in.read();
		SearchEngineBingo myEngine = new SearchEngineBingo();

		long start = System.currentTimeMillis();

		if (Settings.CREATE_INDEX)
			myEngine.index();
		if (Settings.COMPRESS)
			myEngine.compressIndex(); //String directory

//		myEngine.printIndexStats();
//		System.exit(0);

		long time = System.currentTimeMillis() - start;

		System.out.print("Indexing Time:\t" + time + "\tms\n");

		// myEngine.loadIndex(String directory)

		if (Settings.READ_COMPRESSED)
			myEngine.loadCompressedIndex();
		else
			myEngine.loadIndex();

		CachedWebfile webFile = new CachedWebfile();

//		System.in.read();

		boolean PRINT_PR = false;
		if (PRINT_PR) {
			Int2DoubleMap pageRank = myEngine.getPageRanks();
			List<Integer> ids = ImmutableList.of(7861321, 7886437, 8074432, 8074897, 8074994);
			System.out.println("Pageranks");
			for (Integer id : ids) {
				System.out.printf("id: %d, pr: %e\n", id, pageRank.get(id));
			}
			//Comparator<Map.Entry<Integer, Double>> comparing = Comparator.comparing(Map.Entry::getValue);
			//System.out.println(pageRank.entrySet().stream().sorted(comparing.reversed()).limit(10).collect(Collectors.toList()));
		}


		Writer resultWriter = Files.newBufferedWriter(Paths.get("queryresults.txt"), Charsets.UTF_8);
		List<String> queries;
//		queries = ImmutableList.of("add-on module", "digital signature", "data processing", "\"a scanning\"");
//		queries = ImmutableList.of("\"graph editor\"", "\"social trend\"", "fossil hydrocarbons", "physiological AND saline", "tires NOT pressure", "linkTo:8201244");
//		queries = ImmutableList.of("LinkTo:07920906", "LinkTo:07904949", "LinkTo:08078787", "LinkTo:07865308 AND LinkTo:07925708", "LinkTo:07947864 AND LinkTo:07947142", "review guidelines", "on-chip AND OCV NOT asynchronous", "on-chip ocv");
		queries = ImmutableList.of("Marker pen holder", "sodium polyphosphates", "\"ionizing radiation\"",
				"solar coronal holes", "patterns in scale-free networks", "\"nail polish\"", "\"keyboard shortcuts\"",
				"radiographic NOT ventilator", "multi-label AND learning", "LinkTo:07866385", "Marker pen holder #2");
		//queries = ImmutableList.of("perform advanced structures");
		int topK = 10;
		int iterations = 1;

		for (int i = 0; i < iterations; i++)
			for (String query : queries) {
				start = System.currentTimeMillis();
				List<SearchEngineBingo.SearchResult> results = myEngine.searchWithSearchResult(query, topK); //topK, prf
				time = System.currentTimeMillis() - start;

				System.out.print("Searching Time:\t" + time + "\tms\n");

				List<String> patentIds = myEngine.getIds(results);
				List<String> goldStandard = webFile.getGoogleRanking(query.replaceAll("#[0-9]", ""));

				resultWriter.write("[" + query + "]\n");
				double ndcg = myEngine.computeNdcg(goldStandard, patentIds, topK);
				System.out.printf("NDCG: %.3f\n", ndcg);
				resultWriter.write(String.format("NDCG: %.3f\n", ndcg));
				resultWriter.write(String.format("Time: %dms\n", time));
				if (results.isEmpty()) {
					System.out.println("No results found, google results: " + goldStandard);
				} else {
					for (SearchEngineBingo.SearchResult result : results) {
						if (result.snippet.equals("linked")) {
							//System.out.println(result.patentId + " " + result.title);
							resultWriter.write(result.patentId + "\t" + result.title + "\n");
						} else {
							//double gain = myEngine.computeNdcg(goldStandard, ImmutableList.of("" + result.patentId), 1);
							//System.out.println(result + "\nScore: " + result.score + " Gain:" + gain + "\n");
							resultWriter.write(String.format("%d\t%s\n%s\n\n", result.patentId, result.title, result.snippet));
						}
					}
				}

				resultWriter.write("\n");
				System.out.println();
			}
		resultWriter.close();

		//myEngine.writeIndexTerms();
	}

}
