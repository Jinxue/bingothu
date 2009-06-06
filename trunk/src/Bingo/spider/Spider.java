package Bingo.spider ;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
    
    static LinkedList<VideoInfo> videoInfoList = new LinkedList<VideoInfo>();
    
    static IndexManager indexManager;
    
    int linkNum = 0;
    
    static VideoWebsiteFilterInterface vwFilter ;
    
    Parser parser = new Parser();
    
    public Spider(VideoWebsiteFilterInterface vwFilter)
    {
    	this.vwFilter = vwFilter;
    }
    
    public Spider()
    {
    	this.vwFilter = new YoukuFilter();    
    }
	
    public static void main(String[] args) throws Exception{
	   	Spider spider = new Spider();
	   	indexManager = new IndexManager("E:\\Eclipse_workespace-jee\\Bingo\\index");
	   	spider.run();
	   	indexManager.closeIndex();
     }
    
    public void run()
    {
    	queue.add(vwFilter.getEnterPointURL());
    	
    	while(!queue.isEmpty())
    	{
    		String nextLink = (String)queue.removeFirst();
   // 		System.out.println(nextLink);
    		System.out.println(linkNum); 
   		    try {
				parseHtml(nextLink, vwFilter.getLinksFilter());
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	System.out.println("*****************************************************");
    	System.out.println(linkNum); 
    }
    
    protected VideoInfo getInfo(String url, LinkedList<String> queue)
    {
    	
		return null;    	
    }
    
    protected void parseHtml (String url , NodeFilter videoLinkFilter) throws CorruptIndexException, IOException
    {
    	try {    		
			parser.setURL(url);
			VideoInfo videoInfo = getMoreInfo(parser,url);
			getMoreLinks(parser,videoLinkFilter);			
			
			if(videoInfo != null)
			{
				System.out.println(videoInfo.toString());
				
				// Put the index file to indexed DB
				indexManager.addIndex(videoInfo);
				
				synchronized(videoInfoList){
					videoInfoList.add(videoInfo);
				}				
				++linkNum;
			}			
			
		} catch (ParserException e) {
			// TODO Auto-generated catch block
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
    protected void getMoreLinks(Parser parser,NodeFilter videoLinkFilter) throws ParserException
    {
    	parser.reset();
    	NodeList nodes = vwFilter.getNodeList(parser);
    	for(int i=0;i<nodes.size();++i)
    	{
//    		System.out.println(nodes.elementAt(i).toHtml());
    		String link = ((TagNode)nodes.elementAt(i)).getAttribute("href");
//   		    System.out.println(link);
    		if(!visitedURL.contains(link))
    		{
    			String imgLink = ((TagNode)nodes.elementAt(i).getFirstChild()).getAttribute("src");
//    			System.out.println(imgLink);
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
    
    public static VideoInfo getVideoInfo()
    {
    	synchronized(videoInfoList){
    		if(videoInfoList.size()!=0)
    			return videoInfoList.removeFirst();
    		return null;
    	}
    }
} 

