package com.indexa.controller;

import com.indexa.Main;
import com.indexa.dao.BookmarkDAO;
import com.indexa.dao.HistoryDAO;
import com.indexa.dsa.BubbleSort;
import com.indexa.dsa.SelectionSort;
import com.indexa.dsa.Trie;
import com.indexa.model.Document;
import com.indexa.model.SearchResult;
import com.indexa.model.User;
import com.indexa.service.RankingService;
import com.indexa.service.SearchEngine;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Builds the Home/Search screen - the first screen the user actually
 * sees and interacts with.
 *
 * Owns the search box, live autocomplete, Add Document file picker,
 * the Sort filter (Relevance/Title/Date), and pagination over the
 * result list. Actual result card rendering is delegated to
 * SearchController.
 */
public class HomeController {

    private static final int PAGE_SIZE = 5;

    private final Trie trie;
    private final SearchEngine searchEngine;
    private final RankingService rankingService;
    private final List<Document> indexedDocuments;
    private final SearchController searchController;
    private final User currentUser; // null when browsing as a guest
    private final HistoryDAO historyDAO;
    private final BookmarkDAO bookmarkDAO;
    private final Runnable onLoginClick;
    private final Runnable onLogoutClick;
    private final Runnable onHistoryClick;
    private final Runnable onBookmarksClick;
    private final Runnable onDashboardClick;
    private final Consumer<File> onAddDocument;
    private final String initialQuery; // pre-filled query from History's "Search Again", or null

    private TextField searchField;
    private CheckBox exactPhraseCheckBox;
    private ComboBox<String> sortComboBox;
    private VBox suggestionsBox;
    private Label statusLabel;
    private VBox resultsContainer;

    // Pagination + sort state for the CURRENT search's results.
    private List<SearchResult> lastRankedResults = null; // always kept in original relevance order
    private String lastQuery = null;
    private int currentPage = 0;
    private Label pageLabel;
    private Button prevPageButton;
    private Button nextPageButton;
    private HBox paginationBar;

    public HomeController(Trie trie, SearchEngine searchEngine, RankingService rankingService,
                           List<Document> indexedDocuments, User currentUser, HistoryDAO historyDAO,
                           BookmarkDAO bookmarkDAO, Runnable onLoginClick, Runnable onLogoutClick,
                           Runnable onHistoryClick, Runnable onBookmarksClick, Runnable onDashboardClick,
                           Consumer<File> onAddDocument, String initialQuery) {
        this.trie = trie;
        this.searchEngine = searchEngine;
        this.rankingService = rankingService;
        this.indexedDocuments = indexedDocuments;
        this.searchController = new SearchController(bookmarkDAO);
        this.currentUser = currentUser;
        this.historyDAO = historyDAO;
        this.bookmarkDAO = bookmarkDAO;
        this.onLoginClick = onLoginClick;
        this.onLogoutClick = onLogoutClick;
        this.onHistoryClick = onHistoryClick;
        this.onBookmarksClick = onBookmarksClick;
        this.onDashboardClick = onDashboardClick;
        this.onAddDocument = onAddDocument;
        this.initialQuery = initialQuery;
    }

    public Parent getView() {
        HBox topBar = buildTopBar();

        Label title = new Label("INDEXA");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        title.setTextFill(Main.COLOR_LIGHT_PURPLE);

        Label subtitle = new Label("Search your local Data Structures & Algorithms library");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Main.COLOR_TEXT_SECONDARY);

        VBox searchArea = buildSearchArea();

        statusLabel = new Label(indexedDocuments.size() + " documents indexed \u2014 type a query and press Enter.");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        statusLabel.setTextFill(Main.COLOR_TEXT_SECONDARY);

        resultsContainer = new VBox(14);
        resultsContainer.setAlignment(Pos.TOP_CENTER);
        resultsContainer.setPadding(new Insets(10, 0, 20, 0));

        ScrollPane scrollPane = new ScrollPane(resultsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox headerArea = new VBox(14);
        headerArea.setAlignment(Pos.TOP_CENTER);
        headerArea.setPadding(new Insets(30, 40, 10, 40));
        headerArea.getChildren().addAll(title, subtitle, searchArea, statusLabel);

        VBox topSection = new VBox(0, topBar, headerArea);
        topSection.setBackground(new Background(
                new BackgroundFill(Main.COLOR_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)
        ));

        paginationBar = buildPaginationBar();

        BorderPane root = new BorderPane();
        root.setTop(topSection);
        root.setCenter(scrollPane);
        root.setBottom(paginationBar);
        root.setBackground(new Background(
                new BackgroundFill(Main.COLOR_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)
        ));

        if (initialQuery != null && !initialQuery.isBlank()) {
            searchField.setText(initialQuery);
            runSearch();
        }

        return root;
    }

