package Bingo.spider ;

import java.util.LinkedList;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;


public interface VideoWebsiteFilterInterface {
	NodeFilter getLinksFilter();
	NodeFilter getInfoFilter();
	VideoInfo getVideoInfo(Parser parser, String linkUrl, String imgUrl) throws ParserException;
	String getEnterPointURL();	
	NodeList getNodeList(Parser parser) throws ParserException;
}