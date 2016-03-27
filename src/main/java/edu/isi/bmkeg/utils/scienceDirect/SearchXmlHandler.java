package edu.isi.bmkeg.utils.scienceDirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


class SearchXmlHandler extends DefaultHandler {

	String currentAttribute = "";
	String currentMatch = "";
	
	private int total;
	private int offset;
	
	String oa = null;
	String doi = null;
	String url = null;
	String title = null;
	String date = null;
	List<String> authors = new ArrayList<String>();

	private List<Map<String,String>> entries = null;
	
	ArrayList<Exception> exceptions = new ArrayList<Exception>();

	public void startDocument() {
		setEntries(new ArrayList<Map<String,String>>());
	}

	public void endDocument() {
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		this.currentMatch += "." + qName;
		this.currentAttribute = attributes.getValue("IdType");
				
	}

	public void endElement(String uri, String localName, String qName) {
		
		if( this.currentMatch.endsWith("entry") ) {
			if( doi != null && url != null ) {

				Map<String, String> map = new HashMap<String, String>();
				map.put("doi", doi);
				map.put("url", url);
				map.put("title", title);
				map.put("date", date);
				map.put("oa", oa);
				String au = "";
				for( String a : authors ) {
					au += a + "; ";
				}
				if( au.length() > 0 )
					au = au.substring(0, au.length()-2);
				map.put("authors", au);
				getEntries().add(map);
				
			}
			
			doi = null;
			url = null;
			title = null;
			date = null;
			oa = null;
			authors = new ArrayList<String>();
			
		}
		
		String c = this.currentMatch;
		this.currentMatch = c.substring(0, c.lastIndexOf("." + qName));
		
	}

	public void characters(char[] ch, int start, int length) {
				
		String value = new String(ch, start, length);

		try {

			if( this.currentMatch.endsWith(".prism:url") ) {
				url = value;
			}

			if( this.currentMatch.endsWith(".dc:identifier") ) {
				doi = value;
			}

			if( this.currentMatch.endsWith(".dc:title") ) {
				title = value;
			}
			
			if( this.currentMatch.endsWith(".prism:coverDisplayDate") ) {
				date = value;
			}
			
			if( this.currentMatch.endsWith("authors.author.surname") ) {
				authors.add( value );
			}
			
			if( this.currentMatch.endsWith("opensearch:totalResults") ) {
				this.setTotal(new Integer( value ));
			}

			if( this.currentMatch.endsWith("opensearch:startIndex") ) {
				this.setOffset(new Integer( value ));
			}

			if( this.currentMatch.endsWith("openaccessArticle") ) {
				oa = value;
			}

			
			
		} catch (Exception e) {

			this.exceptions.add(e);

		}

	}

	public int getTotal() {
		return total;
	}

	private void setTotal(int total) {
		this.total = total;
	}

	public int getOffset() {
		return offset;
	}

	private void setOffset(int offset) {
		this.offset = offset;
	}

	List<Map<String,String>> getEntries() {
		return entries;
	}

	void setEntries(List<Map<String,String>> entries) {
		this.entries = entries;
	}
	
}
