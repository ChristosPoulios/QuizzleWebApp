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
 * Servlet implementation class QuizServlet Handles quiz gameplay functionality
 * in the web application.
 */
@WebServlet("/quiz")
public class QuizServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public QuizServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		if (session.getAttribute("currentSession") != null) {
			saveCurrentQuizSession(session);
		}

		initializeNewQuizSession(session);
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

		loadQuizDataForPostRequest(request);
		request.getRequestDispatcher("/quiz.jsp").forward(request, response);
	}

	private void handleSubmitAnswer(HttpServletRequest request, HttpSession session) {
		String selectedAnswerIds = request.getParameter("selectedAnswer");

		if (selectedAnswerIds == null || selectedAnswerIds.trim().isEmpty()) {
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

			String[] answerIdArray = selectedAnswerIds.split(",");
			ArrayList<AnswerDTO> allAnswers = dataManager.getAnswersFor(currentQuestion);
			ArrayList<AnswerDTO> selectedAnswers = new ArrayList<>();
			ArrayList<AnswerDTO> correctAnswers = new ArrayList<>();

			for (AnswerDTO answer : allAnswers) {
				if (answer.isCorrect()) {
					correctAnswers.add(answer);
				}
			}

			for (String answerIdStr : answerIdArray) {
				for (AnswerDTO answer : allAnswers) {
					if (String.valueOf(answer.getId()).equals(answerIdStr.trim())) {
						selectedAnswers.add(answer);
						break;
					}
				}
			}

			if (!selectedAnswers.isEmpty()) {
				boolean isCorrect = false;

				if (correctAnswers.size() == 1) {
					isCorrect = selectedAnswers.size() == 1 && selectedAnswers.get(0).isCorrect();
				} else {
					if (selectedAnswers.size() == correctAnswers.size()) {
						isCorrect = true;
						for (AnswerDTO selectedAnswer : selectedAnswers) {
							if (!selectedAnswer.isCorrect()) {
								isCorrect = false;
								break;
							}
						}
						if (isCorrect) {
							for (AnswerDTO correctAnswer : correctAnswers) {
								boolean found = false;
								for (AnswerDTO selectedAnswer : selectedAnswers) {
									if (selectedAnswer.getId() == correctAnswer.getId()) {
										found = true;
										break;
									}
								}
								if (!found) {
									isCorrect = false;
									break;
								}
							}
						}
					}
				}

				if (currentSession.getUserAnswers() == null) {
					currentSession.setUserAnswers(new ArrayList<>());
				}

				for (AnswerDTO selectedAnswer : selectedAnswers) {
					UserAnswerDTO userAnswer = new UserAnswerDTO();
					userAnswer.setQuestionId(currentQuestion.getId());
					userAnswer.setSelectedAnswerId(selectedAnswer.getId());
					userAnswer.setCorrect(isCorrect);
					currentSession.getUserAnswers().add(userAnswer);
				}

				int currentScore = (Integer) session.getAttribute("currentScore");
				int questionsAnswered = (Integer) session.getAttribute("questionsAnswered");

				if (isCorrect) {
					currentScore++;
					if (correctAnswers.size() == 1) {
						session.setAttribute("msg", "Richtig! Score: " + currentScore + "/" + (questionsAnswered + 1));
					} else {
						session.setAttribute("msg",
								"Richtig! Alle " + correctAnswers.size() + " richtigen Antworten gewählt. Score: "
										+ currentScore + "/" + (questionsAnswered + 1));
					}
					session.setAttribute("msgType", "success");
				} else {
					if (correctAnswers.size() == 1) {
						session.setAttribute("msg", "Falsch! Die richtige Antwort war markiert. Score: " + currentScore
								+ "/" + (questionsAnswered + 1));
					} else {
						session.setAttribute("msg", "Falsch! Es gab " + correctAnswers.size()
								+ " richtige Antworten. Score: " + currentScore + "/" + (questionsAnswered + 1));
					}
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
				ArrayList<AnswerDTO> correctAnswers = new ArrayList<>();

				for (AnswerDTO answer : answers) {
					if (answer.isCorrect()) {
						correctAnswers.add(answer);
					}
				}

				if (!correctAnswers.isEmpty()) {
					if (correctAnswers.size() == 1) {
						session.setAttribute("msg",
								"Die richtige Antwort ist: " + correctAnswers.get(0).getAnswerText());
					} else {
						StringBuilder msgBuilder = new StringBuilder("Die richtigen Antworten sind: ");
						for (int i = 0; i < correctAnswers.size(); i++) {
							if (i > 0) {
								if (i == correctAnswers.size() - 1) {
									msgBuilder.append(" und ");
								} else {
									msgBuilder.append(", ");
								}
							}
							msgBuilder.append(correctAnswers.get(i).getAnswerText());
						}
						session.setAttribute("msg", msgBuilder.toString());
					}
					session.setAttribute("msgType", "info");
					session.setAttribute("answerShown", true);
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

			saveCurrentQuizSession(session);

			session.removeAttribute("currentQuestion");
			session.removeAttribute("currentSession");
			session.removeAttribute("answerSubmitted");
			session.removeAttribute("answerShown");
			session.setAttribute("currentScore", 0);
			session.setAttribute("questionsAnswered", 0);

			QuizSessionDTO newSession = new QuizSessionDTO();
			newSession.setTimestamp(new Date());
			newSession.setUserId(1);
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

				ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
				for (ThemeDTO theme : themes) {
					if (selectedTheme.equals(theme.getThemeTitle())) {
						nextQuestion = dataManager.getRandomQuestionFor(theme);
						break;
					}
				}
			} else {

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

			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			request.setAttribute("themes", themes);

			loadNextQuestion(request, session);

			request.setAttribute("currentScore", session.getAttribute("currentScore"));
			request.setAttribute("questionsAnswered", session.getAttribute("questionsAnswered"));
			request.setAttribute("selectedTheme", session.getAttribute("selectedTheme"));

		} catch (Exception e) {
			HttpSession session = request.getSession();
			session.setAttribute("msg", "Fehler beim Laden der Quiz-Daten: " + e.getMessage());
			session.setAttribute("msgType", "error");
		}
	}

	/**
	 * Saves the current quiz session to persistent storage
	 */
	private void saveCurrentQuizSession(HttpSession session) {
		try {
			QuizSessionDTO currentSession = (QuizSessionDTO) session.getAttribute("currentSession");

			if (currentSession != null && currentSession.getUserAnswers() != null
					&& !currentSession.getUserAnswers().isEmpty()) {
				DataManager dataManager = DataManager.getInstance();
				String result = dataManager.saveQuizSession(currentSession);

				if (result != null && !result.isEmpty()) {

					System.out.println("Quiz session saved: " + result);
				}
			}
		} catch (Exception e) {
			System.err.println("Fehler beim Speichern der Quiz-Session: " + e.getMessage());
		}
	}

	/**
	 * Initializes a new quiz session, resetting all relevant attributes in the
	 * session.
	 */
	private void initializeNewQuizSession(HttpSession session) {
		session.removeAttribute("currentQuestion");
		session.removeAttribute("currentSession");
		session.removeAttribute("answerSubmitted");
		session.removeAttribute("answerShown");
		session.setAttribute("currentScore", 0);
		session.setAttribute("questionsAnswered", 0);

		QuizSessionDTO newSession = new QuizSessionDTO();
		newSession.setTimestamp(new Date());
		newSession.setUserId(1);
		newSession.setUserAnswers(new ArrayList<>());
		session.setAttribute("currentSession", newSession);
	}

	/**
	 * Loads quiz data for POST requests, preserving the session state and
	 * attributes.
	 */
	private void loadQuizDataForPostRequest(HttpServletRequest request) {
		try {
			DataManager dataManager = DataManager.getInstance();
			HttpSession session = request.getSession();

			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			request.setAttribute("themes", themes);

			QuestionDTO currentQuestion = (QuestionDTO) session.getAttribute("currentQuestion");
			if (currentQuestion != null) {
				request.setAttribute("currentQuestion", currentQuestion);
			}

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