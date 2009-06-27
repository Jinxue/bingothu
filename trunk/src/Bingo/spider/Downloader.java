package Bingo.spider;

import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.lucene.index.CorruptIndexException;


public class Downloader implements Runnable{
	
	static String dataBufferPath = "E:\\htmlBuffer";
	
	static LinkedList<String> tempHtmlFileName = new LinkedList<String>();
	
	public void run(){
		int timeOutNum = 0;
		while(true)
		{
			String nextUrl = Spider.getNextUrl();
			if(nextUrl == null)
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
//			    System.out.println("begin to download :" + nextUrl);
				
				timeOutNum = 0;
				storeHtmlData(nextUrl);
			}
				
		}
	}

	public static boolean storeHtmlData(String urlStr)
	{
		try {
			//数据源
			URL url = new URL (urlStr);
			InputStream in = url.openStream();
//			in = new BufferedInputStream(in);
			BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			
			//缓冲文件夹
			File folder = new File(dataBufferPath); 
			if(!folder.exists())
			{
				boolean bDir = folder.mkdir();
				if(!bDir)
					return false;
			}
			
			//缓冲文件名
			String tempName = getTempPath(urlStr);	
			
			PrintWriter outer = new PrintWriter(
		//			                new BufferedWriter(
					                	new OutputStreamWriter(
					                			new FileOutputStream(tempName),"UTF-8"));			
			String line ;
			while((line=br.readLine())!=null)
			{
	//			System.out.println(line);
				outer.write(line);
			}
			outer.flush();
			outer.close();
			br.close();
//			System.out.println("OK!");
			
			//添加至缓存文件列表中
			synchronized(tempHtmlFileName){
				tempHtmlFileName.add(tempName);
			}
//			System.out.println("finish download url :"+urlStr);
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * get the name of the html file which has been buffered
	 * @return
	 */
	public static String readHtmlData()
	{
		synchronized(tempHtmlFileName){
			if(tempHtmlFileName.size()==0)
				return null;
			else
				return tempHtmlFileName.removeFirst();
		}
	}
	
	public static boolean eraseHtmlData(String tempPath)
	{
		return new File(tempPath).delete();
	}
	
	public static String getTempPath(String urlStr)
	{
		String tempName = urlStr.replace(':', '~');
		tempName = tempName.replace('/', '$');
		tempName = dataBufferPath+"\\"+tempName+".txt";	
		
		return tempName;
	}
	
	public static String getUrlFromPath(String pathStr)
	{
		String url = pathStr.substring(pathStr.lastIndexOf('\\')+1 , pathStr.lastIndexOf('.'));
		url = url.replace('~', ':');
		url = url.replace('$', '/');
		return url;
	}
	
	public static void main(String [] args){
		Downloader.storeHtmlData("http://v.youku.com/v_show/id_XNTkzMjY4NDQ=.html");
//		System.out.println(eraseHtmlData(readHtmlData()));
		
//		Spider spider = new Spider();
//		VideoWebsiteFilterInterface youkuFilter = new YoukuFilter();
		
		
		String url = readHtmlData();
		System.out.println(url);
		System.out.println(getUrlFromPath(url));
/*	    try {
	    	
			spider.parseHtml(youkuFilter, url);
			
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(url);
			
			e.printStackTrace();
		}*/
		
	}
}