    // ---------- Top bar ----------

    private HBox buildTopBar() {
        Label brand = new Label("INDEXA");
        brand.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        brand.setTextFill(Main.COLOR_LIGHT_PURPLE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addDocButton = LoginController.outlinedButton("+ Add Document");
        addDocButton.setPrefWidth(150);
        addDocButton.setPrefHeight(34);
        addDocButton.setOnAction(e -> handleAddDocument(addDocButton));

        HBox rightSide = new HBox(14);
        rightSide.setAlignment(Pos.CENTER_RIGHT);
        rightSide.getChildren().add(addDocButton);

        if (currentUser == null) {
            Button loginButton = LoginController.outlinedButton("Login");
            loginButton.setPrefWidth(90);
            loginButton.setPrefHeight(34);
            loginButton.setOnAction(e -> onLoginClick.run());
            rightSide.getChildren().add(loginButton);
        } else {
            rightSide.getChildren().addAll(buildUserBadge(), buildPillLink("Dashboard", onDashboardClick),
                    buildPillLink("History", onHistoryClick),
                    buildPillLink("Bookmarks", onBookmarksClick), buildPillLink("Logout", onLogoutClick));
        }

        HBox topBar = new HBox(brand, spacer, rightSide);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 24, 0, 24));
        topBar.setPrefHeight(58);
        topBar.setMaxWidth(Double.MAX_VALUE);
        topBar.setBackground(new Background(
                new BackgroundFill(Main.COLOR_CARD, CornerRadii.EMPTY, Insets.EMPTY)
        ));
        topBar.setBorder(new Border(new BorderStroke(
                Color.TRANSPARENT, Color.TRANSPARENT, Main.COLOR_PRIMARY_PURPLE.deriveColor(0, 1, 1, 0.35), Color.TRANSPARENT,
                BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE,
                CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0), Insets.EMPTY
        )));
        return topBar;
    }

    private HBox buildUserBadge() {
        String initial = currentUser.getName().isBlank() ? "?" : currentUser.getName().substring(0, 1).toUpperCase();

        Circle circle = new Circle(15, Main.COLOR_PRIMARY_PURPLE);
        Label initialLabel = new Label(initial);
        initialLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        initialLabel.setTextFill(Main.COLOR_TEXT_MAIN);

        StackPane avatar = new StackPane(circle, initialLabel);

        Label nameLabel = new Label(currentUser.getName());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nameLabel.setTextFill(Main.COLOR_TEXT_MAIN);

        HBox badge = new HBox(8, avatar, nameLabel);
        badge.setAlignment(Pos.CENTER_LEFT);
        return badge;
    }

    private Button buildPillLink(String text, Runnable onClick) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setPrefHeight(32);
        button.setPadding(new Insets(0, 16, 0, 16));
        button.setCursor(javafx.scene.Cursor.HAND);
        button.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #8B5CF6;" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #A78BFA;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: rgba(139,92,246,0.18); -fx-background-radius: 16;" +
            "-fx-border-color: #8B5CF6; -fx-border-radius: 16; -fx-border-width: 1; -fx-text-fill: #F5F3FF;"));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: transparent; -fx-background-radius: 16;" +
            "-fx-border-color: #8B5CF6; -fx-border-radius: 16; -fx-border-width: 1; -fx-text-fill: #A78BFA;"));
        button.setOnAction(e -> onClick.run());
        return button;
    }

    /**
     * Opens a native file picker restricted to .txt files, and passes
     * the chosen file up to Main via onAddDocument, which owns the
     * IndexingService (shared in-memory index across all screens).
     */
    private void handleAddDocument(Button sourceButton) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Add Document to INDEXA");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        Window ownerWindow = sourceButton.getScene() != null ? sourceButton.getScene().getWindow() : null;
        File selectedFile = chooser.showOpenDialog(ownerWindow);

        if (selectedFile != null) {
            onAddDocument.accept(selectedFile);
        }
    }

    // ---------- Search area ----------

    private VBox buildSearchArea() {
        searchField = new TextField();
        searchField.setPromptText("Search documents... (e.g. binary search tree)");
        searchField.setPrefWidth(480);
        searchField.setPrefHeight(42);
        searchField.setFont(Font.font("Segoe UI", 14));
        searchField.setStyle(
            "-fx-background-color: #18121F;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #8B5CF6;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1.5;" +
            "-fx-text-fill: #F5F3FF;" +
            "-fx-prompt-text-fill: #A1A1AA;"
        );
        searchField.setPadding(new Insets(0, 14, 0, 14));

        Button searchButton = new Button("Search");
        searchButton.setPrefHeight(42);
        searchButton.setPrefWidth(110);
        searchButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        searchButton.setCursor(javafx.scene.Cursor.HAND);
        searchButton.setStyle("-fx-background-color: #8B5CF6; -fx-background-radius: 10; -fx-text-fill: #F5F3FF;");
        searchButton.setOnMouseEntered(e -> searchButton.setStyle(
                "-fx-background-color: #A78BFA; -fx-background-radius: 10; -fx-text-fill: #F5F3FF;"));
        searchButton.setOnMouseExited(e -> searchButton.setStyle(
                "-fx-background-color: #8B5CF6; -fx-background-radius: 10; -fx-text-fill: #F5F3FF;"));

        HBox searchRow = new HBox(10, searchField, searchButton);
        searchRow.setAlignment(Pos.CENTER);

        exactPhraseCheckBox = new CheckBox("Exact phrase match");
        exactPhraseCheckBox.setFont(Font.font("Segoe UI", 12));
        exactPhraseCheckBox.setStyle("-fx-text-fill: #A1A1AA; -fx-mark-color: #8B5CF6;");

        Label sortLabel = new Label("Sort:");
        sortLabel.setFont(Font.font("Segoe UI", 12));
        sortLabel.setTextFill(Main.COLOR_TEXT_SECONDARY);

        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("Relevance", "Title (A-Z)", "Date (Newest)");
        sortComboBox.setValue("Relevance");
        sortComboBox.setPrefHeight(28);
        sortComboBox.setStyle("-fx-background-color: #18121F; -fx-text-fill: #F5F3FF; -fx-mark-color: #A78BFA;");
        sortComboBox.setOnAction(e -> {
            if (lastRankedResults != null) {
                currentPage = 0;
                applySortAndRenderCurrentPage();
            }
        });

        HBox filterRow = new HBox(16, exactPhraseCheckBox, sortLabel, sortComboBox);
        filterRow.setAlignment(Pos.CENTER);
        filterRow.setPadding(new Insets(6, 0, 0, 0));

        suggestionsBox = new VBox(2);
        suggestionsBox.setAlignment(Pos.TOP_LEFT);
        suggestionsBox.setPrefWidth(480);
        suggestionsBox.setPadding(new Insets(4));
        suggestionsBox.setBackground(new Background(
                new BackgroundFill(Main.COLOR_CARD, new CornerRadii(8), Insets.EMPTY)
        ));
        suggestionsBox.setBorder(new Border(new BorderStroke(
                Main.COLOR_PRIMARY_PURPLE, BorderStrokeStyle.SOLID,
                new CornerRadii(8), new BorderWidths(1)
        )));
        suggestionsBox.setVisible(false);
        suggestionsBox.setManaged(false);

        searchField.textProperty().addListener((obs, oldText, newText) -> updateSuggestions(newText));

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                runSearch();
            }
        });
        searchButton.setOnAction(e -> runSearch());

        VBox container = new VBox(6, searchRow, filterRow, suggestionsBox);
        container.setAlignment(Pos.TOP_CENTER);
        return container;
    }

    private void updateSuggestions(String text) {
        suggestionsBox.getChildren().clear();

        if (text == null || text.isBlank()) {
            suggestionsBox.setVisible(false);
            suggestionsBox.setManaged(false);
            return;
        }

        String[] words = text.split(" ");
        String lastWord = words[words.length - 1];

        if (lastWord.isBlank()) {
            suggestionsBox.setVisible(false);
            suggestionsBox.setManaged(false);
            return;
        }

        List<String> suggestions = trie.getSuggestions(lastWord, 5);

        if (suggestions.isEmpty()) {
            suggestionsBox.setVisible(false);
            suggestionsBox.setManaged(false);
            return;
        }

        for (String suggestion : suggestions) {
            Label suggestionLabel = new Label(suggestion);
            suggestionLabel.setFont(Font.font("Segoe UI", 13));
            suggestionLabel.setTextFill(Main.COLOR_TEXT_MAIN);
            suggestionLabel.setPadding(new Insets(6, 10, 6, 10));
            suggestionLabel.setMaxWidth(Double.MAX_VALUE);

            suggestionLabel.setOnMouseEntered(e -> suggestionLabel.setBackground(new Background(
                    new BackgroundFill(Main.COLOR_PRIMARY_PURPLE, new CornerRadii(6), Insets.EMPTY))));
            suggestionLabel.setOnMouseExited(e -> suggestionLabel.setBackground(Background.EMPTY));

            suggestionLabel.setOnMouseClicked(e -> {
                words[words.length - 1] = suggestion;
                searchField.setText(String.join(" ", words));
                searchField.positionCaret(searchField.getText().length());
                suggestionsBox.setVisible(false);
                suggestionsBox.setManaged(false);
                runSearch();
            });

            suggestionsBox.getChildren().add(suggestionLabel);
        }

        suggestionsBox.setVisible(true);
        suggestionsBox.setManaged(true);
    }

    // ---------- Search execution ----------

    private void runSearch() {
        suggestionsBox.setVisible(false);
        suggestionsBox.setManaged(false);

        String query = searchField.getText();
        if (query == null || query.isBlank()) {
            statusLabel.setText("Please enter a search term.");
            resultsContainer.getChildren().clear();
            lastRankedResults = null;
            paginationBar.setVisible(false);
            paginationBar.setManaged(false);
            return;
        }

        long startTime = System.nanoTime();
        Set<Integer> matchingIds = exactPhraseCheckBox.isSelected()
                ? searchEngine.searchExactPhrase(query)
                : searchEngine.search(query);
        List<SearchResult> results = rankingService.rankResults(query, matchingIds, indexedDocuments);
        double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;

        statusLabel.setText(results.size() + " results found in "
                + String.format("%.3f", elapsedSeconds) + " seconds");

        if (currentUser != null) {
            try {
                historyDAO.recordSearch(currentUser.getId(), query);
            } catch (java.sql.SQLException e) {
                System.err.println("[INDEXA] Failed to record search history: " + e.getMessage());
            }
        }

        lastRankedResults = results;
        lastQuery = query;
        currentPage = 0;
        sortComboBox.setValue("Relevance");
        applySortAndRenderCurrentPage();
    }

    // ---------- Sorting + pagination ----------

    /**
     * Applies the currently selected Sort option to a FRESH COPY of
     * the original relevance-ranked results (never mutating the
     * original order, so switching back to "Relevance" is always
     * exact), then renders the current page of that sorted list.
     */
    private void applySortAndRenderCurrentPage() {
        if (lastRankedResults == null) {
            return;
        }
        List<SearchResult> displayList = new ArrayList<>(lastRankedResults);

        String sortChoice = sortComboBox.getValue();
        if ("Title (A-Z)".equals(sortChoice)) {
            BubbleSort.sortResultsByTitle(displayList);
        } else if ("Date (Newest)".equals(sortChoice)) {
            SelectionSort.sortResultsByDate(displayList);
        }
        // "Relevance" needs no sort - displayList is already a copy of
        // the original RankingService order.

        renderCurrentPage(displayList);
    }

    /**
     * Slices displayList down to PAGE_SIZE items for currentPage,
     * renders those as cards, and updates the pagination bar
     * (Prev/Next buttons + "Page X of Y" label).
     */
    private void renderCurrentPage(List<SearchResult> displayList) {
        int totalResults = displayList.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalResults / (double) PAGE_SIZE));
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(totalResults, fromIndex + PAGE_SIZE);
        List<SearchResult> pageItems = fromIndex < toIndex
                ? displayList.subList(fromIndex, toIndex)
                : List.of();

        searchController.renderResults(resultsContainer, pageItems, lastQuery, currentUser);

        boolean showPagination = totalResults > PAGE_SIZE;
        paginationBar.setVisible(showPagination);
        paginationBar.setManaged(showPagination);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
    }

    private HBox buildPaginationBar() {
        prevPageButton = LoginController.outlinedButton("\u2190 Prev");
        prevPageButton.setPrefWidth(100);
        prevPageButton.setPrefHeight(34);
        prevPageButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                applySortAndRenderCurrentPage();
            }
        });

        pageLabel = new Label("Page 1 of 1");
        pageLabel.setFont(Font.font("Segoe UI", 12));
        pageLabel.setTextFill(Main.COLOR_TEXT_SECONDARY);

        nextPageButton = LoginController.outlinedButton("Next \u2192");
        nextPageButton.setPrefWidth(100);
        nextPageButton.setPrefHeight(34);
        nextPageButton.setOnAction(e -> {
            currentPage++;
            applySortAndRenderCurrentPage();
        });

        HBox bar = new HBox(16, prevPageButton, pageLabel, nextPageButton);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(12, 0, 16, 0));
        bar.setBackground(new Background(
                new BackgroundFill(Main.COLOR_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)
        ));
        bar.setVisible(false);
        bar.setManaged(false);
        return bar;
    }
}
