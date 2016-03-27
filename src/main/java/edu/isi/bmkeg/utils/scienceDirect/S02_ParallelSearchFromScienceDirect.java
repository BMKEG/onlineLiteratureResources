package edu.isi.bmkeg.utils.scienceDirect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Script to query Science Direct. Must have a valid API key. This script searches 
 * ScienceDirect for a given query and returns a list of URLs for download. 
 * This uses a multithreaded approach. 
 * 
 * @author Gully
 * 
 */
public class S02_ParallelSearchFromScienceDirect {

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

		@Option(name = "-tryAgain", usage = "Keep Trying if there are exceptions", required = false, metaVar = "OUTPUT")
		public Boolean tryAgain = false;
		
	}

	static String SEARCH_URL = "http://api.elsevier.com/content/search/scidir?";

	private static Logger logger = Logger
			.getLogger(S02_ParallelSearchFromScienceDirect.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();

		CmdLineParser parser = new CmdLineParser(options);
		Set<ElsevierSearchWorker> workers = new HashSet<ElsevierSearchWorker>();
				
		int year = 2015;

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

		S02_ParallelSearchFromScienceDirect readScienceDirect = new S02_ParallelSearchFromScienceDirect();

		SearchXmlHandler h1 = new SearchXmlHandler();

		String urlString = SEARCH_URL + "apiKey=" + options.apiKey + "&query="
				+ options.query 
				+ "&content=" + options.content
				+ "&subscribed=true&sort=coverDate"
				+ "&httpAccept=application/xml&count=200";

		if (options.subj != null) {
			urlString += "&subj=" + options.subj;
		}

		if (options.outFile.exists())
			options.outFile.delete();		

		List<Map<String, String>> entries = new ArrayList<Map<String,String>>();
		while( year > 1970 ) {
			
			URL url = new URL(urlString + "&date=" + year );
			
			ElsevierSearchWorker esw = new ElsevierSearchWorker(url, entries);
			esw.execute();
			
			workers.add(esw);
			
			year--;
			
		}

		URL url = new URL(urlString + "&date=1900-1969");
		ElsevierSearchWorker esw2 = new ElsevierSearchWorker(url, entries);
		esw2.execute();		
		workers.add(esw2);
		
		// Wait for the workers to complete
		Boolean workersAreNotDone = true;
		WAITING: while( workersAreNotDone ) {
			for(ElsevierSearchWorker esw : workers) {
				if( !esw.isDone() ) {
					Thread.sleep(1000);
					continue WAITING;
				}
			}
			workersAreNotDone = false;
		}
		
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
