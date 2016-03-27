package edu.isi.bmkeg.utils.pubmed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class ESearcher 
{	

	private static Logger logger = Logger.getLogger(ESearcher.class);
	
	private static String JOURNALS = "journals";	
	private static String PUBMED = "pubmed";
	
	private String baseQueryPrefix;
	private String baseQuerySuffix;
		
	private String pubmedId;
	private int maxCount = 0;
	
	private String queryString;
	
	private Date lastQuery;
	
	private List<Integer> ids = new ArrayList<Integer>();
		
	public ESearcher(String queryString) throws Exception {
		baseQueryPrefix = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=" + PUBMED + "&term=";
		baseQuerySuffix = "&retmode=xml";
		this.queryString = queryString.replaceAll("\\s+", "+");
		this.executeESearch(0, 1);
	}
	
	public List<Integer> executeESearch(int startOfPage, int pageCapacity ) throws Exception {
				
		logger.info("COUNT: " + (startOfPage) + ", QUERY: " + 
				baseQueryPrefix + this.queryString + baseQuerySuffix);
		
		URL url = new URL(baseQueryPrefix + this.queryString + baseQuerySuffix + 
				"&retStart=" + startOfPage + "&retMax=" + pageCapacity );		
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        InputSource is = new InputSource(in);
        EsearchHandler handler = new EsearchHandler();
        parser.parse(is, handler);
        
        this.maxCount = handler.getMaxCount();
        		
		return handler.getIds();

	}

	public int getMaxCount() {
		return maxCount;
	}

}
