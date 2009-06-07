package Bingo.spider ;

import java.util.LinkedList;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.LinkRegexFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class TudouFilter extends VideoWebsiteFilterInterface{
	
	public TudouFilter()
	{
		linkFilter = new AndFilter(
			            new NodeFilter[]{
			                	  new TagNameFilter("a"),  
			                	  new HasChildFilter(
			                			  new TagNameFilter("img")),
			                	  new LinkRegexFilter(".*tudou.com/.*")
			                  });

       infoFilter = new OrFilter(
			            new NodeFilter[]{
			            		new AndFilter(new TagNameFilter("meta"),
					            		      new HasAttributeFilter("name")),
					            new TagNameFilter("title")
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
		videoInfo.setSource("Tudou");
		NodeList nodes = parser.parse(infoFilter);
    	for(int i=0;i<nodes.size();++i)
    	{
    		TagNode tagNode = (TagNode)nodes.elementAt(i);
//    		System.out.println(tagNode.getTagName());
    		if(tagNode.getTagName().compareToIgnoreCase("meta")==0)
    		{
    			String name = tagNode.getAttribute("name");
        		String content = tagNode.getAttribute("content");
        		if(name.compareToIgnoreCase("keywords")==0)
        			videoInfo.setKeyWord(content);
        		else if(name.compareToIgnoreCase("description")==0)
        			videoInfo.setDescription(content);
    		}
    		else if(tagNode.getTagName().compareToIgnoreCase("title")==0)
    		{
    			videoInfo.setTitle(tagNode.getFirstChild().getText());
    		}
    	}
    	videoInfo.setUrl(linkUrl);
    	videoInfo.setImgUrl(imgUrl);
    	
//   	System.out.println(videoInfo.toString());
    	
    	return videoInfo;
    	
	}
	
	public String getEnterPointURL()
	{
		return "http://www.tudou.com/";
	}	
	
	protected String getImgLinkFromImgTag(TagNode tagNode)
	{
/*		String imgClass = tagNode.getAttribute("class");
		if(imgClass != null)
		{
			if(isDelayImg(imgClass))
				return tagNode.getAttribute("alt");			
		}	
		return tagNode.getAttribute("src");		*/
		String src = tagNode.getAttribute("src");
		String alt = tagNode.getAttribute("alt");
		if(src != null && isEndWith(src,".jpg"))
			return src ;
		if(alt != null && isEndWith(alt,".jpg"))
			return alt ;
		return null ;
	}
	
	private boolean isEndWith(String str , String subStr)
	{
		
		if(str.indexOf(subStr, str.length() - subStr.length())==-1)
			return false;
		return true ;
	}
/*	private boolean isDelayImg (String imgClass)
	{
	    if(imgClass.indexOf("delayImg") != -1)
	    	return true ;
	    return false;
	} */
	
	@Override
	protected String getNormalURL(String base, String rel)
	{
		int countOfSep = 0;
		for(int k = 0 ; k< rel.length();++k)
		{
			if(rel.charAt(k)=='/')
				++countOfSep ;
		}
		
		int j = 0;
		for(j = base.length() - 1 ; j>=0 ; --j)
		{
		    if(base.charAt(j)=='/')
		    {
		    	--countOfSep ;
		    	if(countOfSep == 0)
		    		break ;
		    }
		}
		
		if(j>=0)
		{
			return base.substring(0, j) + rel ;
		}
		
		return base ;	
	}
}