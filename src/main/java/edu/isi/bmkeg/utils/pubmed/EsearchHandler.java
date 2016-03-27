package edu.isi.bmkeg.utils.pubmed;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class EsearchHandler extends DefaultHandler {

	String currentMatch = "";
	String currentAttribute = "";

	boolean error = false;
	private List<Integer> ids = null;
	private String idString = "";
	
	private String maxCountString = "";
	private int maxCount = 0;

	public void startDocument() {
		ids = new ArrayList<Integer>();
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attribute) {
		this.currentMatch += "." + qName;
	}

	public void endElement(String uri, String localName, String qName) {
		String c = this.currentMatch;
		this.currentMatch = c.substring(0, c.lastIndexOf("." + qName));
		
		if (c.equals(".eSearchResult.IdList.Id")) {
			ids.add(new Integer(idString));
			idString = "";
		}
		
		if( this.maxCountString.length() > 0 && this.maxCount == 0 )
			this.maxCount = (new Integer(this.maxCountString)).intValue();
	}
	
	public void characters(char[] ch, int start, int length) {
		String value = new String(ch, start, length);

		if (currentMatch.equals(".eSearchResult.IdList.Id")) {
			this.idString += value;
		} else if (currentMatch.endsWith(".Count")) {
			this.maxCountString += value;
		}

	}

	public List<Integer> getIds() {
		return ids;
	}

	public int getMaxCount() {
		return maxCount;
	}

}
