package servlets;

import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import persistence.DataManager;
import quizlogic.dto.QuizSessionDTO;
import quizlogic.dto.ThemeDTO;
import quizlogic.dto.QuestionDTO;

/**
 * Servlet implementation class StatisticsServlet
 * Handles statistics display for quiz sessions in the web application.
 */
@WebServlet("/statisticsServlet")
public class StatisticsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public StatisticsServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		loadStatistics(request);
		request.getRequestDispatcher("/statistics.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Handle any POST actions if needed
		loadStatistics(request);
		request.getRequestDispatcher("/statistics.jsp").forward(request, response);
	}

	private void loadStatistics(HttpServletRequest request) {
		try {
			DataManager dataManager = DataManager.getInstance();
			
			// Load recent quiz sessions (like original Quizzle app)
			ArrayList<QuizSessionDTO> recentSessions = dataManager.getRecentQuizSessions(10);
			request.setAttribute("recentSessions", recentSessions);
			
			// Load all themes for statistics
			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			request.setAttribute("themes", themes);
			
			// Calculate basic statistics
			int totalThemes = themes.size();
			int totalQuestions = 0;
			
			for (ThemeDTO theme : themes) {
				ArrayList<QuestionDTO> questions = dataManager.getQuestionsFor(theme);
				if (questions != null) {
					totalQuestions += questions.size();
				}
			}
			
			request.setAttribute("totalThemes", totalThemes);
			request.setAttribute("totalQuestions", totalQuestions);
			request.setAttribute("totalSessions", recentSessions.size());
			
		} catch (Exception e) {
			HttpSession session = request.getSession();
			session.setAttribute("msg", "Fehler beim Laden der Statistiken: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}
}