
package com.search.google;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class SearchWikipedia {
	
	public static Map<String,String> linkMap = new HashMap<String,String>();
	public static GoogleSearch g = null;
	public static Map<String,String> idmap = new HashMap<String,String>();
	public static Map<String,String> processedCompanyMap = new HashMap<String,String>();
	public static SqlHelper sqlHelper = new SqlHelper();
	
	public static void main(String[] args) throws InterruptedException {
		searchAll();
	}

	private static void searchAll() {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("config/database.properties"));
			String tablename = properties.getProperty("db.companytablename");
			
			g = new GoogleSearch(true);
			g.setDebugMode(true);
		    g.setThreadNum(2);
			
			ArrayList<Map<String,Object>> googleWebsiteTable = sqlHelper.executeQuery("select company_id,link from google_website", null);
			
			for(Map<String,Object> googleWebsiteRecord: googleWebsiteTable){
				System.out.println(googleWebsiteRecord.get("link"));
				processedCompanyMap.put(googleWebsiteRecord.get("company_id").toString(), "");
				linkMap.put(googleWebsiteRecord.get("link").toString(), "");
			}

			System.out.println("linkMap size:"+linkMap.size());
			
			ArrayList<Map<String,Object>> companyTable  = sqlHelper.executeQuery("select * from "+tablename,null);

			List<String> keywords = new ArrayList<String>();
			int count = 0;
			for(Map<String,Object> companyRecord: companyTable){
				  count++;
				  String name = companyRecord.get("company_name").toString();
				  Object website = companyRecord.get("website");
				  String id = companyRecord.get("id").toString();
				  if(processedCompanyMap.containsKey(id))continue;
				  if(website!=null&&!website.toString().trim().equals("")) continue;
				  idmap.put(name,id);
				  keywords.add(name);
				  
				  if(count>99){
				      search(keywords);
				      keywords.clear();
				      count = 0;
				  }
			}
		    search(keywords);
		      
	        System.exit(0);
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void search(List<String> keywords) throws SQLException {
		g.setKeywords(keywords);
		try {
			g.search();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		String [] insertSqls = new String[200] ;
		int i = 0;
		for(String key : g.getSearchResult().keySet()){
			for ( SearchResult s : g.getSearchResult().get(key)){
				String text = s.getText().replace("'"," ");
				String url = s.getLink();
				String host = null;
				try {
					URL aURL = new URL(url);
					host = aURL.getHost();
					if(host==null||linkMap.containsKey(host)) continue;
				} catch (MalformedURLException e) {
					continue;
				}
				String id = idmap.get(key);
				insertSqls[i] = "INSERT INTO google_website (company_name,company_id ,link,link_text) VALUES "
							         + "('"+key+"','" +id+ "','"+host+"','"+text+"')" ;
				i++;
			}
		}
		if(insertSqls.length>0)
				sqlHelper.executeUpdate(insertSqls, null);

	}
	
}
