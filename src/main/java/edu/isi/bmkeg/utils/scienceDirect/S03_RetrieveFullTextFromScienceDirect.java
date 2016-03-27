package edu.isi.bmkeg.utils.scienceDirect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
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
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S03_RetrieveFullTextFromScienceDirect {

	public static class Options {

		@Option(name = "-searchFile", usage = "Input File", required = true, metaVar = "INPUT")
		public File input;

		@Option(name = "-columnNumber", usage = "Column Number", required = true, metaVar = "COL")
		public int col;
		
		@Option(name = "-apiKey", usage = "API String", required = true, metaVar = "APIKEY")
		public String apiKey;

		@Option(name = "-outDir", usage = "Output", required = true, metaVar = "OUTPUT")
		public File outDir;
		
	}

	private static Logger logger = Logger
			.getLogger(S03_RetrieveFullTextFromScienceDirect.class);

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

		S03_RetrieveFullTextFromScienceDirect readScienceDirect = new S03_RetrieveFullTextFromScienceDirect();

		if( !options.outDir.exists() )
			options.outDir.mkdirs();
		
		//http://api.elsevier.com/content/article/pii/S0001457515000780?apiKey=97f607fc347bd27badcddcab8decb51f&httpAccept=application/json
		
		BufferedReader in = new BufferedReader(new FileReader(options.input));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			String[] fields = inputLine.split("\\t");
			if( fields.length<options.col ) 
				continue;

			String piiUrl = fields[options.col-1];
			if( !piiUrl.startsWith("http") )
				piiUrl = "http://api.elsevier.com/content/article/pii/" + piiUrl;
				
			
			URL url = new URL(piiUrl + "?apiKey=" + options.apiKey + "&httpAccept=text/xml");
			String doi = fields[options.col-1];
			doi = doi.replaceAll("\\/", "_slash_");
			doi = doi.replaceAll("\\:", "_colon_");
			doi = doi.replaceAll("\\(", "_bra_");
			doi = doi.replaceAll("\\)", "_ket_");

			File f = new File(options.outDir.getPath() + "/" + doi + ".xml"); 
			if( f.exists() )
				continue;
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
			
			BufferedReader in2 = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String inputLine2;
			while ((inputLine2 = in2.readLine()) != null)
				out.println(inputLine2);
			in2.close();
			out.close();

			logger.info("File Downloaded: " + piiUrl);
			
		}
		in.close();
	
	}

}
