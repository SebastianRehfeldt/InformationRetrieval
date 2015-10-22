package de.hpi.ir.bingo;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.google.common.collect.Lists;


/**
 * @author: Bingo
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 *
 * This is your file! implement your search engine here!
 *
 * Describe your search engine briefly: - multi-threaded? - stemming? - stopword removal? - index
 * algorithm? - etc.
 *
 * Keep in mind to include your implementation decisions also in the pdf file of each assignment
 */
public class SearchEngineBingo extends SearchEngine { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName

	public SearchEngineBingo() { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
		// This should stay as is! Don't add anything here!
		super();
	}

	@Override
	void index(String directory) {

	}

	private List<String> tokenizeStopStem(Reader reader) {
		Analyzer analyzer = new StandardAnalyzer();
		try {
			TokenStream stream = analyzer.tokenStream("", reader);
			stream = new EnglishMinimalStemFilter(stream);
			stream.reset();
			//OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);
			List<String> result = Lists.newArrayList();
			while (stream.incrementToken()) {
				String token = stream.getAttribute(CharTermAttribute.class).toString();
				//OffsetAttribute attribute = stream.getAttribute(OffsetAttribute.class);
				result.add(token);
				// TODO get position, return objects
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	boolean loadIndex(String directory) {
		return false;
	}

	@Override
	void compressIndex(String directory) {
	}

	@Override
	boolean loadCompressedIndex(String directory) {
		return false;
	}

	@Override
	ArrayList<String> search(String query, int topK, int prf) {
		return null;
	}

	void printPatentTitles() {
		String fileName = "res/testData.xml";

		PatentHandler.parseXml(fileName, (patent) -> {
			System.out.printf("%d: %s\n", patent.getPatentId(), patent.getTitle());
			System.out.println(tokenizeStopStem(new StringReader(patent.getAbstractText())));
			// TODO build index
		});
	}
}
