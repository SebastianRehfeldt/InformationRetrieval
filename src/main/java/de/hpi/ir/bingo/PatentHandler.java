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
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PatentHandler extends DefaultHandler {

	private Stack<String> parents;
	private StringBuilder currentTitle;
	private StringBuilder currentId;
	private StringBuilder currentAbstract;
	private Consumer<PatentData> patentComsumer;

	public static void parseXml(String fileName, Consumer<PatentData> patentComsumer) {
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();

			PatentHandler handler = new PatentHandler(patentComsumer);
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);

			FileReader r = new FileReader(fileName);
			xr.parse(new InputSource(r));
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private PatentHandler(Consumer<PatentData> patentComsumer) {
		this.patentComsumer = patentComsumer;
	}

	@Override
	public void startDocument() {
		parents = new Stack<>();
		currentId = new StringBuilder();
		currentTitle = new StringBuilder();
		currentAbstract = new StringBuilder();
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
		if (qName.equals("us-patent-grant")) {
			int id = Integer.parseInt(currentId.toString());
			String title = currentTitle.toString().replaceAll("\\s+", " "); // remove duplicate whitespace
			String abstractText = currentAbstract.toString().replaceAll("\\s+", " ");
			patentComsumer.accept(new PatentData(id, title, abstractText));
			currentId.setLength(0);
			currentTitle.setLength(0);
			currentAbstract.setLength(0);
		}
		parents.pop();
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (parents.peek().equals("invention-title")) {
			currentTitle.append(ch, start, length);
		}
		if (parents.peek().equals("doc-number") && parents.get(parents.size() - 3).equals("publication-reference")) {
			currentId.append(ch, start, length);
		}
		if (parents.peek().equals("p") && parents.get(parents.size() - 2).equals("abstract")) {
			currentAbstract.append(ch, start, length);
		}
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) {
		return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
	}
}
