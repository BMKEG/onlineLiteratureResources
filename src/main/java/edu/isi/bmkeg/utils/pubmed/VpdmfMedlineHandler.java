package edu.isi.bmkeg.utils.pubmed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.xml.sax.InputSource;

import com.aliasi.medline.MedlineCitation;
import com.aliasi.medline.MedlineHandler;
import com.aliasi.medline.MedlineParser;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import edu.isi.bmkeg.digitalLibrary.ArticleCitation;
import edu.isi.bmkeg.digitalLibrary.Author;
import edu.isi.bmkeg.digitalLibrary.Journal;

public class VpdmfMedlineHandler implements MedlineHandler {

	private static Logger logger = Logger.getLogger(VpdmfMedlineHandler.class);
	
	private MedlineParser parser;
	
	private Map<String,Journal> jLookup = new HashMap<String, Journal>();
		
	private List<ArticleCitation> list = new ArrayList<ArticleCitation>();
	
	// total count of records in the upload directory
	private long recsInDir;
	
	// total count of records in the current upload file 
	private long recsInFile;

	// total count of records committed to the solr store
	private long recsSubmitted;

	// total count of records available in the solr store 
	private long recsInStore;
	
	public VpdmfMedlineHandler(Map<String, Journal> jLookup) 
			throws Exception {
								
		this.refresh();

		this.jLookup = jLookup;
	
	}
	
	/**
	 * Clean up to manage memory resources.
	 */
	private void refresh() {
		if( this.parser != null) {
			this.parser.setHandler(null);
		}		
		this.parser = new MedlineParser(false);
		this.parser.setHandler(this);		
	}

	public void delete(String arg0) {}	
	
	public void handle(MedlineCitation mlCitation) {

		try {

			this.setRecsInDir(this.getRecsInDir() + 1);
			this.setRecsInFile(this.getRecsInFile() + 1);
			
			ArticleCitation a = this
					.mlCitation2ArticleCitation(mlCitation);
			
			if( a == null ) {
				return;
			}

			this.setRecsSubmitted(this.getRecsSubmitted() + 1);
			this.list.add(a);
			
		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	private ArticleCitation mlCitation2ArticleCitation(MedlineCitation citation)
			throws Exception {

		ArticleCitation article = new ArticleCitation();
		article.setAbstractText(citation.article().abstrct().text());
		List<Author> authors = new ArrayList<Author>();
		for (com.aliasi.medline.Author author : citation.article().authorList()
				.authors()) {
			Author p = new Author();
			if (author.name() != null) {
				p.setInitials(author.name().initials());
				p.setSurname(author.name().lastName());
				authors.add(p);
				p.getPublishedWork().add(article);
			}
		}
		article.setAuthorList(authors);
		
		if( authors.size() == 0 )
			return null;

		article.setPmid(Integer.parseInt(citation.pmid()));

		com.aliasi.medline.Journal j = citation.article().journal();
		com.aliasi.medline.JournalInfo jInfo = citation.journalInfo();

		Journal journal = this.jLookup.get(jInfo.medlineTA());
		if( journal == null ) {
			if( this.jLookup.containsKey( j.isoAbbreviation() )) {
				journal = this.jLookup.get(j.isoAbbreviation());
			} else {
				return null;
			}
		}

		article.setVolume(j.journalIssue().volume());
		if( article.getVolume() == null || article.getVolume().length() == 0 ) {
			article.setVolume("-");
			article.setVolValue(-1);
		}

		article.setIssue(j.journalIssue().issue());
		if( article.getIssue() == null || article.getIssue().length() == 0 ) {
			article.setIssue("-");
		}
	
		journal.getArticle().add(article);

		journal.setNlmId(jInfo.nlmUniqueID());

		article.setTitle(citation.article().articleTitle());
		article.setPages(citation.article().pagination());
		if (j.journalIssue().pubDate().year() != null) {
			if (j.journalIssue().pubDate().year().trim().length() > 0) {
				article.setPubYear(Integer.parseInt(j.journalIssue().pubDate()
						.year()));
			}
		}

		article.setJournal(journal);
		return article;
		
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public List<ArticleCitation> parseMedlineFileToList(File f) throws Exception {

		this.setRecsInFile(0);
		
		if (f.getAbsolutePath().endsWith(".xml")) {

			InputSource inputSource = new InputSource(f.getAbsolutePath());
			System.out.println(inputSource.getPublicId());
			System.out.println(inputSource.getSystemId());
			parser.parse(inputSource);

		} else if (f.getAbsolutePath().endsWith(".gz")) {

			FileInputStream fileIn = null;
			GZIPInputStream gzipIn = null;
			InputStreamReader inReader = null;
			BufferedReader bufReader = null;
			InputSource inSource = null;

			try {

				fileIn = new FileInputStream(f);
				gzipIn = new GZIPInputStream(fileIn);
				inReader = new InputStreamReader(gzipIn, Strings.UTF8);
				bufReader = new BufferedReader(inReader);
				inSource = new InputSource(bufReader);

				parser.parse(inSource);

			} finally {
				Streams.closeReader(bufReader);
				Streams.closeReader(inReader);
				Streams.closeInputStream(gzipIn);
				Streams.closeInputStream(fileIn);
				
				fileIn = null;
				gzipIn = null;
				inReader = null;
				bufReader = null;
				inSource = null;
			}

		} else if (f.getAbsolutePath().endsWith(".bz2")) {

			FileInputStream fileIn = null;
			CBZip2InputStream bzipIn = null;
			InputStreamReader inReader = null;
			BufferedReader bufReader = null;
			InputSource inSource = null;

			try {

				fileIn = new FileInputStream(f);

				// //////////HACK found online to make the CBZip2InputStream
				// read the bz2 file correctly////////////
				fileIn.read();
				fileIn.read();
				// //////////HACK found online to make the CBZip2InputStream
				// read the bz2 file correctly////////////

				bzipIn = new CBZip2InputStream(fileIn);
				inReader = new InputStreamReader(bzipIn, Strings.UTF8);
				bufReader = new BufferedReader(inReader);
				inSource = new InputSource(bufReader);
				inSource.setSystemId(f.toURI().toURL().toString());
				parser.parse(inSource);

			} finally {

				Streams.closeReader(bufReader);
				Streams.closeReader(inReader);
				Streams.closeInputStream(bzipIn);
				Streams.closeInputStream(fileIn);
				
				fileIn = null;
				bzipIn = null;
				inReader = null;
				bufReader = null;
				inSource = null;
				
			}

		} else {

			throw new IllegalArgumentException(
					"file suffix must be either '.xml', '.gz' or '.bz'");

		}
		
		return this.list;

	}
	
	public long getRecsInDir() {
		return recsInDir;
	}

	public void setRecsInDir(long recsInDir) {
		this.recsInDir = recsInDir;
	}

	public long getRecsInFile() {
		return recsInFile;
	}

	public void setRecsInFile(long recsInFile) {
		this.recsInFile = recsInFile;
	}

	public long getRecsSubmitted() {
		return recsSubmitted;
	}

	public void setRecsSubmitted(long recsSubmitted) {
		this.recsSubmitted = recsSubmitted;
	}

	public long getRecsInStore() {
		return recsInStore;
	}

	public void setRecsInStore(long recsInStore) {
		this.recsInStore = recsInStore;
	}
	
}
