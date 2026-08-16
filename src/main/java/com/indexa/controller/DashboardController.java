package com.indexa.controller;

import com.indexa.Main;
import com.indexa.dao.BookmarkDAO;
import com.indexa.dao.HistoryDAO;
import com.indexa.model.Document;
import com.indexa.model.SearchHistoryEntry;
import com.indexa.model.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builds the User Dashboard: a "Welcome, [Name]" summary screen with
 * live statistics (never hardcoded - always computed from real
 * HistoryDAO/BookmarkDAO data), plus quick-access sections for Recent
 * Searches, Bookmarked Documents, and a Quick Search box - exactly
 * the layout from the project's "USER DASHBOARD" spec section.
 */
public class DashboardController {

    private final HistoryDAO historyDAO;
    private final BookmarkDAO bookmarkDAO;
    private final User currentUser;
    private final Consumer<String> onSearch;   // Quick Search / Search Again -> Home
    private final Runnable onViewAllHistory;
    private final Runnable onViewAllBookmarks;
    private final Runnable onBack;

    public DashboardController(HistoryDAO historyDAO, BookmarkDAO bookmarkDAO, User currentUser,
                                Consumer<String> onSearch, Runnable onViewAllHistory,
                                Runnable onViewAllBookmarks, Runnable onBack) {
        this.historyDAO = historyDAO;
        this.bookmarkDAO = bookmarkDAO;
        this.currentUser = currentUser;
        this.onSearch = onSearch;
        this.onViewAllHistory = onViewAllHistory;
        this.onViewAllBookmarks = onViewAllBookmarks;
        this.onBack = onBack;
    }

    public Parent getView() {
        // Pull everything fresh from the database every time the
        // Dashboard opens, so statistics are always accurate - never
        // hardcoded or stale.
        List<SearchHistoryEntry> history = safeGetHistory();
        List<Document> bookmarks = safeGetBookmarks();
        int recentSearchCount = countRecentSearches(history);

        Button backButton = LoginController.textLinkButton("\u2190 Back to Search");
        backButton.setOnAction(e -> onBack.run());

        Label welcome = new Label("Welcome, " + currentUser.getName());
        welcome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        welcome.setTextFill(Main.COLOR_LIGHT_PURPLE);

        HBox statsRow = new HBox(16,
                buildStatCard("Total Searches", String.valueOf(history.size())),
                buildStatCard("Saved Documents", String.valueOf(bookmarks.size())),
                buildStatCard("Recent Searches", recentSearchCount + " in last 7 days"));
        statsRow.setAlignment(Pos.CENTER_LEFT);

        VBox quickSearchSection = buildQuickSearchSection();
        VBox recentSearchesSection = buildRecentSearchesSection(history);
        VBox bookmarksSection = buildBookmarksSection(bookmarks);

        VBox content = new VBox(24, welcome, statsRow, quickSearchSection, recentSearchesSection, bookmarksSection);
        content.setPadding(new Insets(30, 40, 40, 40));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox headerArea = new VBox(backButton);
        headerArea.setPadding(new Insets(20, 40, 0, 40));

        BorderPane root = new BorderPane();
        root.setTop(headerArea);
        root.setCenter(scrollPane);
        root.setBackground(new Background(
                new BackgroundFill(Main.COLOR_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)
        ));
        return root;
    }

    // ---------- Sections ----------

    private VBox buildStatCard(String label, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        valueLabel.setTextFill(Main.COLOR_PRIMARY_PURPLE);

        Label captionLabel = new Label(label);
        captionLabel.setFont(Font.font("Segoe UI", 12));
        captionLabel.setTextFill(Main.COLOR_TEXT_SECONDARY);

        VBox card = new VBox(6, valueLabel, captionLabel);
        card.setPadding(new Insets(18));
        card.setPrefWidth(200);
        card.setBackground(new Background(
                new BackgroundFill(Main.COLOR_CARD, new CornerRadii(12), Insets.EMPTY)
        ));
        card.setBorder(new Border(new BorderStroke(
                Main.COLOR_PRIMARY_PURPLE.deriveColor(0, 1, 1, 0.4), BorderStrokeStyle.SOLID,
                new CornerRadii(12), new BorderWidths(1)
        )));
        return card;
    }

