package com.search.google;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.silvertunnel_ng.netlib.adapter.java.JvmGlobalUtil;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;

public class GoogleSearch implements Runnable{
  //  private static final Logger log = Logger.getLogger(GoodleCrawlWithTOR.class.getName());
	private List<String> keywords ;
	private  int  index;
	private  static int  threadNum = 2;
	private  Map<String,List<SearchResult>>  searchResult =  new ConcurrentHashMap<String,List<SearchResult>>();
	private  WebDriverWait wait;
	private  boolean debugMode = false;
	private  int TotalSearchNumberOfTime = 3 ; 
	private  ConcurrentLinkedQueue <WebDriver> driverQueue = new ConcurrentLinkedQueue <WebDriver>();
	private  Long lastStartUpdate = 0l;
	private  Boolean updateState = false;
	private  int debugFrequence = 10;
	private   String domain = "http://www.google.com.sg";

	public GoogleSearch(boolean enableTor){

		LogManager.getLogManager().reset();
		Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
		globalLogger.setLevel(java.util.logging.Level.OFF);
		JvmGlobalUtil.init();
		debugMode = true;
		if(enableTor){
			perpareAnonymousCommunication();
		}
		debugMode = false;
	}
	
	public synchronized WebDriver getDriver(){
		
		DesiredCapabilities capability = DesiredCapabilities.htmlUnit();
		capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, false);
		capability.setJavascriptEnabled(false);
		WebDriver driver = new HtmlUnitDriver(capability);
//		  System.setProperty("webdriver.chrome.driver", "C://Program Files (x86)//Google//Chrome//Application//chromedriver.exe");
//		WebDriver driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
		driver.manage().timeouts().pageLoadTimeout(3, TimeUnit.SECONDS);
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(3, TimeUnit.SECONDS);
		driver.manage().deleteAllCookies();
		driver.get(domain);
		wait = new WebDriverWait(driver, 3);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return driver;
	}

    public void run() {
    	WebDriver driver ;
    	if(driverQueue.size()>0) driver = driverQueue.poll();
    	else driver = getDriver();
    	getSearchResult().clear();
    	while(true){
    		if(index<keywords.size()){
    			driver = search(driver,index++,1);
    		}
    		else break;
        }
    	driverQueue.add(driver);
    }
	public static void main(String[] args) throws Exception {
		GoogleSearch g = new GoogleSearch(true);
		g.setDebugMode(true);
		g.setThreadNum(threadNum);
		List<String> keywords = new ArrayList<String>();
		for( int t = 0 ; t<1000 ; t++){
			keywords.clear();
			for(int i=0 ; i<100;i++){
				keywords.add("qingdao "+i);
			}
			g.setKeywords(keywords);
			g.search();
			System.out.println("size:"+(t+1)*100+" "+ g.getSearchResult().size());
			for(String key : g.getSearchResult().keySet()){
				for ( SearchResult s : g.getSearchResult().get(key)){
					String text = s.getText();
				//	System.out.println(text);
				}
			}
		}
		g.close();
        System.exit(0);
	}

	public void search() throws InterruptedException{
		index = 0;
        List<Thread> list = new ArrayList<Thread>();  
		for(int i=1 ;i<=this.getThreadNum();i++){
			Thread t = new Thread(this, "Thread_NUM"+i);
			t.start();
			list.add(t);
		}
		for(Thread thread : list)  
        {  
            thread.join(); 
        }  
		for(Thread thread : list)  
        {  
            thread.interrupt();
        }  
	}
	private WebDriver search(WebDriver driver, int index , int searchNumberOfTime) {
		
			String key = keywords.get(index);
			if(debugMode&&index>0&&index %debugFrequence==0){
				System.out.println(index);
			}
		//	System.out.println("search:"+key);

			try {	
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("q")));
				driver.findElement(By.name("q")).clear();
				WebElement element = driver.findElement(By.name("q"));
				element.sendKeys(key);
			    element.submit();
				List<SearchResult> list = GetResultList(driver, key , 2);
				saveResult(key , list);

			} catch (Exception e) {
				//	e.printStackTrace();
					if(searchNumberOfTime > TotalSearchNumberOfTime)  return driver;
					long current =  System.currentTimeMillis();
					if(((current - lastStartUpdate)/1000)>15){
						if(debugMode)
							System.out.println(Thread.currentThread().getName()+": Current IP is blocked");
						perpareAnonymousCommunication();
					}
					else {
						while(updateState){
							try {
								Thread.sleep(2000+(int)(Math.random()*2000));
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
					driver.quit();
					driver = getDriver();
					try {
						Thread.sleep((int)(Math.random()*2000));
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
			    	search(driver,index,++searchNumberOfTime);
					return driver;
			}
	
		return driver;
	}

	private List<SearchResult> GetResultList(WebDriver driver, String key , int numIter) {
		List<SearchResult> list = new ArrayList<SearchResult>();
		if(numIter<1) return list;
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("r")));
		List<WebElement> elements = driver.findElements(By.className("r"));
	//	System.out.println("size:"+elements.size());

		if(elements!=null&&elements.size()!=0)
		{
			SearchResult sr = null;
			for(WebElement e :elements){
				sr = new SearchResult();
				String link = null;
				try {
					link = e.findElement(By.tagName("a")).getAttribute("href");
			//		System.out.println(key+"--"+link);

					if(link==null)continue;
					int start = link.indexOf("q=http")+2;
					if(start==-1) continue;
					int end = link.indexOf("&sa");
					if(end==-1) continue;
					link = link.substring(start,end);
					
					sr.setLink(link);
					sr.setText(e.getText());
					list.add(sr);

				} catch (Exception e1) {
					System.out.println("-----Cannot find element with tag name: a,numIter:"+numIter);
					 numIter--;
					list = GetResultList(driver,key , numIter);
					break;
				}
			}
		}
		return list;
	}
	private synchronized void saveResult(String key ,List<SearchResult> list) {
		searchResult.put(key, list);
	}
	
	public synchronized void perpareAnonymousCommunication() {
		updateState = true;
		lastStartUpdate = System.currentTimeMillis();
		if(debugMode)
			System.out.println("---------------StartChangeNetwork--------------------------");
		NetLayer nextNetLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR); 
        JvmGlobalUtil.setNetLayerAndNetAddressNameService(nextNetLayer, true);
		if(debugMode)
			System.out.println("---------------EndchangeNetwork----------------------------");
		updateState = false;
	}
	public  void close() {
		while(!driverQueue.isEmpty()){
			WebDriver driver = driverQueue.poll();
			driver.close();
		}
	}
	public int getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	
	public List<String> getKeywords() {
		return keywords;
	}
	
	public void setKeywords(List<String> keywords) {
		if(keywords.size()>1000)
			 System.out.println("It is dengorous to set more than 1000 keywords, maybe out of memory. " +
			 		"suggest 1000 for initial");
		this.keywords = keywords;
	}
	
	public Map<String, List<SearchResult>> getSearchResult() {
		return searchResult;
	}

	public void setSearchResult(Map<String, List<SearchResult>> searchResult) {
		this.searchResult = searchResult;
	}
	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	public int getTotalSearchNumberOfTime() {
		return TotalSearchNumberOfTime;
	}

	public void setTotalSearchNumberOfTime(int totalSearchNumberOfTime) {
		TotalSearchNumberOfTime = totalSearchNumberOfTime;
	}
	public int getDebugFrequence() {
		return debugFrequence;
	}

	public void setDebugFrequence(int debugFrequence) {
		this.debugFrequence = debugFrequence;
	}
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}