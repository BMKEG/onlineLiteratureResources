package edu.isi.bmkeg.utils.scienceDirect;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;

public class UrlDownloadWorker extends SwingWorker<Integer, String> {

	private static void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted");
		}
	}

	URL url;
	File file;
	
	/**
	 * Creates an instance of the worker
	 * 
	 * @param url
	 *            The URL to search and process.
	 * @param file
	 *            The output file
	 */
	public UrlDownloadWorker(URL url, File file) {
		this.url = url;
		this.file = file;
	}

	@Override
	protected Integer doInBackground() throws Exception {

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		
		BufferedReader in2 = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String inputLine2;
		while ((inputLine2 = in2.readLine()) != null)
			out.println(inputLine2);
		in2.close();
		out.close();

		System.out.println("File Downloaded: " + url);

		return 1;
	}
	
}
