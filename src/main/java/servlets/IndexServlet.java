package servlets;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class IndexServlet Handles CRUD operations for quiz
 * themes in the web application.
 */
@WebServlet("/index")
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public IndexServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (request.getParameter("quiz") != null) {
			response.sendRedirect("quiz");
		} else if (request.getParameter("question") != null) {
			response.sendRedirect("question");
		} else if (request.getParameter("theme") != null) {
			response.sendRedirect("theme");
		} else if (request.getParameter("statistic") != null) {
			response.sendRedirect("statistics");
		} else {
			RequestDispatcher disp = request.getRequestDispatcher("index.jsp");
			disp.forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (request.getParameter("quiz") != null) {
			response.sendRedirect("quiz");
		} else if (request.getParameter("question") != null) {
			response.sendRedirect("question");
		} else if (request.getParameter("theme") != null) {
			response.sendRedirect("theme");
		} else if (request.getParameter("statistic") != null) {
			response.sendRedirect("statistics");
		} else {
			request.getRequestDispatcher("index.jsp").forward(request, response);
		}
	}
}