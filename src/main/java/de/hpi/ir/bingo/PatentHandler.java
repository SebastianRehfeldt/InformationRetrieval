package de.hpi.ir.bingo;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class PatentHandler extends DefaultHandler {

	private Stack<String> parents;
	private List<PatentData> patents;
	private StringBuilder currentTitle;
	private StringBuilder currentId;

	public static Collection<PatentData> parseXml(String fileName) {
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();

			PatentHandler handler = new PatentHandler();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);

			FileReader r = new FileReader(fileName);
			xr.parse(new InputSource(r));

			return handler.getPatents();

		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private PatentHandler() {
	}


	public Collection<PatentData> getPatents() {
		return patents;
	}

	@Override
	public void startDocument() {
		parents = new Stack<>();
		patents = new ArrayList<>();
		currentId = new StringBuilder();
		currentTitle = new StringBuilder();
	}

	@Override
	public void endDocument() {
	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		parents.push(qName);
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		if(qName.equals("us-patent-grant")) {
			int id = Integer.parseInt(currentId.toString());
			String title = currentTitle.toString().replaceAll("\\s+", " "); // remove duplicate whitespace
			patents.add(new PatentData(id, title));
			currentId.setLength(0);
			currentTitle.setLength(0);
		}
		parents.pop();
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (parents.peek().equals("invention-title")) {
			currentTitle.append(ch, start, length);
		}
		if (parents.peek().equals("doc-number")) {
			if (parents.get(parents.size()-3).equals("publication-reference")) {
				currentId.append(ch, start, length);
			}
		}
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) {
		return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
	}
}
