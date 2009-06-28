package Bingo.spider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.CorruptIndexException;
import org.htmlparser.*;
import org.htmlparser.util.ParserException;

import Bingo.index.IndexManager;

public class Spider implements Runnable {
	
	static LinkedList<String> queue = new LinkedList<String>();

	static Set<String> visitedURL = new HashSet<String>();

	static Map<String, String> imgLinkMap = new HashMap<String, String>(); // for temp

	static IndexManager indexManager;

	static int linkNum = 0;
	
	int count = 100000;

	static HashMap<String, VideoWebsiteFilterInterface> vwFilter;

	Parser parser = new Parser();
	
	ArrayList<Thread> threadList = new ArrayList<Thread>();
	
	

	public Spider(HashMap<String, VideoWebsiteFilterInterface> vwFilter) {
		Spider.vwFilter = vwFilter;
	}

	public Spider() {
		// Spider.vwFilter = new YoukuFilter();
		Spider.vwFilter = null;
	}

	public static void main(String[] args) throws Exception {

		HashMap<String, VideoWebsiteFilterInterface> vwFilterMap = new HashMap<String, VideoWebsiteFilterInterface>();
		vwFilterMap.put("Youku", new YoukuFilter());
		vwFilterMap.put("Tudou", new TudouFilter());

		Spider spider = new Spider(vwFilterMap);
    	spider.loadVistitedData();  //load the data saved
		
		indexManager = new IndexManager(
				"E:\\Eclipse_workespace-jee\\Bingo\\index");
		
		// Add the shutdown hook
		Runtime.getRuntime().addShutdownHook(new ShutDownThread(indexManager));  
		Runtime.getRuntime().addShutdownHook(new SaveVisitedDataThread(spider));  

		
		spider.run();
	    indexManager.closeIndex();  
		
		
	}

	public static String getNextUrl()
	{
		synchronized(queue)
		{
			if(queue.size()==0)
				return null;
			return queue.removeFirst();
		}
	}
	
	protected void startDownloadThreads(int num)
	{
		for(int i=0;i<num;++i)
		{
			Thread downLoaderThread = new Thread(new Downloader());
			threadList.add(downLoaderThread);
			
		    downLoaderThread.start();
		}
	}
	
	protected void stopDownloadThreads()
	{
		for(Thread downloadThread : threadList)
		{
			if(downloadThread.getState() != Thread.State.TERMINATED)
				downloadThread.stop();
		}
	}
	
