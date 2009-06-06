package Bingo.spider ;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.CorruptIndexException;
import org.htmlparser.*;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import Bingo.index.IndexManager;

public class Spider implements Runnable
{
    LinkedList<String> queue = new LinkedList<String>();
    
    Set<String>  visitedURL = new HashSet<String>();
    
    Map<String,String> imgLinkMap = new HashMap<String,String>();  // for temp
    
//    static LinkedList<VideoInfo> videoInfoList = new LinkedList<VideoInfo>();
    
    static IndexManager indexManager;
    
    int linkNum = 0;
    
    static VideoWebsiteFilterInterface vwFilter ;
    
    Parser parser = new Parser();
    
    public Spider(VideoWebsiteFilterInterface vwFilter)
    {
    	Spider.vwFilter = vwFilter;
    }
    
    public Spider()
    {
    	Spider.vwFilter = new YoukuFilter();    
    }
	
    public static void main(String[] args) throws Exception{
    	
//	   	Spider spider = new Spider();  // for youku
	   	Spider spider = new Spider(new TudouFilter()); //for Tudou
	   	indexManager = new IndexManager("E:\\Eclipse_workespace-jee\\Bingo\\index");
	   	spider.run();
	   	indexManager.closeIndex();    	
     }
    
    public void run()
    {
    	queue.add(vwFilter.getEnterPointURL());
    	
    	//while((!queue.isEmpty()) && (linkNum < 100))	//We just want to exit normally
    	while(!queue.isEmpty())
    	{
    		String nextLink = (String)queue.removeFirst();
   // 		System.out.println(nextLink);
    		
   		    try {
				
   		    	
   		    	parseHtml(nextLink);
   		    	System.out.println(linkNum); 
				
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	System.out.println("*****************************************************");
    	System.out.println(linkNum);     	
    
    }
    
    protected void parseHtml (String url ) throws CorruptIndexException, IOException
    {
    	try {    		
			parser.setURL(url);
			VideoInfo videoInfo = getMoreInfo(parser,url);
			getMoreLinks(url , parser);			
			
		
			
			if(videoInfo != null)
			{
	//      	System.out.println("**** video Info : "+ videoInfo.toString());
				
				// Put the index file to indexed DB
				indexManager.addIndex(videoInfo);
				
	/*			synchronized(videoInfoList){
					videoInfoList.add(videoInfo);
				}		*/		
				++linkNum;
			}			
			
		} catch (ParserException e) {
			System.out.println(url);
			e.printStackTrace();
		}
    }    
    
    /**
     * Parse html to get more links to be visited
     * @param parser
     * @param videoLinkFilter
     * @throws ParserException
     */
    protected void getMoreLinks(String currentUrl , Parser parser) throws ParserException
    {
    	parser.reset();
    	Map<String , String> linkToImgLink = vwFilter.getLinkAndImgLink(currentUrl , parser);
        Set<String> keySet = linkToImgLink.keySet();
    	for(Iterator<String> it = keySet.iterator(); it.hasNext();)
    	{
    		String link = it.next();    		
    		
    		if(!visitedURL.contains(link))
    		{
    			String imgLink = linkToImgLink.get(link);
//        		System.out.println(link+"\n"+imgLink);
        		
    			imgLinkMap.put(link, imgLink);    			
    			
    			queue.add(link);
    		}
    	}
    }
    
    /**
     * Get more info about the video linked being visited
     * @throws ParserException 
     */
    protected VideoInfo getMoreInfo(Parser parser, String url) throws ParserException
    {
    	String imgUrl = imgLinkMap.get(url);
    	if(imgUrl != null)
    	{
    		imgLinkMap.remove(url);
    		return vwFilter.getVideoInfo(parser,url , imgUrl);
    	}
    	return null ;
    }
    
 /*   public static VideoInfo getVideoInfo()
    {
    	synchronized(videoInfoList){
    		if(videoInfoList.size()!=0)
    			return videoInfoList.removeFirst();
    		return null;
    	}
    } */
} 

