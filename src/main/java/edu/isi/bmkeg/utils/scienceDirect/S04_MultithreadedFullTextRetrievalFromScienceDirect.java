package edu.isi.bmkeg.utils.scienceDirect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S04_MultithreadedFullTextRetrievalFromScienceDirect {

	public static class Options {

		@Option(name = "-searchFile", usage = "Input File", required = true, metaVar = "INPUT")
		public File input;

		@Option(name = "-apiKey", usage = "API String", required = true, metaVar = "APIKEY")
		public String apiKey;

		@Option(name = "-columnNumber", usage = "Column Number", required = true, metaVar = "COL")
		public int col;
		
		@Option(name = "-outDir", usage = "Output", required = true, metaVar = "OUTPUT")
		public File outDir;

		@Option(name = "-activePoolSize", usage = "Number of concurrent threads active", required = true, metaVar = "OUTPUT")
		public int activePoolSize;
		
		@Option(name = "-httpAccept", usage = "Type of format requested", required = true, metaVar = "TYPE")
		public String httpAccept;
		
	}

	private static Logger logger = Logger
			.getLogger(S04_MultithreadedFullTextRetrievalFromScienceDirect.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();

		CmdLineParser parser = new CmdLineParser(options);
		
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

		S04_MultithreadedFullTextRetrievalFromScienceDirect readScienceDirect = new S04_MultithreadedFullTextRetrievalFromScienceDirect();

		if( !options.outDir.exists() )
			options.outDir.mkdirs();
				
		List<UrlDownloadWorker> workers = new ArrayList<UrlDownloadWorker>();

		BufferedReader in = new BufferedReader(new FileReader(options.input));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			String[] fields = inputLine.split("\\t");
			if( fields.length<options.col ) 
				continue;

			String piiUrl = fields[options.col-1];
			if( !piiUrl.startsWith("http") )
				piiUrl = "http://api.elsevier.com/content/article/pii/" + piiUrl;			
			
			URL url = new URL(piiUrl
					+ "?apiKey=" + options.apiKey 
					+ "&httpAccept=" + options.httpAccept);
			String doi = fields[options.col-1];
			doi = doi.replaceAll("\\/", "_slash_");
			doi = doi.replaceAll("\\:", "_colon_");

			File f = new File(options.outDir.getPath() + "/" + doi + ".xml"); 
			if( f.exists() )
				continue;

			UrlDownloadWorker w = new UrlDownloadWorker(url, f);
			workers.add(w);
			
		}
		in.close();
	
		Set<UrlDownloadWorker> activeWorkers = new HashSet<UrlDownloadWorker>();
		Set<UrlDownloadWorker> completeWorkers = new HashSet<UrlDownloadWorker>();
		
		while( completeWorkers.size() < workers.size() ) {
			for( UrlDownloadWorker w : workers ) {
				if( activeWorkers.size() < options.activePoolSize ) {
					activeWorkers.add(w);
					w.execute();
				}
				if( w.isDone() ) {
					activeWorkers.remove(w);
					completeWorkers.add(w);
				} 
			}
	
		}
		
		
	}

}