	/**
	 * Store the URLs which has been visited , also the same to the URLs unvisited in the queue  
	 * 
	 */
	protected void storeVisitedData()
	{
        String fileQueueName = "queue.save";	
        String fileVisitedUrlName = "visitedURL.save";
        String fileImgLinkName = "imgLink.save";
        
        
        try {
			PrintWriter outerQueue = new PrintWriter(
									        new BufferedWriter(
									        	new OutputStreamWriter(
									        			new FileOutputStream(fileQueueName))));
//			outerQueue.println(queue.size());
			for(String s : queue)
			{
	//			outerQueue.write(s);				
				outerQueue.println(s);
			}
			outerQueue.flush();
			
			PrintWriter outerVisitedURL = new PrintWriter(
										        new BufferedWriter(
										        	new OutputStreamWriter(
										        			new FileOutputStream(fileVisitedUrlName))));
//			outerVisitedURL.println(visitedURL.size());
			outerVisitedURL.println(Integer.toString(linkNum));
			for(String s : visitedURL)
			{
	//			outerVisitedURL.write(s);
				outerVisitedURL.println(s);
			}
			outerVisitedURL.flush();
			
			PrintWriter outerImgLink = new PrintWriter(
									        new BufferedWriter(
									        	new OutputStreamWriter(
									        			new FileOutputStream(fileImgLinkName))));
			
//			outerImgLink.println(imgLinkMap.size());
			for(String s : imgLinkMap.keySet())
			{
	//			outerImgLink.write(s+"@"+imgLinkMap.get(s));
				outerImgLink.println(s+"@"+imgLinkMap.get(s));
			}
			outerImgLink.flush();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load the data stored before
	 * 
	 */
	protected void loadVistitedData()
	{
		String fileQueueName = "queue.save";	
        String fileVisitedUrlName = "visitedURL.save";
        String fileImgLinkName = "imgLink.save";
        
        try {
/*	        File fileQueue = new File(fileQueueName);
	        if(fileQueue.exists())
	        {
	        	BufferedReader bfQueue = new BufferedReader(new FileReader(fileQueue));
	        	String line = bfQueue.readLine();
	        	while((line=bfQueue.readLine())!=null)
	        		queue.add(line);
	        } */
	        
	        File fileVisitedURL = new File(fileVisitedUrlName);
	        if(fileVisitedURL.exists())
	        {
	        	BufferedReader bfVisitedURL = new BufferedReader(new FileReader(fileVisitedURL));
//	        	bfVisitedURL.readLine();
	        	
	        	String linkNumStr = bfVisitedURL.readLine();
	        	linkNum = Integer.parseInt(linkNumStr);
	        	String line;
	        	while((line=bfVisitedURL.readLine())!=null)
	        		visitedURL.add(line);
	        }
	        
	        File fileImgLink = new File(fileImgLinkName);
	        if(fileImgLink.exists())
	        {
	        	BufferedReader bfImgLink = new BufferedReader(new FileReader(fileImgLink));
	        	String line ;
	        	while((line=bfImgLink.readLine())!=null)
	        	{
	        		String source = line.substring(0,line.indexOf('@'));
	        		queue.add(source);
	        		
	        		String dest = line.substring(line.indexOf('@')+1) ;
	        		imgLinkMap.put(source, dest);
	        	}
	        }
	        
	        
        }catch (FileNotFoundException e) {
			e.printStackTrace();
        }
        catch(IOException e){
        	e.printStackTrace();
        }        
	} 
	
	/**
	 * Search the hot videos 
	 * @return
	 */
	public Map<String , ArrayList<VideoInfo>> getHotVideos()
	{
		Map<String , ArrayList<VideoInfo>> hotVideos = new HashMap<String, ArrayList<VideoInfo>>();
		
		for(String source : vwFilter.keySet())
		{
			VideoWebsiteFilterInterface filter = vwFilter.get(source);
			String enterPointUrl = filter.getEnterPointURL();
			
//			System.out.println(enterPointUrl);
			
			Map<String , String> imgLinkMap ;
			
			try {
				Parser parser = new Parser(enterPointUrl);
				imgLinkMap = filter.getLinkAndImgLink(enterPointUrl, parser);
				
//				System.out.println(imgLinkMap);
				
				ArrayList<VideoInfo> videoInfos = new ArrayList<VideoInfo>();
				
				parser.reset();
				for(String url : imgLinkMap.keySet())
				{
//					System.out.println(url);
					parser.setURL(url);
					
					String imgUrl = imgLinkMap.get(url);					
					
					VideoInfo videoInfo = filter.getVideoInfo(parser, url, imgUrl);
					videoInfos.add(videoInfo);		
					System.out.println(videoInfo);
				}
				
				hotVideos.put(source, videoInfos);				
				
			} catch (ParserException e) {
				
				e.printStackTrace();
			}
		}
		
		return hotVideos ;
	}
	
	public void run() {
		
		HashMap <String, Double> timeUsed = new HashMap <String, Double>(); 
		for (String source : vwFilter.keySet()) {
			VideoWebsiteFilterInterface filter = vwFilter.get(source); 
			queue.add(filter.getEnterPointURL());			

			//--- Start several threads to download web files 
			startDownloadThreads(5);
			
            long begin = System.nanoTime();
			
			int timeOutNum = 0;
			while (linkNum < count) // We just want to exit normally			
			{				
				String nextLinkPath = Downloader.readHtmlData();
				if(nextLinkPath == null)
				{
					if(timeOutNum < 1000000000)
					{
						++timeOutNum;						
					}
					else
						break;
				}
				else
				{
					// System.out.println(nextLink);
					timeOutNum = 0;
					
					try {
						parseHtml(filter, nextLinkPath);
						System.out.println(linkNum);

					} catch (CorruptIndexException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

            stopDownloadThreads();
			
			double temp = (System.nanoTime() - begin) / 1000000000.0 ;
			timeUsed.put(source, new Double(temp));
			System.out.println("*********************From" + source + " ********************************");
			System.out.println(linkNum);
			queue.clear();
			linkNum = 0;
		}
		
		// Print the time used
		for (String source : timeUsed.keySet()){
			System.out.println("Spide " + count + " webpages from " + source + " using time(s): " + timeUsed.get(source));
		}

	}

	protected void parseHtml(VideoWebsiteFilterInterface filter, String url) throws CorruptIndexException,
			IOException {
		try {
			parser.setURL(url);
			
//			System.out.println(url);
			
			parser.setEncoding("UTF-8");
			
			String realURL = Downloader.getUrlFromPath(url);
			VideoInfo videoInfo = getMoreInfo(filter, parser, realURL);
			getMoreLinks(filter, realURL, parser);

			if (videoInfo != null) {
//				 System.out.println("**** video Info : "+
//				 videoInfo.toString());

				// Put the index file to indexed DB
				indexManager.addIndex(videoInfo);

				++linkNum;
			}
			
			Downloader.eraseHtmlData(url);

		} catch (ParserException e) {
			System.out.println(url);
			e.printStackTrace();
		}
	}

	/**
	 * Parse html to get more links to be visited
	 * @param filter 
	 * 
	 * @param parser
	 * @param videoLinkFilter
	 * @throws ParserException
	 */
	protected void getMoreLinks(VideoWebsiteFilterInterface filter, String currentUrl, Parser parser)
			throws ParserException {
		parser.reset();
		Map<String, String> linkToImgLink = filter.getLinkAndImgLink(
				currentUrl, parser);
		Set<String> keySet = linkToImgLink.keySet();
		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
			String link = it.next();

			if (!visitedURL.contains(link)) {
				String imgLink = linkToImgLink.get(link);
				// System.out.println(link+"\n"+imgLink);

				visitedURL.add(link);
				
				imgLinkMap.put(link, imgLink);
				
				synchronized(queue)
				{
					queue.add(link);
				}
			}
		}
	}

	/**
	 * Get more info about the video linked being visited
	 * @param filter 
	 * 
	 * @throws ParserException
	 */
	protected VideoInfo getMoreInfo(VideoWebsiteFilterInterface filter, Parser parser, String url)
			throws ParserException {
		String imgUrl = imgLinkMap.get(url);
		if (imgUrl != null) {
			imgLinkMap.remove(url);
			return filter.getVideoInfo(parser, url, imgUrl);
		}
		return null;
	}

}
