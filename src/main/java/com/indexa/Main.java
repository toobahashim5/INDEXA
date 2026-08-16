package com.indexa;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;

import com.indexa.controller.BookmarkController;
import com.indexa.controller.DashboardController;
import com.indexa.controller.HistoryController;
import com.indexa.controller.HomeController;
import com.indexa.controller.LoginController;
import com.indexa.controller.RegisterController;
import com.indexa.dao.BookmarkDAO;
import com.indexa.dao.DocumentDAO;
import com.indexa.dao.HistoryDAO;
import com.indexa.dao.UserDAO;
import com.indexa.database.DatabaseInitializer;
import com.indexa.dsa.InvertedIndex;
import com.indexa.dsa.Trie;
import com.indexa.model.Document;
import com.indexa.model.User;
import com.indexa.service.IndexingService;
import com.indexa.service.RankingService;
import com.indexa.service.SearchEngine;
import com.indexa.service.TextProcessor;

import java.util.List;

/**
 * Entry point of the INDEXA desktop application.
 *
 * Responsible for startup wiring (database init, indexing) and for
 * screen navigation between Home, Login, and Register (Step 12).
 * Login is optional - the app always starts on the Home screen as a
 * guest; logging in is something the user opts into from there.
 *
 * The INDEXA color palette lives here as shared public constants so
 * every screen/controller references the same theme.
 */
public class Main extends Application {

    public static final Color COLOR_BACKGROUND = Color.web("#0F0B14");
    public static final Color COLOR_CARD = Color.web("#18121F");
    public static final Color COLOR_PRIMARY_PURPLE = Color.web("#8B5CF6");
    public static final Color COLOR_LIGHT_PURPLE = Color.web("#A78BFA");
    public static final Color COLOR_TEXT_MAIN = Color.web("#F5F3FF");
    public static final Color COLOR_TEXT_SECONDARY = Color.web("#A1A1AA");

    private Stage primaryStage;

    // Core engine pieces, built once at startup and shared by every screen.
    private Trie trie;
    private SearchEngine searchEngine;
    private RankingService rankingService;
    private List<Document> indexedDocuments;
    private UserDAO userDAO;
    private HistoryDAO historyDAO;
    private BookmarkDAO bookmarkDAO;
    private IndexingService indexingService;

    // Current session state - null means "browsing as guest".
    private User currentUser = null;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        DatabaseInitializer.initialize();

        TextProcessor textProcessor = new TextProcessor();
        InvertedIndex invertedIndex = new InvertedIndex();
        trie = new Trie();
        DocumentDAO documentDAO = new DocumentDAO();
        userDAO = new UserDAO();
        historyDAO = new HistoryDAO();
        bookmarkDAO = new BookmarkDAO();
        indexingService = new IndexingService(invertedIndex, trie, textProcessor, documentDAO);
        searchEngine = new SearchEngine(invertedIndex, textProcessor);
        rankingService = new RankingService(textProcessor);

        try {
            indexedDocuments = indexingService.reindexAll("sample-documents");
        } catch (java.sql.SQLException e) {
            System.err.println("[INDEXA] Indexing failed: " + e.getMessage());
            indexedDocuments = new java.util.ArrayList<>();
        }

        System.out.println("[INDEXA] Indexed " + indexedDocuments.size() + " document(s) from sample-documents/");
        System.out.println("[INDEXA] Unique keywords in index: " + invertedIndex.getUniqueKeywordCount());
        System.out.println("[INDEXA] Total indexed terms: " + invertedIndex.getTotalIndexedTerms());

        primaryStage.setTitle("INDEXA");
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        } catch (Exception e) {
            System.err.println("[INDEXA] Could not load app icon: " + e.getMessage());
        }
        showHome();
        primaryStage.show();
    }

    /** Shows the Home/Search screen, reflecting the current login state. */
    private void showHome() {
        showHome(null);
    }

    /** Shows Home, pre-filling and running a query (used by "Search Again"). */
    private void showHome(String initialQuery) {
        HomeController homeController = new HomeController(
                trie, searchEngine, rankingService, indexedDocuments,
                currentUser, historyDAO, bookmarkDAO, this::showLogin, this::logout,
                this::showHistory, this::showBookmarks, this::showDashboard,
                this::addDocument, initialQuery);
        setScene(homeController.getView());
    }

    /**
     * Indexes a newly chosen .txt file (Add Document) and adds it to
     * the same in-memory Trie/InvertedIndex + indexedDocuments list
     * every screen already shares, then refreshes Home so the new
     * document is immediately searchable.
     */
    private void addDocument(File file) {
        try {
            var document = indexingService.indexDocument(file);
            if (document != null) {
                indexedDocuments.add(document);
                showAlert(Alert.AlertType.INFORMATION, "Document Added",
                        "\"" + document.getTitle() + "\" was indexed and is now searchable.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Could Not Add Document",
                        "This file is either not a .txt file, empty, or already indexed.");
            }
        } catch (Exception e) {
            System.err.println("[INDEXA] Failed to add document: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Something went wrong while adding this document. Please try again.");
        }
        showHome();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Shows the Dashboard screen (logged-in users only). */
    private void showDashboard() {
        if (currentUser == null) {
            return; // safety guard - Dashboard link only appears when logged in
        }
        DashboardController dashboardController = new DashboardController(
                historyDAO, bookmarkDAO, currentUser,
                query -> showHome(query),   // Quick Search / Search Again -> Home, run the query
                this::showHistory,          // View all recent searches -> full History screen
                this::showBookmarks,        // View all bookmarks -> full Bookmarks screen
                this::showHome);            // Back -> plain Home
        setScene(dashboardController.getView());
    }

    /** Shows the Search History screen (logged-in users only). */
    private void showHistory() {
        if (currentUser == null) {
            return; // safety guard - History link only appears when logged in
        }
        HistoryController historyController = new HistoryController(
                historyDAO, currentUser,
                query -> showHome(query), // Search Again -> back to Home, run the query
                this::showHome);          // Back -> plain Home
        setScene(historyController.getView());
    }

    /** Shows the Bookmarks screen (logged-in users only). */
    private void showBookmarks() {
        if (currentUser == null) {
            return; // safety guard - Bookmarks link only appears when logged in
        }
        BookmarkController bookmarkController = new BookmarkController(
                bookmarkDAO, currentUser, this::showHome);
        setScene(bookmarkController.getView());
    }

    /** Shows the Login screen. */
    private void showLogin() {
        LoginController loginController = new LoginController(
                userDAO,
                user -> { currentUser = user; showHome(); }, // onLoginSuccess
                this::showHome,                              // onContinueAsGuest
                this::showRegister);                         // onGoToRegister
        setScene(loginController.getView());
    }

    /** Shows the Register screen. */
    private void showRegister() {
        RegisterController registerController = new RegisterController(
                userDAO,
                this::showLogin, // onRegisterSuccess -> back to Login to sign in
                this::showLogin);
        setScene(registerController.getView());
    }

    /** Clears the session and returns to Home as a guest. */
    private void logout() {
        currentUser = null;
        showHome();
    }

    private void setScene(Parent root) {
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
