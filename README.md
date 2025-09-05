# QuizzleWebApp - Projekt-Dokumentation

## Projektstruktur

Das QuizzleWebApp Projekt wurde aufgeräumt und neu organisiert. Hier ist die neue Struktur:

### Verzeichnisstruktur
```
QuizzleWebApp/
├── src/main/
│   ├── java/servlets/          # Java Servlet-Klassen
│   │   ├── IndexServlet.java
│   │   ├── QuestionServlet.java
│   │   └── QuizServlet.java
│   └── webapp/                 # Web-Ressourcen
│       ├── css/                # Modulare CSS-Dateien
│       │   ├── base.css        # Basis-Styles und Reset
│       │   ├── navigation.css  # Navigation/Tabs
│       │   ├── themes.css      # Themenverwaltung
│       │   ├── questions.css   # Fragenverwaltung
│       │   └── buttons.css     # Button-Styles
│       ├── js/                 # JavaScript-Dateien
│       │   └── questions.js    # Fragen-Management JS
│       ├── index.jsp           # Themenverwaltung
│       ├── question.jsp        # Fragenverwaltung
│       ├── quiz.jsp            # Quiz-Ausführung
│       ├── styles.css          # Original CSS (Legacy)
│       ├── styles-new.css      # Neue modulare CSS-Imports
│       ├── META-INF/
│       └── WEB-INF/
│           ├── web.xml
│           └── lib/
```

## CSS-Architektur

### Modulare CSS-Struktur
Das CSS wurde in logische Module aufgeteilt:

1. **base.css** - Grundlegende Styles, Reset, Formulare, Nachrichten
2. **navigation.css** - Tab-Navigation und Header
3. **themes.css** - Themenverwaltungsseite spezifische Styles
4. **questions.css** - Fragenverwaltungsseite spezifische Styles
5. **buttons.css** - Wiederverwendbare Button-Komponenten

### Import-System
Die `styles-new.css` importiert alle Module:
```css
@import url('css/base.css');
@import url('css/navigation.css');
@import url('css/themes.css');
@import url('css/questions.css');
@import url('css/buttons.css');
```

## JavaScript-Organisation

### Ausgelagerte Funktionen
- Form-Validierung
- AJAX-ähnliche Funktionalität
- Scroll-Position-Verwaltung
- UI-Interaktionen

### Features
- Automatische Scroll-Position-Wiederherstellung
- Formular-Validierung vor Submit
- Theme/Question Toggle-Funktionalität

## Verbesserungen

### Code-Organisation
✅ CSS in logische Module aufgeteilt
✅ JavaScript in separate Dateien ausgelagert
✅ Bessere Trennung von Struktur, Style und Verhalten
✅ Konsistente Dateistruktur

### Wartbarkeit
✅ Modulare CSS-Architektur ermöglicht einfache Wartung
✅ Wiederverwendbare Komponenten (Buttons, Navigation)
✅ Klare Trennung von Zuständigkeiten

### Performance
✅ Optimierte CSS-Struktur
✅ Effiziente JavaScript-Organisation
✅ Reduzierte Code-Duplikation

## Migration

### Von alter zu neuer Struktur
1. JSP-Dateien verwenden jetzt `styles-new.css`
2. JavaScript wurde in externe Dateien ausgelagert
3. Original `styles.css` bleibt als Legacy-Fallback erhalten

### Rückwärtskompatibilität
- Alte `styles.css` bleibt verfügbar
- Alle bestehenden Funktionen bleiben erhalten
- Schrittweise Migration möglich

## Entwicklung

### CSS-Änderungen
Bearbeiten Sie die entsprechenden Module in `/css/`:
- Layout-Änderungen → `base.css`
- Navigation → `navigation.css`
- Themen-spezifisch → `themes.css`
- Fragen-spezifisch → `questions.css`
- Buttons → `buttons.css`

### JavaScript-Erweiterungen
Neue Funktionen in `/js/` hinzufügen und in entsprechende JSP-Dateien einbinden.