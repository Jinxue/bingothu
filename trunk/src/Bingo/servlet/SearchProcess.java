package Bingo.servlet;

import java.io.IOException;
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
		// IndexConstant.indexDir = root + config.getInitParameter("indexDir");

		// IndexConstant.dataDir = root + config.getInitParameter("dataDir");

		IndexConstant.indexDir = "E:\\Eclipse_workespace-jee\\Bingo\\index";
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

}
