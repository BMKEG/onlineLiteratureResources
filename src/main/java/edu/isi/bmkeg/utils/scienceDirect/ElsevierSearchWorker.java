package edu.isi.bmkeg.utils.scienceDirect;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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


public class ElsevierSearchWorker extends SwingWorker<Integer, String> {

	private static void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted");
		}
	}

	SearchXmlHandler h = new SearchXmlHandler();
	List<Map<String, String>> entries = new ArrayList<Map<String, String>>();

	/**
	 * The URL for the search.
	 */
	private URL url;

	/** The target list of entries to be filled in. */
	private List<Map<String, String>> targetEntries;

	/**
	 * Creates an instance of the worker
	 * 
	 * @param url
	 *            The URL to search and process.
	 * @param entries
	 *            The entries derived from the search
	 */
	public ElsevierSearchWorker(URL url, List<Map<String, String>> entries) {
		this.url = url;
		this.targetEntries = entries;
	}

	@Override
	protected Integer doInBackground() throws Exception {

		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));

		h = new SearchXmlHandler();

		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		InputSource is = new InputSource(in);
		saxParser.parse(is, this.h);

		int total = this.h.getTotal();
		int pos = this.h.getOffset() + this.h.getEntries().size();
		entries.addAll(h.getEntries());

		try {
			while (pos < total) {

				URL url2 = new URL(url.toString() + "&start=" + pos);

				in = new BufferedReader(
						new InputStreamReader(url2.openStream()));

				h = new SearchXmlHandler();
				is = new InputSource(in);
				saxParser.parse(is, h);

				pos = h.getOffset() + h.getEntries().size();
				entries.addAll(h.getEntries());

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Return the number of matches found
		return pos;
	}

	@Override
	protected void done() {
		this.targetEntries.addAll( this.entries );
	}
}
