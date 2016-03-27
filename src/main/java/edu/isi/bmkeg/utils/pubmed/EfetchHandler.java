package edu.isi.bmkeg.utils.pubmed;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import edu.isi.bmkeg.digitalLibrary.ArticleCitation;
import edu.isi.bmkeg.digitalLibrary.Author;
import edu.isi.bmkeg.digitalLibrary.ID;
import edu.isi.bmkeg.digitalLibrary.Journal;

class EfetchHandler extends DefaultHandler {

	ArticleCitation article;
	Author person;
	Journal journal;
	ID id;

	boolean error = false;

	private ArrayList<ArticleCitation> articles = new ArrayList<ArticleCitation>();
	private Author nobody = new Author();
	int authorCount = 0;
	int urlCount = 0;
	int idCount = 0;
	int keyCount = 0;
	String currentMatch = "";
	String currentAttribute = "";
	int onePercent;

	int globalPosition = 0;
	int lastPosition = 0;

	ArrayList<Exception> exceptions = new ArrayList<Exception>();

	public void startDocument() {
		articles = new ArrayList<ArticleCitation>();
		nobody.setSurname("-");
		nobody.setInitials("-");
		nobody.setEmail("-");
	}

	public void endDocument() {
		for (ArticleCitation a : this.articles) {
			if(a.getAuthorList() != null && a.getAuthorList().size() == 0 ) {
				a.getAuthorList().add(nobody);
			}
		}
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		this.currentMatch += "." + qName;
		this.currentAttribute = attributes.getValue("IdType");

		if (currentMatch.endsWith(".PubmedArticle")) {
			article = new ArticleCitation();
			articles.add(article);
			this.authorCount = 0;
			this.idCount = 0;
			this.keyCount = 0;
			this.urlCount = 0;
		}
		//
		// Parse the author information
		//
		else if (currentMatch.endsWith(".Author") && 
				article != null) {

			try {
				person = new Author();
				article.getAuthorList().add(person);
			} catch (Exception e) {
				this.exceptions.add(e);
			}
		}
		else if( currentMatch.endsWith("ArticleId") && 
				!currentAttribute.equals("pubmed") && 
				article != null) {
		  
		  id = new ID();
		  id.setIdType(this.currentAttribute);
		  article.getIds().add(id);
		  
		}
		
	}

	public void endElement(String uri, String localName, String qName) {
		String c = this.currentMatch;
		this.currentMatch = c.substring(0, c.lastIndexOf("." + qName));
	}

	public void characters(char[] ch, int start, int length) {
		String value = new String(ch, start, length);

		this.lastPosition = start;

		try {

			//
			// Parse the pubmed information
			//
			if (currentMatch.endsWith(".LastName")) {
				if( person.getSurname() != null )
					person.setSurname(person.getSurname() + " " + value);
				else 
					person.setSurname(value);
			} 
			else if (currentMatch.endsWith(".Initials")) {
				if( person.getInitials() != null )
					person.setInitials(value + " " + person.getInitials() );
				else 
					person.setInitials(value);
			}
			else if (currentMatch.endsWith(".CollectiveName")) {
				if( person.getSurname() != null )
					person.setSurname(value + " " + person.getSurname() );
				else 
					person.setSurname(value);
			}	
			//
			// Title information
			//
			else if (currentMatch.endsWith(".ArticleTitle")) {
				if( article.getTitle() != null )
					article.setTitle(article.getTitle() + value);
				else 
					article.setTitle(value);
			}
			//
			// Date information
			//
			else if (currentMatch.endsWith(".PubDate.Year")) {
				article.setPubYear(new Integer(value).intValue());
			}
			//
			// Date information
			//
			else if (currentMatch.endsWith(".PubDate.Year")) {
				article.setPubYear(new Integer(value).intValue());
			}
			//
			// Page information
			//
			else if (currentMatch.endsWith(".MedlinePgn")) {
				article.setPages(value);
			}
			//
			// Volume does not change
			else if (currentMatch.endsWith(".Volume")) {
				article.setVolume(value);
				Pattern p = Pattern.compile("(\\d+)");
				Matcher m = p.matcher(value);
				if( m.find() ) {
					Integer v = new Integer(m.group(1));
					article.setVolValue(v);
				}
			}
			//
			// Issue does not change
			else if (currentMatch.endsWith(".Issue")) {
				article.setIssue(value);
			}
			//
			// Source (journal) doesn't change but must force a lookup in
			// the
			// relevent Lookup FormControl
			//
			else if (currentMatch.endsWith(".Article.Journal.ISOAbbreviation")) {
				if( article.getJournal() == null ) {
					journal = new Journal();
					article.setJournal(journal);
				}
				if( article.getJournal().getAbbr() != null )
					article.getJournal().setAbbr(article.getJournal().getAbbr() + value);
				else 
					article.getJournal().setAbbr(value);
				
			}
			else if (currentMatch.endsWith(".Article.Journal.Title")) {
				if( article.getJournal() == null ) {
					journal = new Journal();
					article.setJournal(journal);
				}
				if( article.getJournal().getJournalTitle() != null )
					article.getJournal().setJournalTitle(article.getJournal().getJournalTitle() + value);
				else 
					article.getJournal().setJournalTitle(value);
			}
			//
			// Source (journal) doesn't change but must force a lookup in
			// the relevent Lookup FormControl
			else if (currentMatch.endsWith(".AbstractText")) {
				if( article.getAbstractText() != null )
					article.setAbstractText(article.getAbstractText() + value);
				else 
					article.setAbstractText(value);
				
			}
			//
			//
			else if (currentMatch.endsWith(".PubmedArticle.MedlineCitation.PMID")) {
			
				article.setPmid(new Integer(value).intValue());
			
			} else if( currentMatch.endsWith("ArticleId") && 
						!currentAttribute.equals("pubmed")) {

				id.setIdValue(value);
				
			}
			
		} catch (Exception e) {

			this.exceptions.add(e);

		}

	}
	
	public ArrayList<ArticleCitation> getArticles() {
		return this.articles;
	}

}
