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

		if (selectedTheme == null || selectedTheme.trim().isEmpty()) {
			session.setAttribute("msg", "Bitte wählen Sie ein Thema aus!");
			session.setAttribute("msgType", "error");
			return;
		}

		try {
			DataManager dataManager = DataManager.getInstance();
			
			// Finde das ThemeDTO-Objekt für das ausgewählte Thema
			ThemeDTO targetTheme = null;
			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			for (ThemeDTO theme : themes) {
				if (selectedTheme.equals(theme.getThemeTitle())) {
					targetTheme = theme;
					break;
				}
			}
			
			if (targetTheme == null) {
				session.setAttribute("msg", "Das ausgewählte Thema konnte nicht gefunden werden!");
				session.setAttribute("msgType", "error");
				return;
			}
			
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

			// Verwende die saveQuestion Methode mit Thema-Parameter
			String result = dataManager.saveQuestion(question, targetTheme);
			if (result != null && result.contains("successfully")) {
				// Setze die gespeicherte Frage als aktuell bearbeitete Frage
				session.setAttribute("currentEditingQuestion", titel.trim());
				
				session.setAttribute("msg", "Frage '" + titel + "' erfolgreich gespeichert!");
				session.setAttribute("msgType", "success");

				// Behalte die Daten im Formular nach erfolgreichem Speichern
				request.setAttribute("currentThema", selectedTheme);
				request.setAttribute("currentTitel", titel);
				request.setAttribute("currentFrage", frage);
				request.setAttribute("selectedQuestionTitle", titel); // Markiere die gespeicherte Frage in der Liste
				for (int i = 1; i <= 4; i++) {
					request.setAttribute("antwort" + i, request.getParameter("antwort" + i));
					if (request.getParameter("richtig" + i) != null) {
						request.setAttribute("richtig" + i, true);
					}
				}
			} else {
				session.setAttribute("msg", "Fehler beim Speichern: " + (result != null ? result : "Unbekannter Fehler"));
				session.setAttribute("msgType", "error");
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
			
			// Finde die Frage durch Durchsuchen aller Themen (wie im ursprünglichen Quizzle-Projekt)
			QuestionDTO foundQuestion = null;
			String foundThemeTitle = null;
			
			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			for (ThemeDTO theme : themes) {
				ArrayList<QuestionDTO> questions = dataManager.getQuestionsFor(theme);
				if (questions != null) {
					for (QuestionDTO question : questions) {
						if (selectedQuestion.equals(question.getQuestionTitle())) {
							foundQuestion = question;
							foundThemeTitle = theme.getThemeTitle();
							break;
						}
					}
					if (foundQuestion != null) break;
				}
			}
			
			if (foundQuestion != null) {
				// Lade die Antworten (wie im ursprünglichen Projekt)
				ArrayList<AnswerDTO> answers = dataManager.getAnswersFor(foundQuestion);
				foundQuestion.setAnswers(answers);
				
				// Lade die Fragendaten in die Request-Attribute (wie onQuestionSelected im Original)
				request.setAttribute("currentTitel", foundQuestion.getQuestionTitle());
				request.setAttribute("currentFrage", foundQuestion.getQuestionText());
				request.setAttribute("currentThema", foundThemeTitle);
				request.setAttribute("selectedTheme", foundThemeTitle);
				request.setAttribute("selectedQuestionTitle", selectedQuestion);
				
				// Lade die Antworten in die Formularfelder
				if (answers != null) {
					for (int i = 0; i < answers.size() && i < 4; i++) {
						AnswerDTO answer = answers.get(i);
						request.setAttribute("antwort" + (i + 1), answer.getAnswerText());
						if (answer.isCorrect()) {
							request.setAttribute("richtig" + (i + 1), true);
						}
					}
				}
				
				session.setAttribute("msg", "Frage '" + selectedQuestion + "' erfolgreich geladen!");
				session.setAttribute("msgType", "success");
			} else {
				session.setAttribute("msg", "Frage '" + selectedQuestion + "' konnte nicht gefunden werden!");
				session.setAttribute("msgType", "error");
			}
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
			
			// Finde die zu löschende Frage durch Durchsuchen aller Themen
			QuestionDTO questionToDelete = null;
			
			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			for (ThemeDTO theme : themes) {
				ArrayList<QuestionDTO> questions = dataManager.getQuestionsFor(theme);
				if (questions != null) {
					for (QuestionDTO question : questions) {
						if (selectedQuestion.equals(question.getQuestionTitle())) {
							questionToDelete = question;
							break;
						}
					}
					if (questionToDelete != null) break;
				}
			}
			
			if (questionToDelete != null) {
				String result = dataManager.deleteQuestion(questionToDelete);
				if (result != null && result.contains("successfully")) {
					session.setAttribute("msg", "Frage '" + selectedQuestion + "' erfolgreich gelöscht!");
					session.setAttribute("msgType", "success");
					
					// Formular leeren nach erfolgreichem Löschen
					request.setAttribute("currentTitel", "");
					request.setAttribute("currentFrage", "");
					request.setAttribute("antwort1", "");
					request.setAttribute("antwort2", "");
					request.setAttribute("antwort3", "");
					request.setAttribute("antwort4", "");
				} else {
					session.setAttribute("msg", "Fehler beim Löschen: " + (result != null ? result : "Unbekannter Fehler"));
					session.setAttribute("msgType", "error");
				}
			} else {
				session.setAttribute("msg", "Frage '" + selectedQuestion + "' konnte nicht gefunden werden!");
				session.setAttribute("msgType", "error");
			}
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
				
				// Formular leeren bei Themenwechsel (wie im originalen Quizzle-Projekt)
				request.setAttribute("currentTitel", "");
				request.setAttribute("currentFrage", "");
				request.setAttribute("antwort1", "");
				request.setAttribute("antwort2", "");
				request.setAttribute("antwort3", "");
				request.setAttribute("antwort4", "");
				request.setAttribute("richtig1", null);
				request.setAttribute("richtig2", null);
				request.setAttribute("richtig3", null);
				request.setAttribute("richtig4", null);
				request.setAttribute("selectedQuestionTitle", "");
				
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
				
				// Formular auch leeren bei "Alle Themen"
				request.setAttribute("currentTitel", "");
				request.setAttribute("currentFrage", "");
				request.setAttribute("antwort1", "");
				request.setAttribute("antwort2", "");
				request.setAttribute("antwort3", "");
				request.setAttribute("antwort4", "");
				request.setAttribute("richtig1", null);
				request.setAttribute("richtig2", null);
				request.setAttribute("richtig3", null);
				request.setAttribute("richtig4", null);
				request.setAttribute("selectedQuestionTitle", "");
				
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
			HttpSession session = request.getSession();
			
			// Überprüfe ob ein Themenwechsel stattgefunden hat
			String previousTheme = (String) session.getAttribute("lastSelectedTheme");
			boolean themeChanged = false;
			
			if (selectedTheme != null && !selectedTheme.trim().isEmpty()) {
				themeChanged = !selectedTheme.equals(previousTheme);
				session.setAttribute("lastSelectedTheme", selectedTheme);
			} else {
				themeChanged = previousTheme != null && !previousTheme.equals("");
				session.setAttribute("lastSelectedTheme", "");
			}
			
			if (selectedTheme != null && !selectedTheme.trim().isEmpty()) {
				request.setAttribute("selectedTheme", selectedTheme);
				request.setAttribute("currentThema", selectedTheme);
				
				// Formular leeren bei Themenwechsel (wie im originalen Quizzle-Projekt)
				if (themeChanged) {
					request.setAttribute("currentTitel", "");
					request.setAttribute("currentFrage", "");
					request.setAttribute("antwort1", "");
					request.setAttribute("antwort2", "");
					request.setAttribute("antwort3", "");
					request.setAttribute("antwort4", "");
					request.setAttribute("richtig1", null);
					request.setAttribute("richtig2", null);
					request.setAttribute("richtig3", null);
					request.setAttribute("richtig4", null);
					request.setAttribute("selectedQuestionTitle", "");
				}
				
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
				
				// Formular auch leeren bei "Alle Themen"
				if (themeChanged) {
					request.setAttribute("currentTitel", "");
					request.setAttribute("currentFrage", "");
					request.setAttribute("antwort1", "");
					request.setAttribute("antwort2", "");
					request.setAttribute("antwort3", "");
					request.setAttribute("antwort4", "");
					request.setAttribute("richtig1", null);
					request.setAttribute("richtig2", null);
					request.setAttribute("richtig3", null);
					request.setAttribute("richtig4", null);
					request.setAttribute("selectedQuestionTitle", "");
				}
				
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