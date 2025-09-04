<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="quizlogic.dto.ThemeDTO" %>
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>Quizzle - Quizthemen</title>
<link rel="stylesheet" type="text/css" href="styles.css" />
</head>
<body>
	<!-- Navigation Tabs -->
	<div class="tab-container">
		<div class="tabs">
			<a href="indexServlet" class="tab active">Quizthemen</a>
			<a href="questionServlet" class="tab">Quizfragen</a>
			<a href="quizServlet" class="tab">Quiz</a>
			<a href="#" class="tab">Statistik</a>
		</div>
	</div>

	<form id="exam-form" method="post" action="indexServlet" novalidate>
		<input type="hidden" name="action" value="load" id="actionInput" />
		
		<div class="container">
			<div id="neues-thema">
				<label for="titel">Titel</label> 
				<input type="text" id="titel" name="titel" 
					value="<%= request.getAttribute("currentTitel") != null ? request.getAttribute("currentTitel") : "" %>" />
				<label for="info">Information zum Thema</label>
				<textarea id="info" name="info"><%= request.getAttribute("currentInfo") != null ? request.getAttribute("currentInfo") : "" %></textarea>
			</div>

			<div id="themen">
				<label>Themen</label> 
				<select id="themen-liste" size="10" name="themen-liste" onchange="document.getElementById('actionInput').value='autoload'; this.form.submit();">
					<% 
					ArrayList<ThemeDTO> themes = (ArrayList<ThemeDTO>) request.getAttribute("themes");
					String selectedTheme = (String) request.getAttribute("selectedTheme");
					if (themes != null && !themes.isEmpty()) {
						for (ThemeDTO theme : themes) {
							boolean isSelected = selectedTheme != null && selectedTheme.equals(theme.getThemeTitle());
					%>
						<option value="<%= theme.getThemeTitle() %>" <%= isSelected ? "selected" : "" %>>
							<%= theme.getThemeTitle() %>
							<% if (theme.getThemeDescription() != null && !theme.getThemeDescription().trim().isEmpty()) { %>
							- <%= theme.getThemeDescription() %>
							<% } %>
						</option>
					<% 
						}
					} else { 
					%>
						<option disabled>Keine Themen vorhanden</option>
					<% } %>
				</select>
			</div>
		</div>

		<div id="message-area" aria-live="polite" role="status"
			<% String msgType = (String) session.getAttribute("msgType"); %>
			<%= msgType != null ? "class=\"" + msgType + "\"" : "" %>>
			<% String msg = (String) session.getAttribute("msg"); 
			   if (msg != null) { %>
				<%= msg %>
				<% session.removeAttribute("msgType"); %>
			<% } else { %>
				Meldungen
			<% } %>
		</div>

		<div class="buttons">
			<button type="submit" onclick="document.getElementById('actionInput').value='delete'">Thema löschen</button>
			<button type="submit" onclick="document.getElementById('actionInput').value='save'">Thema speichern</button>
			<button type="submit" onclick="document.getElementById('actionInput').value='new'">Neues Thema</button>
		</div>
	</form>
</body>
</html>