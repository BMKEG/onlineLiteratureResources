package edu.isi.bmkeg.utils.pubmed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;


public class Elink
{
	//private static String baseQuery="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pmc&id=";
	//private static String querySuffix = "&cmd=neighbor&db=pubmed&LinkName=pmc_pubmed";
	private static String baseQuery="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?";
	private String querySuffix;
	private boolean debug = false;
	
	private static String PUBMED = "pubmed";
	private static String PMC = "pmc";
	private static String PMC_PUBMED_REFS = "pmc_pubmed_refs";
	private static String PUBMED_PMC_REFS= "pubmed_pmc_refs";
	private static String PUBMED_PUBMED_REF = "pubmed_pubmed_refs";
	private static String PUBMED_PUBMED_CITED_IN = "pubmed_pubmed_citedin";
	private static String COMMAND_NEIGHBOR = "&cmd=neighbor";
	private static String DB_FROM = "&dbfrom=";
	private static String DB_TO = "&db=";
	private static String ID = "&id=";
	private static String LINK = "&LinkName=";
	
	@Deprecated
	private Set<String> sourceIds;
	
	public Elink(){
		querySuffix="";
	}
	@Deprecated 
	/**
	 * {@link edu.isi.bmkeg.utils.pubmedCentral.eutils.PubMedCentralToTargetDBLinker}
	 * 
	 */
	public Elink(Set<String> pmcIDs){
		this.sourceIds = pmcIDs;
	}
	
	public String getPMCToPubMedLink(String pmcID){
		querySuffix = querySuffix+DB_FROM+PMC+ID+pmcID+COMMAND_NEIGHBOR+DB_TO+PUBMED+LINK+PMC_PUBMED_REFS;
		String query=baseQuery+querySuffix;
		return executeQuery(query);
	}
	public String getPubMedLinkToPMC(String pubmedID){
		querySuffix = querySuffix+DB_FROM+PUBMED+ID+pubmedID+COMMAND_NEIGHBOR+DB_TO+PMC+LINK+PUBMED_PMC_REFS;
		String query=baseQuery+querySuffix;
		return executeQuery(query);
	}
	public String getPubMedToPubMedLink(String pubmedID){
		querySuffix = querySuffix+DB_FROM+PUBMED+ID+pubmedID+COMMAND_NEIGHBOR+DB_TO+PUBMED+LINK+PUBMED_PUBMED_REF;
		String query=baseQuery+querySuffix;
		return executeQuery(query);
	}
	@Deprecated
	public String constructConjunctiveQuery(){
		StringBuilder sb = new StringBuilder();
		for(String id : sourceIds){
			sb.append(id+",");
		}
		String ids = sb.toString();
		return baseQuery+ids.substring(0, ids.length()-1)+querySuffix;
	}
	@Deprecated
	public String getTargetLinkForID(String pmcID){
		String query=baseQuery+pmcID+querySuffix;
		return executeQuery(query);
	}
	public String executeQuery(String query){
		StringBuilder sb = new StringBuilder();
		try{
			URL url = new URL(query);
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
//
}
