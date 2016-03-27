package edu.isi.bmkeg.utils.scienceDirect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.xml.sax.InputSource;

/**
 * Script to query Science Direct. Must have a valid API key. This script searches 
 * ScienceDirect for a given query and returns a list of URLs for download.
 * 
 * @author Gully
 * 
 */
public class S01_SearchScienceDirect {

	public static class Options {

		@Option(name = "-query", usage = "Query String", required = true, metaVar = "QUERY")
		public String query;

		@Option(name = "-apiKey", usage = "API String", required = true, metaVar = "APIKEY")
		public String apiKey;

		@Option(name = "-content", usage = "Content", required = true, metaVar = "CONTENT")
		public String content;

		@Option(name = "-subj", usage = "Subject", required = false, metaVar = "SUBJECT")
		public String subj;

		@Option(name = "-outFile", usage = "Output", required = true, metaVar = "OUTPUT")
		public File outFile;

		@Option(name = "-tryAgain", usage = "Keep Trying if there are exceptions", required = false)
		public Boolean tryAgain = false;
		
	}

	static String SEARCH_URL = "http://api.elsevier.com/content/search/scidir?";

	private static Logger logger = Logger
			.getLogger(S01_SearchScienceDirect.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();

		CmdLineParser parser = new CmdLineParser(options);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy");
		Date date = new Date();
		Integer year = new Integer(dateFormat.format(date));

		try {

			parser.parseArgument(args);

		} catch (CmdLineException e) {

			System.err.println(e.getMessage());
			System.err.print("Arguments: ");
			parser.printSingleLineUsage(System.err);
			System.err.println("\n\n Options: \n");
			parser.printUsage(System.err);
			System.exit(-1);

		}

		S01_SearchScienceDirect readScienceDirect = new S01_SearchScienceDirect();

		SearchXmlHandler h1 = new SearchXmlHandler();

		String urlString = SEARCH_URL + "apiKey=" + options.apiKey + "&query="
				+ options.query 
				+ "&content=" + options.content
				+ "&subscribed=true&sort=coverDate"
				+ "&httpAccept=application/xml&count=200";

		if (options.subj != null) {
			urlString += "&subj=" + options.subj;
		}

		List<Map<String, String>> entries = new ArrayList<Map<String,String>>();
		while( year > 1970 ) {
			
			URL url = new URL(urlString + "&date=" + year );
	
			if (options.outFile.exists())
				options.outFile.delete();		
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
	
			// String inputLine;
			// while ((inputLine = in.readLine()) != null)
			// System.out.println(inputLine);
			// in.close();
	
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			InputSource is = new InputSource(in);
			saxParser.parse(is, h1);
	
			int total = h1.getTotal();
			int pos = h1.getOffset() + h1.getEntries().size();
			entries.addAll(h1.getEntries());
			
			logger.info(year + " total number of docs = " + total);
	
			logger.info(year + " number of entries downloaded = " + h1.getEntries().size());
	
			try {
				boolean dataChanged = true;
				while (pos < total && dataChanged) {
		
					URL url2 = new URL(url.toString() + "&start=" + pos);
					logger.info(url2.toString());
		
					try {
		
						in = new BufferedReader(
								new InputStreamReader(url2.openStream()));
		
						h1 = new SearchXmlHandler();
						is = new InputSource(in);
						saxParser.parse(is, h1);
		
						if( h1.getEntries().size() == 0 )
							dataChanged = false;
						
						pos = h1.getOffset() + h1.getEntries().size();
						entries.addAll(h1.getEntries());
		
						logger.info(year + " number of entries downloaded = " + pos);
						logger.info("Total number of entries downloaded = " + entries.size());
		
					} catch (Exception e2) {
		
						e2.printStackTrace();
						
						if( !options.tryAgain ) {
							throw e2;
						} else {
							// try again in 10 seconds.
							Thread.sleep(10000);
						}
						
					}
					
				} 
	
			} catch(Exception e) {
				// do nothing. Just go on to the next year
			}
			
			year--;
			
		}
		
		//
		// Finally, add all references that occurred before 1970.
		//
		URL url = new URL(urlString + "&date=1900-1969");
		
		if (options.outFile.exists())
			options.outFile.delete();		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));

		// String inputLine;
		// while ((inputLine = in.readLine()) != null)
		// System.out.println(inputLine);
		// in.close();

		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		InputSource is = new InputSource(in);
		saxParser.parse(is, h1);

		entries.addAll(h1.getEntries());
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				options.outFile, true)));
		out.println(urlString);
		String s = "authors\ttitle\tdate\topenAccess\tdoi\turl";
		out.println(s);

		for (Map<String, String> a : entries) {
			s = a.get("authors") + "\t" + a.get("title") + "\t" + a.get("date")
					+ "\t" + a.get("oa") + "\t" + a.get("doi") + "\t"
					+ a.get("url") + "\t";
			out.println(s);
		}
		out.close();

		logger.info("Total number of references loaded: " + entries.size());
		logger.info("Data saved to " + options.outFile.getPath());

	}

}
