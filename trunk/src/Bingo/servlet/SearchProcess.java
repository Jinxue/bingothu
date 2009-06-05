package Bingo.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Bingo.search.SearchManager;
import Bingo.search.SearchResultBean;

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
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String searchWord = request.getParameter("searchWord");
		SearchManager searchManager = new SearchManager(searchWord);
		ArrayList<SearchResultBean> searchResult = null;
	    searchResult = searchManager.search();
	    RequestDispatcher dispatcher = request.getRequestDispatcher("search.jsp");
	    request.setAttribute("searchResult",searchResult);
	    dispatcher.forward(request, response);
	}

}
