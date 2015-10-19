package de.hpi.ir.bingo;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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

		Collection<PatentData> patents = PatentHandler.parseXml(fileName);
		for (PatentData patent : patents) {
			System.out.printf("%d: %s\n", patent.getPatentId(), patent.getTitle());
		}
	}
}
