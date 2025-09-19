<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="quizlogic.dto.QuizSessionDTO"%>
<%@ page import="quizlogic.dto.ThemeDTO"%>
<%@ page import="java.text.SimpleDateFormat"%>
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>Quizzle - Statistiken</title>
<link rel="stylesheet" type="text/css" href="styles.css" />
</head>
<body>
	<!-- Navigation Tabs -->
	<div class="tab-container">
		<div class="tabs">
			<a href="indexServlet" class="tab">Quizthemen</a>
			<a href="questionServlet" class="tab">Quizfragen</a>
			<a href="quizServlet" class="tab">Quiz</a>
			<a href="statisticsServlet" class="tab active">Statistik</a>
		</div>
	</div>

	<div class="statistics-container">

		
		<!-- Übersichts-Statistiken -->
		<div class="stats-overview">
			<div class="stats-card">
				<h3>Gesamtanzahl Themen</h3>
				<div class="stats-number"><%=request.getAttribute("totalThemes") != null ? request.getAttribute("totalThemes") : 0%></div>
			</div>
			<div class="stats-card">
				<h3>Gesamtanzahl Fragen</h3>
				<div class="stats-number"><%=request.getAttribute("totalQuestions") != null ? request.getAttribute("totalQuestions") : 0%></div>
			</div>
			<div class="stats-card">
				<h3>Quiz-Sessions</h3>
				<div class="stats-number"><%=request.getAttribute("totalSessions") != null ? request.getAttribute("totalSessions") : 0%></div>
			</div>
		</div>

		<!-- Letzte Quiz-Sessions -->
		<div class="recent-sessions">
			<h3>Letzte Quiz-Sessions</h3>
			<div class="sessions-list">
				<%
				ArrayList<QuizSessionDTO> recentSessions = (ArrayList<QuizSessionDTO>) request.getAttribute("recentSessions");
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				if (recentSessions != null && !recentSessions.isEmpty()) {
					for (QuizSessionDTO quizSession : recentSessions) {
						int correctAnswers = 0;
						int totalAnswers = quizSession.getUserAnswers() != null ? quizSession.getUserAnswers().size() : 0;
						if (quizSession.getUserAnswers() != null) {
							for (quizlogic.dto.UserAnswerDTO answer : quizSession.getUserAnswers()) {
								if (answer.isCorrect()) {
									correctAnswers++;
								}
							}
						}
						double percentage = totalAnswers > 0 ? (correctAnswers * 100.0 / totalAnswers) : 0;
				%>
					<div class="session-item">
						<div class="session-date"><%=dateFormat.format(quizSession.getTimestamp())%></div>
						<div class="session-score">
							<%=correctAnswers%>/<%=totalAnswers%> 
							(<%=String.format("%.1f", percentage)%>%)
						</div>
					</div>
				<%
					}
				} else {
				%>
					<div class="no-sessions">Noch keine Quiz-Sessions vorhanden</div>
				<%
				}
				%>
			</div>
		</div>

		<!-- Themen-Übersicht -->
		<div class="themes-overview">
			<h3>Themen-Übersicht</h3>
			<div class="themes-list">
				<%
				ArrayList<ThemeDTO> themes = (ArrayList<ThemeDTO>) request.getAttribute("themes");
				if (themes != null && !themes.isEmpty()) {
					for (ThemeDTO theme : themes) {
				%>
					<div class="theme-item">
						<div class="theme-title"><%=theme.getThemeTitle()%></div>
						<div class="theme-description">
							<%=theme.getThemeDescription() != null ? theme.getThemeDescription() : "Keine Beschreibung"%>
						</div>
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

		<!-- Message Area moved below themes overview -->
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
			Statistik-Bereich geladen
			<%
			}
			%>
		</div>
	</div>
</body>
</html>