    private VBox buildQuickSearchSection() {
        Label heading = sectionHeading("Quick Search", null);

        TextField quickField = new TextField();
        quickField.setPromptText("Search documents...");
        quickField.setPrefHeight(40);
        quickField.setPrefWidth(400);
        quickField.setFont(Font.font("Segoe UI", 13));
        quickField.setBackground(new Background(
                new BackgroundFill(Main.COLOR_CARD, new CornerRadii(8), Insets.EMPTY)
        ));
        quickField.setBorder(new Border(new BorderStroke(
                Main.COLOR_PRIMARY_PURPLE, BorderStrokeStyle.SOLID,
                new CornerRadii(8), new BorderWidths(1)
        )));
        quickField.setStyle(
            "-fx-background-color: #18121F;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #8B5CF6;" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #F5F3FF;" +
            "-fx-prompt-text-fill: #A1A1AA;"
        );
        quickField.setPadding(new Insets(0, 12, 0, 12));
        quickField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !quickField.getText().isBlank()) {
                onSearch.accept(quickField.getText());
            }
        });

        Button goButton = LoginController.filledButton("Search");
        goButton.setPrefWidth(90);
        goButton.setPrefHeight(40);
        goButton.setOnAction(e -> {
            if (!quickField.getText().isBlank()) {
                onSearch.accept(quickField.getText());
            }
        });

        HBox row = new HBox(10, quickField, goButton);
        row.setAlignment(Pos.CENTER_LEFT);

        return new VBox(10, heading, row);
    }

    private VBox buildRecentSearchesSection(List<SearchHistoryEntry> history) {
        Label heading = sectionHeading("Recent Searches", onViewAllHistory);

        VBox list = new VBox(8);
        List<SearchHistoryEntry> recent = history.size() > 5 ? history.subList(0, 5) : history;

        if (recent.isEmpty()) {
            list.getChildren().add(emptyStateLabel("No searches yet."));
        } else {
            for (SearchHistoryEntry entry : recent) {
                list.getChildren().add(buildMiniRow(entry.getQuery(), entry.getSearchedAt(),
                        () -> onSearch.accept(entry.getQuery()), "Search Again"));
            }
        }
        return new VBox(10, heading, list);
    }

    private VBox buildBookmarksSection(List<Document> bookmarks) {
        Label heading = sectionHeading("Bookmarked Documents", onViewAllBookmarks);

        VBox list = new VBox(8);
        List<Document> recent = bookmarks.size() > 5 ? bookmarks.subList(0, 5) : bookmarks;

        if (recent.isEmpty()) {
            list.getChildren().add(emptyStateLabel("No bookmarks yet."));
        } else {
            for (Document doc : recent) {
                list.getChildren().add(buildMiniRow(doc.getTitle(), doc.getFileType(),
                        () -> SearchController.openDocument(doc), "Open"));
            }
        }
        return new VBox(10, heading, list);
    }

    // ---------- Small shared builders ----------

    private Label sectionHeading(String text, Runnable onViewAll) {
        Label heading = new Label(text);
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        heading.setTextFill(Main.COLOR_TEXT_MAIN);
        return heading;
    }

    private Label emptyStateLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", 12));
        label.setTextFill(Main.COLOR_TEXT_SECONDARY);
        return label;
    }

    private HBox buildMiniRow(String primaryText, String secondaryText, Runnable onAction, String actionLabel) {
        Label primary = new Label(primaryText);
        primary.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        primary.setTextFill(Main.COLOR_TEXT_MAIN);

        Label secondary = new Label(secondaryText);
        secondary.setFont(Font.font("Segoe UI", 11));
        secondary.setTextFill(Main.COLOR_TEXT_SECONDARY);

        VBox textBox = new VBox(2, primary, secondary);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button actionButton = LoginController.outlinedButton(actionLabel);
        actionButton.setPrefWidth(110);
        actionButton.setPrefHeight(32);
        actionButton.setOnAction(e -> onAction.run());

        HBox row = new HBox(12, textBox, actionButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setBackground(new Background(
                new BackgroundFill(Main.COLOR_CARD, new CornerRadii(8), Insets.EMPTY)
        ));
        return row;
    }

    // ---------- Data helpers ----------

    private List<SearchHistoryEntry> safeGetHistory() {
        try {
            return historyDAO.getHistoryForUser(currentUser.getId());
        } catch (SQLException e) {
            System.err.println("[INDEXA] Dashboard failed to load history: " + e.getMessage());
            return List.of();
        }
    }

    private List<Document> safeGetBookmarks() {
        try {
            return bookmarkDAO.getBookmarkedDocuments(currentUser.getId());
        } catch (SQLException e) {
            System.err.println("[INDEXA] Dashboard failed to load bookmarks: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Counts searches from the last 7 days - gives the "Recent
     * Searches" stat real meaning distinct from the all-time Total
     * Searches count, computed from actual timestamps, never faked.
     */
    private int countRecentSearches(List<SearchHistoryEntry> history) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int count = 0;
        for (SearchHistoryEntry entry : history) {
            try {
                LocalDateTime searchedAt = LocalDateTime.parse(entry.getSearchedAt(), formatter);
                if (searchedAt.isAfter(cutoff)) {
                    count++;
                }
            } catch (Exception e) {
                // If a timestamp doesn't parse for any reason, skip it
                // rather than crashing the whole Dashboard.
            }
        }
        return count;
    }
}
