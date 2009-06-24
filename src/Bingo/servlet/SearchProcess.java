package Bingo.servlet;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Bingo.index.IndexConstant;
import Bingo.search.SearchManager;
import Bingo.spider.VideoInfo;

/**
 * Servlet implementation class SearchProcess
 */
public class SearchProcess extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
		 
//		IndexConstant.indexDir = root + config.getInitParameter("indexDir");
//
//		IndexConstant.dataDir = root + config.getInitParameter("dataDir");

		IndexConstant.indexDir = "E:\\Eclipse_workespace-jee\\Bingo\\index";
		IndexConstant.dataDir = "E:\\Eclipse_workespace-jee\\Bingo\\data";
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
		
//		String searchWord = new String(request.getParameter("searchWord")
//				.getBytes("ISO-8859-1"), "UTF-8");
		
		String searchWord = request.getParameter("searchWord");
		SearchManager searchManager = null;
		ArrayList<VideoInfo> searchResult = new ArrayList<VideoInfo>();
		
		if(searchWord.isEmpty())
		{
			// Do something
			searchWord = "中国";
		}
		
		String xmlContent = new String("<?xml version='1.0' encoding='UTF-8'?>".getBytes(), "UTF-8");
		String youkuContent = new String();
		String tudouContent = new String();
//		String xmlContent = new String("");
//		xmlContent += "<?xml version='1.0' encoding='UTF-8'?>";
		
        response.setContentType("text/xml;charset=UTF-8");   
        PrintWriter out = response.getWriter();   

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

			// return the data in XML format
			// Also write it to a file
			System.out.println(System.getProperty("user.dir"));
			File file = new File(IndexConstant.dataDir, "results.xml");
			FileOutputStream fos = new FileOutputStream(file);
//			DataOutputStream outs = new DataOutputStream(
//		               new FileOutputStream(file));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));   
			
			xmlContent += "<items>";
			
//			String content = null;
			if(!searchResult.isEmpty()){
				for(VideoInfo item: searchResult){
					if(item.getSource().equals("Youku"))
						youkuContent += 	"<item><title>" + item.getTitle() + 
									"</title><keyword>" + item.getKeyWord() +
									"</keyword><description>" + item.getDescription() +
									"</description><imageurl>" + item.getImgUrl() +
									"</imageurl><url>" + item.getUrl() +
//									"</url><source>" + item.getSource() + 
//									"</source></item>";
									"</url></item>";
					else if(item.getSource().equals("Tudou"))
						tudouContent += 	"<item><title>" + item.getTitle() + 
						"</title><keyword>" + item.getKeyWord() +
						"</keyword><description>" + item.getDescription() +
						"</description><imageurl>" + item.getImgUrl() +
						"</imageurl><url>" + item.getUrl() +
//						"</url><source>" + item.getSource() + 
//						"</source></item>";
						"</url></item>";

				}
			}
			
			xmlContent += "<youku>" + youkuContent + "</youku><tudou>"
							+ tudouContent + "</tudou></items>";
			
//			outs.writeUTF(xmlContent);
			bw.write(xmlContent);
			
			out.print(xmlContent);
			out.flush();
			out.close();
			
//			outs.close();
			bw.close();
			fos.close();
		}
	}
}
