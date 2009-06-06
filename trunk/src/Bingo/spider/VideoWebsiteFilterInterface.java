package Bingo.spider ;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;


public abstract class VideoWebsiteFilterInterface {
	protected NodeFilter linkFilter;
	protected NodeFilter infoFilter; 
	
	public NodeFilter getLinksFilter()
	{
		return linkFilter;
	}
	
	public NodeFilter getInfoFilter()
	{
		return infoFilter;
	}
	
	abstract VideoInfo getVideoInfo(Parser parser, String linkUrl, String imgUrl) throws ParserException;
	
	abstract String getEnterPointURL();	
	
	/**
	 * Get the video link together with the url of image
	 * @param baseURL
	 * @param parser
	 * @return
	 * @throws ParserException
	 */
	public Map<String,String> getLinkAndImgLink(String baseURL, Parser parser) throws ParserException
	{
		Map<String, String>  linkToImgLink = new HashMap<String, String>();
		
		NodeList nodes = parser.parse(linkFilter);
		String  link, imgLink = null ;
		
//		System.out.println(nodes.size());
		
		for(int i=0;i<nodes.size();++i)
		{
//			System.out.println(nodes.elementAt(i).toHtml());
			link = ((TagNode)nodes.elementAt(i)).getAttribute("href");
		
			//Turn the relative url to normal url
			if(link!=null && link.charAt(0)=='/')
			{
				link = getNormalURL(baseURL,link);
			}
			
		    NodeList childrenList = nodes.elementAt(i).getChildren();
		    for(int j=0;j<childrenList.size();++j)
		    {
		    	Node child = childrenList.elementAt(j);
		    	if(child instanceof TagNode && ((TagNode)child).getTagName().compareToIgnoreCase("img")==0)
		    	{
		    		imgLink = getImgLinkFromImgTag((TagNode)child);
		    		if(imgLink != null)
		    			break;
		    	}
		    	
		    }	
		    if(link != null && imgLink != null)
		    	linkToImgLink.put(link,imgLink);
		    
		}
		return linkToImgLink ;
	}
	
	protected String getImgLinkFromImgTag(TagNode tagNode)
	{
		return tagNode.getAttribute("src");		
	}
	
	/**
	 * Subclasses should override this method if needed
	 * @param base
	 * @param rel
	 * @return
	 */
	protected String getNormalURL(String base, String rel)
	{
		return rel ;
	}
}