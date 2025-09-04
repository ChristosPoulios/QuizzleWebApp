package servlets;

import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.DataManager;
import quizlogic.dto.ThemeDTO;

/**
 * Servlet implementation class IndexServlet
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
		
		String titel = request.getParameter("titel");
		String info = request.getParameter("info");
		String selectedTheme = request.getParameter("themen-liste");
		String action = request.getParameter("action");
		String message = null;
		
		System.out.println("Titel: " + titel);
		System.out.println("Info: " + info);
		System.out.println("Selected Theme: " + selectedTheme);
		System.out.println("Action: " + action);

		if ("save".equals(action)) {
			if (titel != null && !titel.trim().isEmpty()) {
				try {
					DataManager dm = DataManager.getInstance();
					ThemeDTO themeDTO = new ThemeDTO();
					themeDTO.setTitle(titel.trim());
					themeDTO.setThemeDescription(info != null ? info.trim() : "");
					
					@SuppressWarnings("unused")
					String result = dm.saveTheme(themeDTO);
					String storageMethod = dm.getStorageMethodDescription();
					message = "Thema erfolgreich gespeichert (" + storageMethod + ")";
					
					System.out.println("Theme saved successfully using: " + storageMethod);
					
				} catch (Exception e) {
					message = "Fehler beim Speichern: " + e.getMessage();
					System.err.println("Error saving theme: " + e.getMessage());
					e.printStackTrace();
				}
			} else {
				message = "Bitte geben Sie einen Titel ein.";
			}
			
		} else if ("delete".equals(action)) {
			if (selectedTheme != null && !selectedTheme.trim().isEmpty()) {
				try {
					DataManager dm = DataManager.getInstance();
					ThemeDTO themeDTO = new ThemeDTO();
					themeDTO.setTitle(selectedTheme.trim());
					
					String result = dm.deleteTheme(themeDTO);
					message = "Thema '" + selectedTheme + "' wurde gelöscht";
					
					System.out.println("Theme deleted: " + result);
					
				} catch (Exception e) {
					message = "Fehler beim Löschen: " + e.getMessage();
					System.err.println("Error deleting theme: " + e.getMessage());
					e.printStackTrace();
				}
			} else {
				message = "Bitte wählen Sie ein Thema zum Löschen aus.";
			}
			
		} else if ("load".equals(action)) {

			if (selectedTheme != null && !selectedTheme.trim().isEmpty()) {
				try {
					DataManager dm = DataManager.getInstance();
					ArrayList<ThemeDTO> themes = dm.getAllThemes();
					
					for (ThemeDTO theme : themes) {
						if (theme.getTitle().equals(selectedTheme)) {
							titel = theme.getTitle();
							info = theme.getThemeDescription();
							message = "Thema '" + selectedTheme + "' wurde geladen";
							break;
						}
					}
				} catch (Exception e) {
					message = "Fehler beim Laden des Themas: " + e.getMessage();
					System.err.println("Error loading theme: " + e.getMessage());
					e.printStackTrace();
				}
			} else {
				message = "Bitte wählen Sie ein Thema zum Laden aus.";
			}
			
		} else if ("new".equals(action)) {
			titel = "";
			info = "";
			selectedTheme = null;
			message = "Bereit für neues Thema";
		}
		
		ArrayList<ThemeDTO> themes = new ArrayList<>();
		try {
			DataManager dm = DataManager.getInstance();
			themes = dm.getAllThemes();
			System.out.println("Loaded " + themes.size() + " themes from storage");
		} catch (Exception e) {
			System.err.println("Error loading themes: " + e.getMessage());
			e.printStackTrace();
			if (message == null) {
				message = "Fehler beim Laden der Themen: " + e.getMessage();
			}
		}
		
		request.setAttribute("themes", themes);
		request.setAttribute("currentTitel", titel);
		request.setAttribute("currentInfo", info);
		request.setAttribute("selectedTheme", selectedTheme);
		
		if (message != null) {
			request.getSession().setAttribute("msg", message);
		}
		
		String responseJsp = "/index.jsp";
		RequestDispatcher dispatcher = request.getRequestDispatcher(responseJsp);
		dispatcher.forward(request, response);
		
		if (message != null) {
			request.getSession().removeAttribute("msg");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}