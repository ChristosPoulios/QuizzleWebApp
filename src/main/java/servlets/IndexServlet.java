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

/**
 * Servlet implementation class IndexServlet Handles CRUD operations for quiz
 * themes in the web application.
 */
@WebServlet("/indexServlet")
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public IndexServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		loadThemes(request);
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String action = request.getParameter("action");
		HttpSession session = request.getSession();

		try {
			switch (action) {
			case "save":
				handleSave(request, session);
				break;
			case "delete":
				handleDelete(request, session);
				break;
			case "load":
				handleLoad(request, session);
				break;
			case "autoload":
				handleAutoLoad(request, session);
				break;
			case "new":
				handleNew(request, session);
				break;
			default:
				session.setAttribute("msg", "Unbekannte Aktion: " + action);
			}
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler: " + e.getMessage());
		}

		loadThemes(request);
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}

	/**
	 * Handles saving a new or existing theme
	 */
	private void handleSave(HttpServletRequest request, HttpSession session) {
		String titel = request.getParameter("titel");
		String info = request.getParameter("info");

		if (titel == null || titel.trim().isEmpty()) {
			session.setAttribute("msg", "Titel darf nicht leer sein!");
			return;
		}

		try {
			ThemeDTO theme = new ThemeDTO(titel.trim(), info != null ? info.trim() : "");
			DataManager dataManager = DataManager.getInstance();

			String result = dataManager.saveTheme(theme);
			if (result == null) {
				session.setAttribute("msg", "Fehler beim Speichern: " + result);
			} else {
				session.setAttribute("msg", "Thema '" + titel + "' erfolgreich gespeichert!");
			}
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Speichern: " + e.getMessage());
		}
	}

	/**
	 * Handles deleting a selected theme
	 */
	private void handleDelete(HttpServletRequest request, HttpSession session) {
		String selectedTheme = request.getParameter("themen-liste");

		if (selectedTheme == null || selectedTheme.trim().isEmpty()) {
			session.setAttribute("msg", "Bitte wählen Sie ein Thema zum Löschen aus!");
			return;
		}

		try {
			DataManager dataManager = DataManager.getInstance();
			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			ThemeDTO themeToDelete = null;

			for (ThemeDTO theme : themes) {
				if (selectedTheme.equals(theme.getThemeTitle())) {
					themeToDelete = theme;
					break;
				}
			}

			if (themeToDelete != null) {
				String result = dataManager.deleteTheme(themeToDelete);
				if (result == null) {
					session.setAttribute("msg", "Fehler beim Löschen: " + result);
				} else {
					session.setAttribute("msg", "Thema '" + selectedTheme + "' erfolgreich gelöscht!");
				}
			} else {
				session.setAttribute("msg", "Thema nicht gefunden!");
			}
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Löschen: " + e.getMessage());
		}
	}

	/**
	 * Handles loading a selected theme for editing
	 */
	private void handleLoad(HttpServletRequest request, HttpSession session) {
		String selectedTheme = request.getParameter("themen-liste");

		if (selectedTheme == null || selectedTheme.trim().isEmpty()) {
			session.setAttribute("msg", "Bitte wählen Sie ein Thema zum Laden aus!");
			return;
		}

		try {
			DataManager dataManager = DataManager.getInstance();
			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			ThemeDTO themeToLoad = null;

			for (ThemeDTO theme : themes) {
				if (selectedTheme.equals(theme.getThemeTitle())) {
					themeToLoad = theme;
					break;
				}
			}

			if (themeToLoad != null) {
				request.setAttribute("currentTitel", themeToLoad.getThemeTitle());
				request.setAttribute("currentInfo", themeToLoad.getThemeDescription());
				request.setAttribute("selectedTheme", selectedTheme);
				session.setAttribute("msg", "Thema '" + selectedTheme + "' geladen!");
			} else {
				session.setAttribute("msg", "Thema nicht gefunden!");
			}
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim Laden: " + e.getMessage());
		}
	}

	/**
	 * Handles creating a new theme (clears form)
	 */
	private void handleNew(HttpServletRequest request, HttpSession session) {
		request.removeAttribute("currentTitel");
		request.removeAttribute("currentInfo");
		request.removeAttribute("selectedTheme");
		session.setAttribute("msg", "Neues Thema - Formular geleert!");
	}

	/**
	 * Handles automatic loading of a selected theme when clicked in the list
	 */
	private void handleAutoLoad(HttpServletRequest request, HttpSession session) {
		String selectedTheme = request.getParameter("themen-liste");

		if (selectedTheme == null || selectedTheme.trim().isEmpty()) {
			return;
		}

		try {
			DataManager dataManager = DataManager.getInstance();
			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			ThemeDTO themeToLoad = null;

			for (ThemeDTO theme : themes) {
				if (selectedTheme.equals(theme.getThemeTitle())) {
					themeToLoad = theme;
					break;
				}
			}

			if (themeToLoad != null) {
				request.setAttribute("currentTitel", themeToLoad.getThemeTitle());
				request.setAttribute("currentInfo", themeToLoad.getThemeDescription());
				request.setAttribute("selectedTheme", selectedTheme);
			}
		} catch (Exception e) {
			session.setAttribute("msg", "Fehler beim automatischen Laden: " + e.getMessage());
		}
	}

	/**
	 * Loads all themes from database and sets them as request attribute
	 */
	private void loadThemes(HttpServletRequest request) {
		try {
			DataManager dataManager = DataManager.getInstance();
			ArrayList<ThemeDTO> themes = dataManager.getAllThemes();
			request.setAttribute("themes", themes);
		} catch (Exception e) {
			request.setAttribute("themes", new ArrayList<ThemeDTO>());
			HttpSession session = request.getSession();
			session.setAttribute("msg", "Fehler beim Laden der Themen: " + e.getMessage());
		}
	}
}