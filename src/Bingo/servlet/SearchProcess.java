package Bingo.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Bingo.index.IndexConstant;
import Bingo.search.SearchManager;
import Bingo.spider.Spider;
import Bingo.spider.VideoInfo;

/**
 * Servlet implementation class SearchProcess
 */
public class SearchProcess extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private boolean getHotVideoFinished = false;
	
	private String recentHotXML;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchProcess() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		// directory = getServletContext().getRealPath("/");
		// Set the directory for data and index
		String root = config.getServletContext().getRealPath("/");

		IndexConstant.indexDir = root + config.getInitParameter("indexDir");
		IndexConstant.dataDir = root + config.getInitParameter("dataDir");

//		IndexConstant.indexDir = "E:\\Eclipse_workespace-jee\\Bingo\\index";
//		IndexConstant.dataDir = "E:\\Eclipse_workespace-jee\\Bingo\\data";
		
		recentHotXML = null;

		Timer timer = new Timer();
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				try {
					getHotVideos();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		timer.schedule(tt, 1, IndexConstant.hotVideoPeriod);
	}
	
	private synchronized void setFlag(boolean flag){
		getHotVideoFinished = flag;
	}

	private void getHotVideos() throws IOException{
		setFlag(false);
		System.out.println(System.getProperty("user.dir"));
		File file = new File(IndexConstant.dataDir, "hots.xml");
		if(file.exists())
			return;
		FileOutputStream fos = new FileOutputStream(file);
		// DataOutputStream outs = new DataOutputStream(
		// new FileOutputStream(file));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos,
				"UTF-8"));
		Spider spider = new Spider();
		String youkuContent = new String();
		String tudouContent = new String();
		String xmlContent = new String();
		Map<String, ArrayList<VideoInfo>> hotVideos = spider.getHotVideos();
		// Return the hot videos
		for (String source : hotVideos.keySet()) {
			if (source.equals("Youku")) {
				for (VideoInfo item : hotVideos.get(source))
					youkuContent += item.toXML();
			} else if (source.equals("Tudou"))
				for (VideoInfo item : hotVideos.get(source))
					tudouContent += item.toXML();
		}
		xmlContent += "<youku>" + youkuContent + "</youku><tudou>"
		+ tudouContent + "</tudou>";
		bw.write(xmlContent);
		bw.close();
		fos.close();
		recentHotXML = xmlContent;
		setFlag(true);
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String searchWord = new String(request.getParameter("searchWord")
				.getBytes("ISO-8859-1"), "UTF-8");

		SearchManager searchManager = null;
		ArrayList<VideoInfo> searchResult = new ArrayList<VideoInfo>();

		if (!searchWord.isEmpty()) {
			try {
				searchManager = new SearchManager(searchWord);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				searchResult = searchManager.search();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		request.setAttribute("searchResult", searchResult);
		RequestDispatcher dispatcher = request
				.getRequestDispatcher("search.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		// String searchWord = new String(request.getParameter("searchWord")
		// .getBytes("ISO-8859-1"), "UTF-8");

		String searchWord = request.getParameter("searchWord");
		SearchManager searchManager = null;
		ArrayList<VideoInfo> searchResult = new ArrayList<VideoInfo>();

		String xmlContent = new String("<?xml version='1.0' encoding='UTF-8'?>"
				.getBytes(), "UTF-8");
		String youkuContent = new String();
		String tudouContent = new String();
		response.setContentType("text/xml;charset=UTF-8");
		PrintWriter out = response.getWriter();

		// return the data in XML format
		// Also write it to a file

		xmlContent += "<items>";
		if (searchWord.isEmpty()) {
			File file = new File(IndexConstant.dataDir, "hots.xml");
			if(recentHotXML == null && file.exists()){
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis,
					"UTF-8"));
//				xmlContent += br.toString();
				String inputLine;
				while ((inputLine = br.readLine()) != null) {
					recentHotXML += inputLine;
				}
			}
			xmlContent += recentHotXML;
		} else {
			try {
				searchManager = new SearchManager(searchWord);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				searchResult = searchManager.search();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// String content = null;
			if (!searchResult.isEmpty()) {
				for (VideoInfo item : searchResult) {
					if (item.getSource().equals("Youku"))
						youkuContent += item.toXML();
					else if (item.getSource().equals("Tudou"))
						tudouContent += item.toXML();
				}
			}
			xmlContent += "<youku>" + youkuContent + "</youku><tudou>"
			+ tudouContent + "</tudou>";
		}
		xmlContent += "</items>";
		out.print(xmlContent);
		out.flush();
		out.close();
	}
}
