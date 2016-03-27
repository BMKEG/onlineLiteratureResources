package edu.isi.bmkeg.utils.pubmed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * this class wraps the pubmed central search functions
 * @author cartic
 * 
 */
public class ESearch 
{
	private static String baseQuery="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pmc&term=";
	private boolean debug = false;
	
	public ESearch()
	{
		
	}
	
	public ESearch(String sourceName)
	{
		baseQuery="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db="+sourceName+"&term=";
	}
	
	public ESearch(String sourceName,String field)
	{
		baseQuery="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db="+sourceName+"&field="+field+"&term=";
	}
	
	public ESearch(int retmax)
	{
		baseQuery="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pmc&retmax="+retmax+"&term=";
	}
	
	public String executeQuery(String term){
		StringBuilder sb = new StringBuilder();
		//asthma[mh]+AND+hay+fever[mh] 
		String result="";
		try{
			URL url = new URL(baseQuery+term.trim().replaceAll(" ", "+"));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line;
			while((line=rd.readLine()) != null) {
				if(debug)
					System.out.println(line);
				sb.append(line);
			}
			conn.disconnect();
		}catch (Exception e) {
			// TODO: handle exception
		}
		return sb.toString();
	}
	
	
	
	/**
	 * @param args
	 *
	public static void main(String[] args)
	{
		String searchTerm = "(\"mice\"[MeSH Terms] OR mouse[Figure/Table Caption] OR mouse[Section Title] OR mouse[Body - All Words] OR mouse[Title] OR mouse[Abstract]) OR (\"mice\"[MeSH Terms] OR mice[Figure/Table Caption] OR mice[Section Title] OR mice[Body - All Words] OR mice[Title] OR mice[Abstract]) OR (\"carbamide peroxide\"[Substance Name] OR \"mice\"[MeSH Terms] OR murine[Figure/Table Caption] OR murine[Section Title] OR murine[Body - All Words] OR murine[Title] OR murine[Abstract])";
		//String searchTerm = "mice OR mouse OR murine";
		ESearch primingSearch = new ESearch();
		PubMedCentralQueryResultProcessor pqrp = new PubMedCentralQueryResultProcessor();

		try
		{
			String result = primingSearch.executeQuery(searchTerm);
			EUtilsXmlResponseReader pxrr = new EUtilsXmlResponseReader(result);
			int hitCount = pxrr.getHitCount();
			System.out.println(hitCount);
			ESearch retrievalSearch = new ESearch(20);
			pqrp.getHits(retrievalSearch.executeQuery(searchTerm));
			System.out.println(pqrp.getIds().size());
			Elink pcttdl = new Elink();
			for(String id : pqrp.getIds()){
				pxrr = new EUtilsXmlResponseReader(pcttdl.getTargetLinkForID(id));
				System.out.println("PMC:"+id+"-->PMID:"+pxrr.getLinkTarget());
			}
		} catch (SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		
	}*/



}
