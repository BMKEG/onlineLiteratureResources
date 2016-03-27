package edu.isi.bmkeg.utils.pubmed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.isi.bmkeg.digitalLibrary.ArticleCitation;

public class BuildCitationFromMedlineMetaData {

	public int getPMIDFromMetadata(String author, String year,
			String volume, String page) throws Exception {
		
		// simple fix for double-barreled names.
		author = author.replaceAll("_", "%20");
		
		MedlineEUtilsService meus = new MedlineEUtilsService(MedlineEUtilsService.ESEARCH, MedlineEUtilsService.PUBMED);
		EsearchHandler esh = (EsearchHandler) meus.getHandler();
		
		meus.exec("term=(" + author + "[au]+AND+" + year + "[dp]+AND+" + volume + "[vi]+AND+" + page + "[pg])");
		
		int pmid = -1;
		if( esh.getIds().size() == 1 )
			pmid = esh.getIds().get(0);
		
		return pmid;

	}

	public ArticleCitation getArticleFromPMID(int pmid) throws Exception {

		ArticleCitation a = null;

		MedlineEUtilsService meus = new MedlineEUtilsService(MedlineEUtilsService.EFETCH, MedlineEUtilsService.PUBMED);
		EfetchHandler efh = (EfetchHandler) meus.getHandler();
		meus.exec("id=" + pmid);

		a = efh.getArticles().get(0);

		return a;

	}
	
	public List<Integer> getPmidsFromQuery(String query, int retstart, int retmax) throws Exception {
		
		MedlineEUtilsService meus = new MedlineEUtilsService(MedlineEUtilsService.ESEARCH, MedlineEUtilsService.PUBMED);
		meus.setRetstart(retstart);
		meus.setRetmax(retmax);
		EsearchHandler esh = (EsearchHandler) meus.getHandler();

		meus.exec("term=(" + query + ")" );

		return esh.getIds();

	}

	public ArrayList<ArticleCitation> getArticlesFromPmids(ArrayList<Integer> ids) throws Exception {
		
		MedlineEUtilsService meus = new MedlineEUtilsService(MedlineEUtilsService.EFETCH, MedlineEUtilsService.PUBMED);
		EfetchHandler esh = (EfetchHandler) meus.getHandler();

		String q = "";
		Iterator<Integer> it = ids.iterator();
		while(it.hasNext()) {
			int i = it.next().intValue();
			q += i;
			if( it.hasNext() ) 
				q += ",";
		}
		
		meus.exec("id=" + q + "" );

		return esh.getArticles();

	}

	
	
	
}
