package de.hpi.ir.bingo;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PatentHandler extends DefaultHandler {

	private Stack<String> parents;
	private StringBuilder currentTitle;
	private StringBuilder currentId;
	private StringBuilder currentAbstract;
	private StringBuilder currentClaim;
	private IntList patentCitations;
	

	private String currentApplType;
	private final Consumer<PatentData> patentComsumer;
	private StringBuilder currentCiteDate;
	private StringBuilder currentCiteCountry;
	private StringBuilder currentCiteId;

	private PatentHandler(Consumer<PatentData> patentComsumer) {
		this.patentComsumer = patentComsumer;
	}

	public static void parseXml(String fileName, Consumer<PatentData> patentComsumer) {
		try {

			XMLReader xr = XMLReaderFactory.createXMLReader();

			PatentHandler handler = new PatentHandler(patentComsumer);
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			xr.setEntityResolver(handler);


			if (fileName.endsWith(".zip")) {
				ZipFile zipFile = new ZipFile(fileName);
				List<? extends ZipEntry> entries = Collections.list(zipFile.entries());
				entries.sort(Comparator.comparing(ZipEntry::getName));
				for (ZipEntry zipEntry : entries) {
					if (zipEntry.getName().endsWith(".xml")) {
						System.out.println("reading: " + zipEntry.getName());
						Reader r = new InputStreamReader(zipFile.getInputStream(zipEntry));
						xr.parse(new InputSource(r));
					}
				}
			} else {
				Reader r = new FileReader(fileName);
				xr.parse(new InputSource(r));
			}
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void startDocument() {
		parents = new Stack<>();
		currentId = new StringBuilder();
		currentTitle = new StringBuilder();
		currentAbstract = new StringBuilder();
		currentClaim = new StringBuilder();
		currentCiteDate = new StringBuilder();
		currentCiteCountry = new StringBuilder();
		currentCiteId = new StringBuilder();
		patentCitations = new IntArrayList();
	}

	@Override
	public void endDocument() {
	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		if (name.equals("us-patent-grant")) {
			currentId.setLength(0);
			currentTitle.setLength(0);
			currentAbstract.setLength(0);
			currentClaim.setLength(0);
			currentApplType = "";
			patentCitations.clear();
		}
		if (name.equals("application-reference")) {
			currentApplType = atts.getValue("appl-type");
		}
		if(name.equals("patcit")){
			currentCiteCountry.setLength(0);
			currentCiteDate.setLength(0);
			currentCiteId.setLength(0);
		}
		parents.push(qName);
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		if (name.equals("us-patent-grant")) {
			if (currentApplType.equals("utility")) {
				int id = Integer.parseInt(currentId.toString());
				String title = currentTitle.toString().replaceAll("\\s+", " "); // remove duplicate whitespace
				String abstractText = currentAbstract.toString().replaceAll("\\s+", " ");
				String claimText = currentClaim.toString().replaceAll("\\s+", " ");
				IntArrayList citations = new IntArrayList(patentCitations);
				patentComsumer.accept(new PatentData(id, title, abstractText, claimText, citations));
			}
		}
		if(name.equals("patcit")){
			if(currentCiteCountry.toString().equals("US") && currentCiteDate.toString().compareTo("20110000")>0){
				try {
					patentCitations.add(Integer.parseInt(currentCiteId.toString()));
				} catch (NumberFormatException e) {
					// not a valid integer
				}

			}
		}
		parents.pop();
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (parents.peek().equals("invention-title")) {
			currentTitle.append(ch, start, length);
		}
		else if (parents.peek().equals("doc-number") && parents.get(parents.size() - 3).equals("publication-reference")) {
			currentId.append(ch, start, length);
		}
		else if (parents.contains("abstract")) {
			currentAbstract.append(ch, start, length);
		}
		else if (parents.contains("claim-text")) {
			currentClaim.append(ch, start, length);
		}
		else if (parents.size()>=3 && parents.get(parents.size()-3).equals("patcit")){
			if(parents.peek().equals("country")){
				currentCiteCountry.append(ch,start,length);
			}
			else if(parents.peek().equals("date")){
				currentCiteDate.append(ch,start,length);
			}
			else if(parents.peek().equals("doc-number")){
				currentCiteId.append(ch,start,length);
			}
		}
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) {
		return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
	}
}
