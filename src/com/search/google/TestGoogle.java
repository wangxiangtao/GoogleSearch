package com.search.google;
import java.util.ArrayList;
import java.util.List;


public class TestGoogle {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		GoogleSearch g = new GoogleSearch(true);
		g.setDebugMode(true);
		g.setThreadNum(4);
		List<String> keywords = new ArrayList<String>();
		for( int t = 0 ; t<100 ; t++){
			keywords.clear();
			for(int i=0 ; i<1000;i++){
				keywords.add((i+"----dd"+i));
			}
			g.setKeywords(keywords);
			g.search();
			System.out.println("size:"+(t+1)*1000+" "+ g.getSearchResult().size());
		}
		g.close();
        System.exit(0);

	}

}
