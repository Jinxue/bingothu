package Bingo.spider;

import java.io.IOException;
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

	Set<String> visitedURL = new HashSet<String>();

	Map<String, String> imgLinkMap = new HashMap<String, String>(); // for temp

	static IndexManager indexManager;

	int linkNum = 0;
	
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
		indexManager = new IndexManager(
				"E:\\Eclipse_workespace-jee\\Bingo\\index");
		
		// Add the shutdown hook
		Runtime.getRuntime().addShutdownHook(new ShutDownThread(indexManager));  

		
//		System.out.println(spider.getHotVideos());
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
			startDownloadThreads(6);
			
			long begin = System.nanoTime();
			
			int timeOutNum = 0;
			while (linkNum < count) // We just want to
															// exit normally
			// while(!queue.isEmpty())
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
