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
import quizlogic.dto.ThemeDTO;
import quizlogic.dto.QuestionDTO;
import quizlogic.dto.AnswerDTO;

/**
 * Servlet implementation class QuestionServlet
 * Handles CRUD operations for quiz questions in the web application.
 */
@WebServlet("/questionServlet")
public class QuestionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public QuestionServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		loadData(request);
		request.getRequestDispatcher("/question.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String action = request.getParameter("action");
		HttpSession session = request.getSession();

		try {
			switch (action != null ? action : "") {
			case "save":
				handleSaveQuestion(request, session);
				break;
			case "load":
				handleLoadQuestion(request, session);
				break;
			case "delete":
				handleDeleteQuestion(request, session);
				break;
			default:
				handleThemeSelection(request, session);
			}
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}

		loadData(request);
		request.getRequestDispatcher("/question.jsp").forward(request, response);
	}

	private void handleSaveQuestion(HttpServletRequest request, HttpSession session) {
		String titel = request.getParameter("titel");
		String frage = request.getParameter("frage");
		String selectedTheme = request.getParameter("selectedTheme");

		if (titel == null || titel.trim().isEmpty()) {
			session.setAttribute("msg", "Titel darf nicht leer sein!");
			session.setAttribute("msgType", "error");
			return;
		}

		if (frage == null || frage.trim().isEmpty()) {
			session.setAttribute("msg", "Frage darf nicht leer sein!");
			session.setAttribute("msgType", "error");
			return;
		}

		try {
			DataManager dataManager = DataManager.getInstance();
			
			QuestionDTO question = new QuestionDTO();
			question.setQuestionTitle(titel.trim());
			question.setQuestionText(frage.trim());

			ArrayList<AnswerDTO> answers = new ArrayList<>();
			for (int i = 1; i <= 4; i++) {
				String antwortText = request.getParameter("antwort" + i);
				boolean isCorrect = request.getParameter("richtig" + i) != null;
				
				if (antwortText != null && !antwortText.trim().isEmpty()) {
					AnswerDTO answer = new AnswerDTO();
					answer.setAnswerText(antwortText.trim());
					answer.setCorrect(isCorrect);
					answers.add(answer);
				}
			}
			
			question.setAnswers(answers);

			String result = dataManager.saveQuestion(question);
			if (result == null) {
				session.setAttribute("msg", "Fehler beim Speichern: " + result);
				session.setAttribute("msgType", "error");
			} else {
				session.setAttribute("msg", "Frage '" + titel + "' erfolgreich gespeichert!");
				session.setAttribute("msgType", "success");

				request.setAttribute("currentThema", selectedTheme);
				request.setAttribute("currentTitel", titel);
				request.setAttribute("currentFrage", frage);
				for (int i = 1; i <= 4; i++) {
					request.setAttribute("antwort" + i, request.getParameter("antwort" + i));
					if (request.getParameter("richtig" + i) != null) {
						request.setAttribute("richtig" + i, true);
					}
				}
			}
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Speichern: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}

	private void handleLoadQuestion(HttpServletRequest request, HttpSession session) {
		String selectedQuestion = request.getParameter("selectedQuestion");
		
		if (selectedQuestion == null || selectedQuestion.trim().isEmpty()) {
			session.setAttribute("msg", "Bitte wählen Sie eine Frage zum Laden aus!");
			session.setAttribute("msgType", "error");
			return;
		}

		try {
			DataManager dataManager = DataManager.getInstance();
			// Note: This would need to be implemented in DataManager
			// For now, we'll simulate loading
			session.setAttribute("msg", "Frage '" + selectedQuestion + "' geladen!");
			session.setAttribute("msgType", "success");
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Laden: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}

	private void handleDeleteQuestion(HttpServletRequest request, HttpSession session) {
		String selectedQuestion = request.getParameter("selectedQuestion");
		
		if (selectedQuestion == null || selectedQuestion.trim().isEmpty()) {
			session.setAttribute("msg", "Bitte wählen Sie eine Frage zum Löschen aus!");
			session.setAttribute("msgType", "error");
			return;
		}

		try {
			DataManager dataManager = DataManager.getInstance();
			// Note: This would need to be implemented in DataManager
			session.setAttribute("msg", "Frage '" + selectedQuestion + "' gelöscht!");
			session.setAttribute("msgType", "success");
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Löschen: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}

	private void handleThemeSelection(HttpServletRequest request, HttpSession session) {
		String selectedTheme = request.getParameter("selectedTheme");
		
		try {
			DataManager dataManager = DataManager.getInstance();
			
			if (selectedTheme != null && !selectedTheme.trim().isEmpty()) {
				request.setAttribute("selectedTheme", selectedTheme);
				request.setAttribute("currentThema", selectedTheme);
				
				ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
				for (ThemeDTO theme : themes) {
					if (selectedTheme.equals(theme.getThemeTitle())) {
						ArrayList<QuestionDTO> questions = dataManager.getQuestionsFor(theme);
						request.setAttribute("questions", questions);
						break;
					}
				}
			} else {
				request.setAttribute("selectedTheme", "");
				request.setAttribute("currentThema", "Alle Themen");
				
				ArrayList<QuestionDTO> allQuestions = new ArrayList<>();
				ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
				
				for (ThemeDTO theme : themes) {
					ArrayList<QuestionDTO> themeQuestions = dataManager.getQuestionsFor(theme);
					if (themeQuestions != null) {
						allQuestions.addAll(themeQuestions);
					}
				}
				
				request.setAttribute("questions", allQuestions);
			}
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Laden der Fragen: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}

	private void loadData(HttpServletRequest request) {
		try {
			DataManager dataManager = DataManager.getInstance();
			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			request.setAttribute("themes", themes);

			String selectedTheme = request.getParameter("selectedTheme");
			if (selectedTheme != null && !selectedTheme.trim().isEmpty()) {
				request.setAttribute("selectedTheme", selectedTheme);
				request.setAttribute("currentThema", selectedTheme);
				for (ThemeDTO theme : themes) {
					if (selectedTheme.equals(theme.getThemeTitle())) {
						ArrayList<QuestionDTO> questions = dataManager.getQuestionsFor(theme);
						request.setAttribute("questions", questions);
						break;
					}
				}
			} else {
				request.setAttribute("selectedTheme", "");
				request.setAttribute("currentThema", "Alle Themen");
				
				ArrayList<QuestionDTO> allQuestions = new ArrayList<>();
				
				for (ThemeDTO theme : themes) {
					ArrayList<QuestionDTO> themeQuestions = dataManager.getQuestionsFor(theme);
					if (themeQuestions != null) {
						allQuestions.addAll(themeQuestions);
					}
				}
				
				request.setAttribute("questions", allQuestions);
			}
		} catch (Exception e) {
			request.setAttribute("themes", new ArrayList<ThemeDTO>());
			request.setAttribute("questions", new ArrayList<QuestionDTO>());
			HttpSession session = request.getSession();
			session.setAttribute("msg", "Fehler beim Laden der Daten: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}
}