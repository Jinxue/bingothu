package Bingo.spider ;

import java.util.LinkedList;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.LinkRegexFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class YoukuFilter extends VideoWebsiteFilterInterface{
	
	public YoukuFilter()
	{
		linkFilter = new AndFilter(
			            new NodeFilter[]{
			                	  new TagNameFilter("a"),  
			                	  new HasChildFilter(
			                			  new TagNameFilter("img")),					                	
			                	  new HasAttributeFilter("target","video")
			                  });

        infoFilter = new AndFilter(
			            new NodeFilter[]{
			            		new TagNameFilter("meta"),
			            		new HasAttributeFilter("name")
			            });
	}
	
	public NodeFilter getLinksFilter()
	{
	   	return linkFilter ;
	}
	
	public NodeFilter getInfoFilter()
	{
		return infoFilter ;
	}
	
	public VideoInfo getVideoInfo(Parser parser, String linkUrl, String imgUrl) throws ParserException
	{
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.setSource("Youku");
		NodeFilter filter = infoFilter; 
		NodeList nodes = parser.parse(filter);
    	for(int i=0;i<nodes.size();++i)
    	{
    		String name = ((TagNode)nodes.elementAt(i)).getAttribute("name");
    		String content = ((TagNode)nodes.elementAt(i)).getAttribute("content");
    		if(name.compareToIgnoreCase("title")==0)
    			videoInfo.setTitle(content);
    		else if(name.compareToIgnoreCase("keywords")==0)
    			videoInfo.setKeyWord(content);
    		else if(name.compareToIgnoreCase("description")==0)
    			videoInfo.setDescription(content);
    	}
    	videoInfo.setUrl(linkUrl);
    	videoInfo.setImgUrl(imgUrl);
    	
    	return videoInfo;
    	
	}
	
	public String getEnterPointURL()
	{
		return "http://www.youku.com/";
	}
}
