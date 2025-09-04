<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="quizlogic.dto.ThemeDTO" %>
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>Examens Formular - Feste Größe mit vier Buttons</title>
<link rel="stylesheet" type="text/css" href="styles.css" />
</head>
<body>

	<form id="exam-form" method="post" action="indexServlet" novalidate>
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
				<select id="themen-liste" size="10" name="themen-liste">
					<% 
					ArrayList<ThemeDTO> themes = (ArrayList<ThemeDTO>) request.getAttribute("themes");
					String selectedTheme = (String) request.getAttribute("selectedTheme");
					if (themes != null && !themes.isEmpty()) {
						for (ThemeDTO theme : themes) {
							boolean isSelected = selectedTheme != null && selectedTheme.equals(theme.getTitle());
					%>
						<option value="<%= theme.getTitle() %>" <%= isSelected ? "selected" : "" %>>
							<%= theme.getTitle() %>
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

		<div id="message-area" aria-live="polite" role="status">
			<% String msg = (String) session.getAttribute("msg"); 
			   if (msg != null) { %>
				<%= msg %>
			<% } %>
		</div>

		<div class="buttons">
			<button type="submit" name="action" value="delete">Thema Löschen</button>
			<button type="submit" name="action" value="save">Speichern</button>
			<button type="submit" name="action" value="new">Neues Thema</button>
			<button type="submit" name="action" value="load">Thema Laden</button>
		</div>
	</form>
</body>
</html>