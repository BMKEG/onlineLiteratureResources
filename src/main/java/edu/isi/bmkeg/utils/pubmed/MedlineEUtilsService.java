package edu.isi.bmkeg.utils.pubmed;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MedlineEUtilsService {

	private String base = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	
	private String service = "";
	public static String EFETCH = "efetch.fcgi";
	public static String ESEARCH = "esearch.fcgi";
	
	private String database = ";";
	public static String PUBMED = "pubmed";
	
	private String email = "gully@usc.edu";
	private String tool = "bioscholar";
	private int retstart = 0;
	private int retmax = 10000;

	private String term = "";
	
	private DefaultHandler handler = null;
	
	public MedlineEUtilsService(String service, String database) throws Exception {
		this.service = service;
		this.database = database;
		
		if(this.service.equals(EFETCH)) 
			this.handler = new EfetchHandler();
		else if(this.service.equals(ESEARCH)) 
			this.handler = new EsearchHandler();
		else throw new Exception("Must set service to EFETCH or ESEARCH");
		
		
	}

	public void exec(String term) throws Exception {
		
		if( !this.service.equals(EFETCH) && !(this.service.equals(ESEARCH))) {
			throw new Exception("handler not set to EUtils Handler type");
		}
		
		this.setTerm(term);

		String req = this.base + this.service + 
				"?db=" + this.database + 
				"&email=" + email +
				"&tool=" + tool +
				"&retmode=xml" + 
				"&retstart=" + this.getRetstart() + 
				"&retmax=" + this.getRetmax() + 
				"&" + this.getTerm(); 
		
		URL url = new URL(req);
		InputStream is = url.openConnection().getInputStream();
		
		// Always wait 3 seconds after running EUtils
        Thread.sleep(3000);
		
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setValidating(false);
		SAXParser parser = saxFactory.newSAXParser();

		parser.parse(is, this.handler);		
		
	}

	public void setRetstart(int retstart) {
		this.retstart = retstart;
	}

	public int getRetstart() {
		return retstart;
	}

	public void setRetmax(int retmax) {
		this.retmax = retmax;
	}

	public int getRetmax() {
		return retmax;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getTerm() {
		return term;
	}

	public DefaultHandler getHandler() {
		return handler;
	}

}
