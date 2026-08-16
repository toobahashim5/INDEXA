package com.indexa.controller;

import com.indexa.Main;
import com.indexa.dao.BookmarkDAO;
import com.indexa.model.Document;
import com.indexa.model.SearchResult;
import com.indexa.model.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

/**
 * Renders search results as real cards in the UI.
 *
 * As of Step 14, the Bookmark button is fully functional for
 * logged-in users: it checks BookmarkDAO to show the correct initial
 * state (Bookmark vs Bookmarked), and toggles the saved state in
 * SQLite on click, updating its own label immediately.
 */
public class SearchController {

    private final BookmarkDAO bookmarkDAO;

    public SearchController(BookmarkDAO bookmarkDAO) {
        this.bookmarkDAO = bookmarkDAO;
    }

    public void renderResults(VBox resultsContainer, List<SearchResult> results, String query, User currentUser) {
        resultsContainer.getChildren().clear();

        if (results.isEmpty()) {
            resultsContainer.getChildren().add(buildNoResultsCard(query));
            return;
        }

        int position = 1;
        for (SearchResult result : results) {
            resultsContainer.getChildren().add(buildResultCard(result, position, currentUser));
            position++;
        }
    }

    private VBox buildResultCard(SearchResult result, int position, User currentUser) {
        Document doc = result.getDocument();

        Label positionAndTitle = new Label("#" + position + "  " + doc.getTitle());
        positionAndTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        positionAndTitle.setTextFill(Main.COLOR_LIGHT_PURPLE);
        positionAndTitle.setWrapText(true);

        Label snippet = new Label(result.getSnippet());
        snippet.setFont(Font.font("Segoe UI", 13));
        snippet.setTextFill(Main.COLOR_TEXT_SECONDARY);
        snippet.setWrapText(true);

        String matchedText = result.getMatchedKeywords().isEmpty()
                ? "Matched keywords: (title only)"
                : "Matched keywords: " + String.join(", ", result.getMatchedKeywords());
        Label matched = new Label(matchedText);
        matched.setFont(Font.font("Segoe UI", 12));
        matched.setTextFill(Main.COLOR_TEXT_SECONDARY);
        matched.setWrapText(true);

        Label relevance = new Label("Relevance: " + result.getRelevancePercent() + "%");
        relevance.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        relevance.setTextFill(Main.COLOR_PRIMARY_PURPLE);

        Label type = new Label("Type: " + doc.getFileType());
        type.setFont(Font.font("Segoe UI", 12));
        type.setTextFill(Main.COLOR_TEXT_SECONDARY);

        HBox metaRow = new HBox(20, relevance, type);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Button openButton = buildActionButton("Open", true);
        openButton.setOnAction(e -> openDocument(doc));

        Button bookmarkButton = buildActionButton("Bookmark", false);
        setupBookmarkButton(bookmarkButton, doc, currentUser);

        HBox buttonRow = new HBox(10, openButton, bookmarkButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(8, positionAndTitle, snippet, matched, metaRow, buttonRow);
        card.setPadding(new Insets(16));
        card.setMaxWidth(600);
        card.setBackground(new Background(
                new BackgroundFill(Main.COLOR_CARD, new CornerRadii(12), Insets.EMPTY)
        ));
        card.setBorder(new Border(new BorderStroke(
                Main.COLOR_PRIMARY_PURPLE.deriveColor(0, 1, 1, 0.4), BorderStrokeStyle.SOLID,
                new CornerRadii(12), new BorderWidths(1)
        )));
        return card;
    }

    /**
     * Wires up the Bookmark button's initial label (checking whether
     * this document is already saved) and its click behavior (toggle
     * save/unsave). Guests get the "Login Required" message instead,
     * since bookmarks require an account.
     */
    private void setupBookmarkButton(Button button, Document doc, User currentUser) {
        if (currentUser == null) {
            button.setText("Bookmark");
            button.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login Required");
                alert.setHeaderText(null);
                alert.setContentText("Please log in to bookmark documents.");
                alert.showAndWait();
            });
            return;
        }

