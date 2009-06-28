package Bingo.spider;

import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;


public class Downloader implements Runnable{
	
	static String dataBufferPath = "E:\\htmlBuffer";
	
	static Map<String,String> tempHtmlFileName = new HashMap<String , String>();
	
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

	public  boolean storeHtmlData(String urlStr)
	{
		try {
			//���Դ
			URL url = new URL (urlStr);		
			
			URLConnection u = url.openConnection();
			if(u == null)
				return false;
			
			//��ȡ��ҳ�����ʽ
			String contentType = u.getHeaderField("Content-type");	
			int i = contentType.indexOf("charset=");
			String encodingType = null;
			if(i == -1)
			{
				if(urlStr.indexOf("tudou") != -1) //�v�����ҳĬ����gbk
					encodingType = "GBK";
				else if(urlStr.indexOf("youku") != -1 || urlStr.indexOf("yokoo")!=-1) //�ſ�Ĭ����utf-8
					encodingType = "UTF-8";
				else
					return false;
			}
			else
			    encodingType = contentType.substring(contentType.indexOf("charset=")+8);
			
//			System.out.println(encodingType);
			
			InputStream in = u.getInputStream();			
			
			BufferedReader br = new BufferedReader(new InputStreamReader(in,encodingType));
			
			//�����ļ���
			File folder = new File(dataBufferPath); 
			if(!folder.exists())
			{
				boolean bDir = folder.mkdir();
				if(!bDir)
					return false;
			}
			
			//�����ļ���
			String tempName = getTempPath(urlStr);	
			
			PrintWriter outer = new PrintWriter(
					                new BufferedWriter(
					                	new OutputStreamWriter(
					                			new FileOutputStream(tempName),encodingType)));			
			String line ;
			while((line=br.readLine())!=null)
			{
//				System.out.println(line);
				outer.write(line);
			}
			outer.flush();
			outer.close();
			br.close();
//			System.out.println("OK!");
			
			//����{����ļ��б���
			synchronized(tempHtmlFileName){
				tempHtmlFileName.put(tempName, urlStr);
			}
//			System.out.println("finish download url :"+urlStr);
			
			
		} catch (MalformedURLException e) {
			System.err.println(urlStr);
			
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println(urlStr);
			
			e.printStackTrace();
			return false;
		} catch(Exception e){
			System.err.println(urlStr);
			
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
			{
			    for(String s : tempHtmlFileName.keySet())
			    {			    	
			    	return s ;
			    }
			}
		}
		return null;
	}
	
	public static boolean eraseHtmlData(String tempPath)
	{
		tempHtmlFileName.remove(tempPath);
		return new File(tempPath).delete();
	}
	
	public static String getTempPath(String urlStr)
	{
/*		String tempName = urlStr.replace(':', '~');
		tempName = tempName.replace('/', '$');
		tempName = dataBufferPath+"\\"+tempName+".txt";	 */
		String fileName = dataBufferPath+"\\"+getRandomString(8)+".txt";
		while(tempHtmlFileName.containsKey(fileName))
			fileName = dataBufferPath+"\\"+getRandomString(8)+".txt";
		
		
//		tempHtmlFileName.put(fileName, urlStr);
		
		return fileName;
	}
	
	public static String getUrlFromPath(String pathStr)
	{
/*		String url = pathStr.substring(pathStr.lastIndexOf('\\')+1 , pathStr.lastIndexOf('.'));
		url = url.replace('~', ':');
		url = url.replace('$', '/');
		return url;  */
		return tempHtmlFileName.get(pathStr);
	}
	
	protected static String getRandomString (int len)
	{
		String strTable = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		char [] buf = new char[len];
		Random random = new Random();
		
		for(int i =0 ;i<len ; ++i)
		{
			buf[i]= strTable.charAt(random.nextInt(strTable.length()-1));
		}
		
		return new String(buf);
	}
	
	public static void main(String [] args){
		new Downloader().storeHtmlData("http://www.tudou.com/playlist/id/6260180/");
//		System.out.println(eraseHtmlData(readHtmlData()));
		
//		Spider spider = new Spider();
//		VideoWebsiteFilterInterface youkuFilter = new YoukuFilter();
		
		
		/*		String url = readHtmlData();
		System.out.println(url);
		System.out.println(getUrlFromPath(url));
	    try {
	    	
			spider.parseHtml(youkuFilter, url);
			
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(url);
			
			e.printStackTrace();
		}
		URL url;
		try {
			url = new URL("http://www.tudou.com/");
			try {
				URLConnection c = url.openConnection();
				
				System.out.println(c.getHeaderField("Content-type"));
				System.out.println(c.getContentType());
				System.out.println(c.getContentEncoding());
				
				System.out.println(c.getHeaderField("Content-type").indexOf("charset="));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
}