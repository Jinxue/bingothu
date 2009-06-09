package Bingo.spider;

import java.io.IOException;
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
	LinkedList<String> queue = new LinkedList<String>();

	Set<String> visitedURL = new HashSet<String>();

	Map<String, String> imgLinkMap = new HashMap<String, String>(); // for temp

	// static LinkedList<VideoInfo> videoInfoList = new LinkedList<VideoInfo>();

	static IndexManager indexManager;

	int linkNum = 0;

	static HashMap<String, VideoWebsiteFilterInterface> vwFilter;

	Parser parser = new Parser();

	public Spider(HashMap<String, VideoWebsiteFilterInterface> vwFilter) {
		Spider.vwFilter = vwFilter;
	}

	public Spider() {
		// Spider.vwFilter = new YoukuFilter();
		Spider.vwFilter = null;
	}

	public static void main(String[] args) throws Exception {

		// Spider spider = new Spider(); // for youku
		// Spider spider = new Spider(new TudouFilter()); //for Tudou
		HashMap<String, VideoWebsiteFilterInterface> vwFilterMap = new HashMap<String, VideoWebsiteFilterInterface>();
		vwFilterMap.put("Youku", new YoukuFilter());
		vwFilterMap.put("Tudou", new TudouFilter());

		Spider spider = new Spider(vwFilterMap);
		indexManager = new IndexManager(
				"E:\\Eclipse_workespace-jee\\Bingo\\index");
		// Add the shutdown hook
		Runtime.getRuntime().addShutdownHook(new ShutDownThread(indexManager));

		// Thread.sleep(5000);
		// System.exit(0);

		spider.run();
		indexManager.closeIndex();
	}

	public void run() {
		int count = 100000;
		HashMap <String, Double> timeUsed = new HashMap <String, Double>(); 
		for (String source : vwFilter.keySet()) {
			VideoWebsiteFilterInterface filter = vwFilter.get(source); 
			queue.add(filter.getEnterPointURL());

			long begin = System.nanoTime();
			while ((!queue.isEmpty()) && (linkNum < count)) // We just want to
															// exit normally
			// while(!queue.isEmpty())
			{
				String nextLink = (String) queue.removeFirst();
				// System.out.println(nextLink);

				try {
					parseHtml(filter, nextLink);
					System.out.println(linkNum);

				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
			VideoInfo videoInfo = getMoreInfo(filter, parser, url);
			getMoreLinks(filter, url, parser);

			if (videoInfo != null) {
//				 System.out.println("**** video Info : "+
//				 videoInfo.toString());

				// Put the index file to indexed DB
				indexManager.addIndex(videoInfo);

				/*
				 * synchronized(videoInfoList){ videoInfoList.add(videoInfo); }
				 */
				++linkNum;
			}

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
				
				queue.add(link);
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

	/*
	 * public static VideoInfo getVideoInfo() { synchronized(videoInfoList){
	 * if(videoInfoList.size()!=0) return videoInfoList.removeFirst(); return
	 * null; } }
	 */
}
