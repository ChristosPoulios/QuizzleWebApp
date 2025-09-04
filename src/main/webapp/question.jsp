<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="quizlogic.dto.ThemeDTO"%>
<%@ page import="quizlogic.dto.QuestionDTO"%>
<%@ page import="quizlogic.dto.AnswerDTO"%>
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>Quizzle - Quizfragen</title>
<link rel="stylesheet" type="text/css" href="styles.css" />
</head>
<body>
	<!-- Navigation Tabs -->
	<div class="tab-container">
		<div class="tabs">
			<a href="indexServlet" class="tab">Quizthemen</a> <a
				href="questionServlet" class="tab active">Quizfragen</a> <a
				href="quizServlet" class="tab">Quiz</a> <a href="#" class="tab">Statistik</a>
		</div>
	</div>

	<form id="question-form" method="post" action="questionServlet"
		novalidate>
		<input type="hidden" name="action" value="save" id="actionInput" /> <input
			type="hidden" name="selectedQuestion" value=""
			id="selectedQuestionInput" />

		<div class="question-container">
			<!-- Left Section -->
			<div class="left-section">
				<div class="question-input">
					<label for="thema">Thema</label> <input type="text" id="thema"
						name="thema" readonly
						value="<%=request.getAttribute("currentThema") != null ? request.getAttribute("currentThema") : ""%>" />

					<label for="titel">Titel</label> <input type="text" id="titel"
						name="titel"
						value="<%=request.getAttribute("currentTitel") != null ? request.getAttribute("currentTitel") : ""%>" />

					<label for="frage">Frage</label>
					<textarea id="frage" name="frage" rows="6"><%=request.getAttribute("currentFrage") != null ? request.getAttribute("currentFrage") : ""%></textarea>
				</div>

				<div class="answers-section">
					<div class="answers-header-row">
						<h3>Mögliche Antworten</h3>
						<span class="answer-header">Richtig</span>
					</div>

					<div class="answer-row">
						<label>Antwort 1</label> <input type="text" name="antwort1"
							value="<%=request.getAttribute("antwort1") != null ? request.getAttribute("antwort1") : ""%>" />
						<input type="checkbox" name="richtig1"
							<%=request.getAttribute("richtig1") != null ? "checked" : ""%> />
					</div>

					<div class="answer-row">
						<label>Antwort 2</label> <input type="text" name="antwort2"
							value="<%=request.getAttribute("antwort2") != null ? request.getAttribute("antwort2") : ""%>" />
						<input type="checkbox" name="richtig2"
							<%=request.getAttribute("richtig2") != null ? "checked" : ""%> />
					</div>

					<div class="answer-row">
						<label>Antwort 3</label> <input type="text" name="antwort3"
							value="<%=request.getAttribute("antwort3") != null ? request.getAttribute("antwort3") : ""%>" />
						<input type="checkbox" name="richtig3"
							<%=request.getAttribute("richtig3") != null ? "checked" : ""%> />
					</div>

					<div class="answer-row">
						<label>Antwort 4</label> <input type="text" name="antwort4"
							value="<%=request.getAttribute("antwort4") != null ? request.getAttribute("antwort4") : ""%>" />
						<input type="checkbox" name="richtig4"
							<%=request.getAttribute("richtig4") != null ? "checked" : ""%> />
					</div>
				</div>
			</div>

			<!-- Right Section -->
			<div class="right-section">
				<div class="questions-list-section">
					<div class="questions-header">
						<h3 id="list-title">Fragen zum Thema</h3>
						<div class="theme-controls">
							<button type="button" id="toggle-view-btn" onclick="toggleView()">Themenliste</button>
							<select name="selectedTheme" onchange="this.form.submit();">
								<option value="">Alle Themen</option>
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
					</div>

					<!-- Fragenliste -->
					<div class="questions-list" id="questions-view">
						<%
						ArrayList<QuestionDTO> questions = (ArrayList<QuestionDTO>) request.getAttribute("questions");
						if (questions != null && !questions.isEmpty()) {
							for (QuestionDTO question : questions) {
						%>
						<div class="question-item"
							onclick="loadQuestion('<%=question.getQuestionTitle()%>')">
							<%=question.getQuestionTitle()%>
						</div>
						<%
						}
						} else {
						%>
						<div class="no-questions">Keine Fragen für dieses Thema
							vorhanden</div>
						<%
						}
						%>
					</div>

					<!-- Themenliste (initial versteckt) -->
					<div class="themes-list" id="themes-view" style="display: none;">
						<%
						if (themes != null && !themes.isEmpty()) {
							for (ThemeDTO theme : themes) {
						%>
						<div class="theme-item"
							onclick="selectTheme('<%=theme.getThemeTitle()%>')">
							<div class="theme-title"><%=theme.getThemeTitle()%></div>
							<%
							if (theme.getThemeDescription() != null && !theme.getThemeDescription().trim().isEmpty()) {
							%>
							<div class="theme-description"><%=theme.getThemeDescription()%></div>
							<%
							}
							%>
						</div>
						<%
						}
						} else {
						%>
						<div class="no-themes">Keine Themen vorhanden</div>
						<%
						}
						%>
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
			session.removeAttribute("msgType");
			%>
			<%
			} else {
			%>
			Meldungen
			<%
			}
			%>
		</div>

		<div class="buttons">
			<button type="button" onclick="deleteQuestion()">Frage
				löschen</button>
			<button type="submit" onclick="setAction('save')">Frage
				speichern</button>
			<button type="button" onclick="newQuestion()">Neue Frage</button>
		</div>
	</form>

	<script>
		function setAction(action) {
			document.getElementById('actionInput').value = action;
		}

		function loadQuestion(questionTitle) {
			document.getElementById('actionInput').value = 'load';
			document.querySelector('input[name="selectedQuestion"]').value = questionTitle;
			document.getElementById('question-form').submit();
		}

		function deleteQuestion() {
			if (confirm('Sind Sie sicher, dass Sie diese Frage löschen möchten?')) {
				document.getElementById('actionInput').value = 'delete';
				document.getElementById('question-form').submit();
			}
		}

		function newQuestion() {
			document.getElementById('titel').value = '';
			document.getElementById('frage').value = '';
			document.querySelector('input[name="antwort1"]').value = '';
			document.querySelector('input[name="antwort2"]').value = '';
			document.querySelector('input[name="antwort3"]').value = '';
			document.querySelector('input[name="antwort4"]').value = '';
			document.querySelector('input[name="richtig1"]').checked = false;
			document.querySelector('input[name="richtig2"]').checked = false;
			document.querySelector('input[name="richtig3"]').checked = false;
			document.querySelector('input[name="richtig4"]').checked = false;
		}

		function toggleView() {
			var questionsView = document.getElementById('questions-view');
			var themesView = document.getElementById('themes-view');
			var listTitle = document.getElementById('list-title');
			var toggleButton = document.getElementById('toggle-view-btn');

			if (questionsView.style.display === 'none') {

				questionsView.style.display = 'block';
				themesView.style.display = 'none';
				listTitle.innerHTML = 'Fragen zum Thema';
				toggleButton.innerHTML = 'Themenliste';
			} else {

				questionsView.style.display = 'none';
				themesView.style.display = 'block';
				listTitle.innerHTML = 'Themenliste';
				toggleButton.innerHTML = 'Fragenliste';
			}
		}

		function selectTheme(themeTitle) {

			document.querySelector('select[name="selectedTheme"]').value = themeTitle;

			var questionsView = document.getElementById('questions-view');
			var themesView = document.getElementById('themes-view');
			var listTitle = document.getElementById('list-title');
			var toggleButton = document.getElementById('toggle-view-btn');

			questionsView.style.display = 'block';
			themesView.style.display = 'none';
			listTitle.innerHTML = 'Fragen zum Thema';
			toggleButton.innerHTML = 'Themenliste';

			document.getElementById('question-form').submit();
		}
	</script>
</body>
</html>