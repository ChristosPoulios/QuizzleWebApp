package servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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
import quizlogic.dto.QuizSessionDTO;
import quizlogic.dto.UserAnswerDTO;

/**
 * Servlet implementation class QuizServlet
 * Handles quiz gameplay functionality in the web application.
 */
@WebServlet("/quizServlet")
public class QuizServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public QuizServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		loadQuizData(request);
		request.getRequestDispatcher("/quiz.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String action = request.getParameter("action");
		HttpSession session = request.getSession();

		try {
			switch (action != null ? action : "") {
			case "submitAnswer":
				handleSubmitAnswer(request, session);
				break;
			case "showAnswer":
				handleShowAnswer(request, session);
				break;
			case "nextQuestion":
				handleNextQuestion(request, session);
				break;
			case "newQuiz":
				handleNewQuiz(request, session);
				break;
			default:
				handleThemeSelection(request, session);
			}
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}

		loadQuizData(request);
		request.getRequestDispatcher("/quiz.jsp").forward(request, response);
	}

	private void handleSubmitAnswer(HttpServletRequest request, HttpSession session) {
		String selectedAnswerId = request.getParameter("selectedAnswer");
		
		if (selectedAnswerId == null || selectedAnswerId.trim().isEmpty()) {
			session.setAttribute("msg", "Bitte wählen Sie eine Antwort aus!");
			session.setAttribute("msgType", "error");
			return;
		}

		try {
			DataManager dataManager = DataManager.getInstance();
			QuestionDTO currentQuestion = (QuestionDTO) session.getAttribute("currentQuestion");
			QuizSessionDTO currentSession = (QuizSessionDTO) session.getAttribute("currentSession");
			
			if (currentQuestion == null || currentSession == null) {
				session.setAttribute("msg", "Keine aktive Quiz-Session gefunden!");
				session.setAttribute("msgType", "error");
				return;
			}

			// Finde die ausgewählte Antwort
			AnswerDTO selectedAnswer = null;
			ArrayList<AnswerDTO> answers = dataManager.getAnswersFor(currentQuestion);
			for (AnswerDTO answer : answers) {
				if (String.valueOf(answer.getId()).equals(selectedAnswerId)) {
					selectedAnswer = answer;
					break;
				}
			}

			if (selectedAnswer != null) {
				// Erstelle UserAnswer
				UserAnswerDTO userAnswer = new UserAnswerDTO();
				userAnswer.setQuestionId(currentQuestion.getId());
				userAnswer.setSelectedAnswerId(selectedAnswer.getId());
				userAnswer.setCorrect(selectedAnswer.isCorrect());

				// Füge zur Session hinzu
				if (currentSession.getUserAnswers() == null) {
					currentSession.setUserAnswers(new ArrayList<>());
				}
				currentSession.getUserAnswers().add(userAnswer);

				// Aktualisiere Score
				int currentScore = (Integer) session.getAttribute("currentScore");
				int questionsAnswered = (Integer) session.getAttribute("questionsAnswered");
				
				if (selectedAnswer.isCorrect()) {
					currentScore++;
					session.setAttribute("msg", "Richtig! Score: " + currentScore + "/" + (questionsAnswered + 1));
					session.setAttribute("msgType", "success");
				} else {
					session.setAttribute("msg", "Falsch! Die richtige Antwort war markiert. Score: " + currentScore + "/" + (questionsAnswered + 1));
					session.setAttribute("msgType", "error");
				}

				session.setAttribute("currentScore", currentScore);
				session.setAttribute("questionsAnswered", questionsAnswered + 1);
				session.setAttribute("answerSubmitted", true);
			}

		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Auswerten der Antwort: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}

	private void handleShowAnswer(HttpServletRequest request, HttpSession session) {
		try {
			DataManager dataManager = DataManager.getInstance();
			QuestionDTO currentQuestion = (QuestionDTO) session.getAttribute("currentQuestion");
			
			if (currentQuestion != null) {
				ArrayList<AnswerDTO> answers = dataManager.getAnswersFor(currentQuestion);
				for (AnswerDTO answer : answers) {
					if (answer.isCorrect()) {
						session.setAttribute("msg", "Die richtige Antwort ist: " + answer.getAnswerText());
						session.setAttribute("msgType", "info");
						session.setAttribute("answerShown", true);
						break;
					}
				}
			}
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Anzeigen der Antwort: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}

	private void handleNextQuestion(HttpServletRequest request, HttpSession session) {
		try {
			loadNextQuestion(request, session);
			session.removeAttribute("answerSubmitted");
			session.removeAttribute("answerShown");
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Laden der nächsten Frage: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}

	private void handleNewQuiz(HttpServletRequest request, HttpSession session) {
		try {
			// Session zurücksetzen
			session.removeAttribute("currentQuestion");
			session.removeAttribute("currentSession");
			session.removeAttribute("answerSubmitted");
			session.removeAttribute("answerShown");
			session.setAttribute("currentScore", 0);
			session.setAttribute("questionsAnswered", 0);

			// Neue Session starten
			QuizSessionDTO newSession = new QuizSessionDTO();
			newSession.setTimestamp(new Date());
			newSession.setUserId(1); // Default user
			newSession.setUserAnswers(new ArrayList<>());
			session.setAttribute("currentSession", newSession);

			loadNextQuestion(request, session);
			session.setAttribute("msg", "Neues Quiz gestartet!");
			session.setAttribute("msgType", "success");
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Starten eines neuen Quiz: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}

	private void handleThemeSelection(HttpServletRequest request, HttpSession session) {
		String selectedTheme = request.getParameter("selectedTheme");
		session.setAttribute("selectedTheme", selectedTheme);
		loadNextQuestion(request, session);
	}

	private void loadNextQuestion(HttpServletRequest request, HttpSession session) {
		try {
			DataManager dataManager = DataManager.getInstance();
			String selectedTheme = (String) session.getAttribute("selectedTheme");
			QuestionDTO nextQuestion = null;

			if (selectedTheme != null && !selectedTheme.trim().isEmpty()) {
				// Lade Frage für spezifisches Thema
				ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
				for (ThemeDTO theme : themes) {
					if (selectedTheme.equals(theme.getThemeTitle())) {
						nextQuestion = dataManager.getRandomQuestionFor(theme);
						break;
					}
				}
			} else {
				// Lade zufällige Frage
				nextQuestion = dataManager.getRandomQuestion();
			}

			if (nextQuestion != null) {
				ArrayList<AnswerDTO> answers = dataManager.getAnswersFor(nextQuestion);
				nextQuestion.setAnswers(answers);
				session.setAttribute("currentQuestion", nextQuestion);
				request.setAttribute("currentQuestion", nextQuestion);
			} else {
				session.setAttribute("msg", "Keine Fragen verfügbar für das ausgewählte Thema!");
				session.setAttribute("msgType", "warning");
			}

		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Laden der Frage: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}

	private void loadQuizData(HttpServletRequest request) {
		try {
			DataManager dataManager = DataManager.getInstance();
			HttpSession session = request.getSession();
			
			// Lade Themen
			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			request.setAttribute("themes", themes);

			// Initialisiere Session-Daten falls nicht vorhanden
			if (session.getAttribute("currentScore") == null) {
				session.setAttribute("currentScore", 0);
			}
			if (session.getAttribute("questionsAnswered") == null) {
				session.setAttribute("questionsAnswered", 0);
			}
			if (session.getAttribute("currentSession") == null) {
				QuizSessionDTO newSession = new QuizSessionDTO();
				newSession.setTimestamp(new Date());
				newSession.setUserId(1); // Default user
				newSession.setUserAnswers(new ArrayList<>());
				session.setAttribute("currentSession", newSession);
			}

			// Lade aktuelle Frage falls nicht vorhanden
			if (session.getAttribute("currentQuestion") == null) {
				loadNextQuestion(request, session);
			} else {
				request.setAttribute("currentQuestion", session.getAttribute("currentQuestion"));
			}

			// Setze Attribute für JSP
			request.setAttribute("currentScore", session.getAttribute("currentScore"));
			request.setAttribute("questionsAnswered", session.getAttribute("questionsAnswered"));
			request.setAttribute("selectedTheme", session.getAttribute("selectedTheme"));

		} catch (Exception e) {
			HttpSession session = request.getSession();
			session.setAttribute("msg", "Fehler beim Laden der Quiz-Daten: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}
}