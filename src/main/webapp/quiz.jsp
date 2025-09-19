<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%@ page import="quizlogic.dto.ThemeDTO"%>
<%@ page import="quizlogic.dto.QuestionDTO"%>
<%@ page import="quizlogic.dto.AnswerDTO"%>
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>Quizzle - Quiz</title>
<link rel="stylesheet" type="text/css" href="styles.css" />
</head>
<body>
	<!-- Navigation Tabs -->
	<div class="tab-container">
		<div class="tabs">
			<a href="indexServlet" class="tab">Quizthemen</a> <a
				href="questionServlet" class="tab">Quizfragen</a> <a
				href="quizServlet" class="tab active">Quiz</a> <a
				href="statisticsServlet" class="tab">Statistik</a>
		</div>
	</div>

	<form id="quiz-form" method="post" action="quizServlet" novalidate>
		<input type="hidden" name="action" value="" id="actionInput" /> <input
			type="hidden" name="selectedAnswer" value="" id="selectedAnswerInput" />

		<div class="quiz-container">
			<!-- Left Section - Current Question -->
			<div class="left-section">
				<div class="question-display">
					<%
					QuestionDTO currentQuestion = (QuestionDTO) request.getAttribute("currentQuestion");
					if (currentQuestion != null) {
					%>
					<h2><%=currentQuestion.getQuestionTitle()%></h2>
					<div class="question-text"><%=currentQuestion.getQuestionText()%></div>

					<div class="answers-section">
						<%
						List<AnswerDTO> answers = currentQuestion.getAnswers();
						if (answers != null && !answers.isEmpty()) {
							for (int i = 0; i < answers.size(); i++) {
								AnswerDTO answer = answers.get(i);
						%>
						<div class="answer-option">
							<input type="radio" name="answer" value="<%=answer.getId()%>"
								id="answer<%=i%>" /> <label for="answer<%=i%>"><%=answer.getAnswerText()%></label>
						</div>
						<%
						}
						}
						%>
					</div>
					<%
					} else {
					%>
					<div class="no-question">
						<h2>Keine Fragen verfügbar</h2>
						<p>Bitte wählen Sie ein Thema aus oder starten Sie ein neues
							Quiz.</p>
					</div>
					<%
					}
					%>
				</div>
			</div>

			<!-- Right Section - Quiz Info and Theme Selection -->
			<div class="right-section">
				<div class="theme-selection">
					<h3>Thema auswählen</h3>
					<select name="selectedTheme" onchange="this.form.submit();">
						<option value="">Zufällige Fragen</option>
						<%
						ArrayList<ThemeDTO> themes = (ArrayList<ThemeDTO>) request.getAttribute("themes");
						String selectedTheme = (String) request.getAttribute("selectedTheme");
						if (themes != null) {
							for (ThemeDTO theme : themes) {
								boolean isSelected = selectedTheme != null && selectedTheme.equals(theme.getThemeTitle());
						%>
						<option value="<%=theme.getThemeTitle()%>"
							<%=isSelected ? "selected" : ""%>>
							<%=theme.getThemeTitle()%>
						</option>
						<%
						}
						}
						%>
					</select>
				</div>

				<div class="quiz-info">
					<h3>Quiz Information</h3>
					<div class="quiz-stats">
						<div class="stat-item">
							<span class="stat-label">Aktueller Score:</span> <span
								class="stat-value"><%=request.getAttribute("currentScore") != null ? request.getAttribute("currentScore") : "0"%></span>
						</div>
						<div class="stat-item">
							<span class="stat-label">Fragen beantwortet:</span> <span
								class="stat-value"><%=request.getAttribute("questionsAnswered") != null ? request.getAttribute("questionsAnswered") : "0"%></span>
						</div>
						<div class="stat-item">
							<span class="stat-label">Aktives Thema:</span> <span
								class="stat-value"><%=request.getAttribute("selectedTheme") != null ? request.getAttribute("selectedTheme") : "Zufällig"%></span>
						</div>
					</div>
				</div>
			</div>
		</div>

		<div id="message-area" aria-live="polite" role="status"
			<%String msgType = (String) session.getAttribute("msgType");%>
			<%=msgType != null ? "class=\"" + msgType + "\"" : ""%>>
			<%
			String msg = (String) session.getAttribute("msg");
			if (msg != null) {
			%>
			<%=msg%>
			<%
			session.removeAttribute("msg");
			session.removeAttribute("msgType");
			%>
			<%
			} else {
			%>
			Quiz bereit
			<%
			}
			%>
		</div>

		<!-- Quiz Controls - moved below message area -->
		<div class="quiz-controls">
			<button type="button" onclick="showAnswer()" id="showAnswerBtn">Antwort
				zeigen</button>
			<button type="button" onclick="submitAnswer()" id="submitAnswerBtn">Antwort
				abgeben</button>
			<button type="button" onclick="nextQuestion()" id="nextQuestionBtn">Nächste
				Frage</button>
			<button type="button" onclick="newQuiz()" id="newQuizBtn">Neues
				Quiz</button>
		</div>
	</form>

	<script>
		function submitAnswer() {
			var selectedAnswer = document
					.querySelector('input[name="answer"]:checked');
			if (!selectedAnswer) {
				alert('Bitte wählen Sie eine Antwort aus!');
				return;
			}

			document.getElementById('selectedAnswerInput').value = selectedAnswer.value;
			document.getElementById('actionInput').value = 'submitAnswer';
			document.getElementById('quiz-form').submit();
		}

		function showAnswer() {
			document.getElementById('actionInput').value = 'showAnswer';
			document.getElementById('quiz-form').submit();
		}

		function nextQuestion() {
			document.getElementById('actionInput').value = 'nextQuestion';
			document.getElementById('quiz-form').submit();
		}

		function newQuiz() {
			if (confirm('Möchten Sie wirklich ein neues Quiz starten? Der aktuelle Fortschritt geht verloren.')) {
				document.getElementById('actionInput').value = 'newQuiz';
				document.getElementById('quiz-form').submit();
			}
		}
	</script>
</body>
</html>