        boolean[] isBookmarked = {false};
        try {
            isBookmarked[0] = bookmarkDAO.isBookmarked(currentUser.getId(), doc.getId());
        } catch (SQLException e) {
            System.err.println("[INDEXA] Failed to check bookmark status: " + e.getMessage());
        }
        updateBookmarkLabel(button, isBookmarked[0]);

        button.setOnAction(e -> {
            try {
                if (isBookmarked[0]) {
                    bookmarkDAO.removeBookmark(currentUser.getId(), doc.getId());
                    isBookmarked[0] = false;
                } else {
                    bookmarkDAO.addBookmark(currentUser.getId(), doc.getId());
                    isBookmarked[0] = true;
                }
                updateBookmarkLabel(button, isBookmarked[0]);
            } catch (SQLException ex) {
                System.err.println("[INDEXA] Bookmark action failed: " + ex.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Could not update bookmark. Please try again.");
                alert.showAndWait();
            }
        });
    }

    private void updateBookmarkLabel(Button button, boolean bookmarked) {
        button.setText(bookmarked ? "\u2605 Bookmarked" : "Bookmark");
    }

    private VBox buildNoResultsCard(String query) {
        Label message = new Label("No results found for \"" + query + "\"");
        message.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        message.setTextFill(Main.COLOR_TEXT_MAIN);

        Label hint = new Label("Try different keywords, or check your spelling.");
        hint.setFont(Font.font("Segoe UI", 13));
        hint.setTextFill(Main.COLOR_TEXT_SECONDARY);

        VBox card = new VBox(6, message, hint);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24));
        card.setMaxWidth(600);
        card.setBackground(new Background(
                new BackgroundFill(Main.COLOR_CARD, new CornerRadii(12), Insets.EMPTY)
        ));
        return card;
    }

    private Button buildActionButton(String text, boolean filled) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setPrefWidth(110);
        button.setPrefHeight(32);
        button.setCursor(javafx.scene.Cursor.HAND);

        if (filled) {
            button.setStyle(
                "-fx-background-color: #8B5CF6;" +
                "-fx-background-radius: 8;" +
                "-fx-text-fill: #F5F3FF;"
            );
            button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #A78BFA; -fx-background-radius: 8; -fx-text-fill: #F5F3FF;"));
            button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: #8B5CF6; -fx-background-radius: 8; -fx-text-fill: #F5F3FF;"));
        } else {
            button.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #8B5CF6;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1.2;" +
                "-fx-text-fill: #A78BFA;"
            );
            button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: rgba(139,92,246,0.15); -fx-background-radius: 8;" +
                "-fx-border-color: #8B5CF6; -fx-border-radius: 8; -fx-border-width: 1.2; -fx-text-fill: #A78BFA;"));
            button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 8;" +
                "-fx-border-color: #8B5CF6; -fx-border-radius: 8; -fx-border-width: 1.2; -fx-text-fill: #A78BFA;"));
        }
        return button;
    }

    public static void openDocument(Document doc) {
        TextArea contentArea = new TextArea(doc.getContent());
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setFont(Font.font("Segoe UI", 13));
        contentArea.setPrefSize(560, 420);

        Label titleLabel = new Label(doc.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Main.COLOR_LIGHT_PURPLE);

        Label wordCountLabel = new Label(doc.getWordCount() + " words \u00b7 " + doc.getFileType());
        wordCountLabel.setFont(Font.font("Segoe UI", 12));
        wordCountLabel.setTextFill(Main.COLOR_TEXT_SECONDARY);

        VBox layout = new VBox(10, titleLabel, wordCountLabel, contentArea);
        layout.setPadding(new Insets(20));
        layout.setBackground(new Background(
                new BackgroundFill(Main.COLOR_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)
        ));

        Stage viewerStage = new Stage();
        viewerStage.setTitle(doc.getTitle() + " \u2014 INDEXA");
        viewerStage.setScene(new javafx.scene.Scene(layout, 600, 500));
        viewerStage.show();
    }
}
