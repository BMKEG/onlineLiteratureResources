package edu.isi.bmkeg.utils.scienceDirect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import bioc.BioCAnnotation;
import bioc.BioCLocation;

public class RetrieveXmlHandler extends DefaultHandler {

	boolean error = false;
	boolean on = false;

	private File xmlFile;
	private List<String> fields;
	private List<String> newlineFields;
	private List<BioCAnnotation> tags;

	BioCAnnotation currentAnnotation;
	String currentAttribute = "";
	String currentMatch = "";
	
	int pos = 0;
	String content = "";
	
	Pattern whiteReplace = Pattern.compile("\\s+");
			
	ArrayList<Exception> exceptions = new ArrayList<Exception>();

	public RetrieveXmlHandler(List<String> fields, List<String> newlineFields) {
		this.fields = fields;
		this.newlineFields = newlineFields;
	}

	public void setXmlFile(File xmlFile) {
		this.xmlFile = xmlFile;
	}
	
	public void startDocument() {
		tags = new ArrayList<BioCAnnotation>();
		pos = 0;
		content = "";
	}

	public void endDocument() {
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		this.currentMatch += "." + qName;
		this.currentAttribute = attributes.getValue("IdType");

		if(qName.equals("Results"))
			on = true;

		if(qName.equals("Discussion"))
			on = false;
		
		if( fields.contains(qName) && on) {
			BioCAnnotation a = new BioCAnnotation();
			a.setID(this.xmlFile.getName());
			a.putInfon("type", qName);
			a.putInfon("file", this.xmlFile.getPath());			
			BioCLocation l = new BioCLocation();
			l.setOffset(pos);
			a.setLocation(l);
			currentAnnotation = a;
			tags.add(a);
		}
		
	}

	public void endElement(String uri, String localName, String qName) {

		if( fields.contains(qName) && on ) {
			BioCLocation l = this.currentAnnotation.getLocations().get(0);
			l.setLength(pos - l.getOffset());
			String t = this.content.substring(
					l.getOffset(), 
					l.getOffset() + l.getLength() );
			
			if( t.startsWith(" ") ) {
				l.setOffset(l.getOffset() + 1);
				l.setLength(l.getLength() - 1);
				t = t.substring(1,t.length());
			}
			
			this.currentAnnotation.setText(t);
		}
		
		if( newlineFields.contains(qName) && on ) {
			content += "\n";
			pos++;
		}
		
		String c = this.currentMatch;
		this.currentMatch = c.substring(0, c.lastIndexOf("." + qName));

	}

	public void characters(char[] ch, int start, int length) {
		
		if(!on)
			return;
		
		String value = new String(ch, start, length);

		try {

			value = value.replaceAll("\\s+", " ");
			
			pos += value.length();
			content += value;
			
		} catch (Exception e) {

			this.exceptions.add(e);

		}

	}
	
	public List<BioCAnnotation> getAnnotations() {
		return this.tags;
	}

	public String getContent() {
		return this.content;
	}

	
}
