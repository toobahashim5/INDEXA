package com.indexa.controller;

import com.indexa.Main;
import com.indexa.dao.HistoryDAO;
import com.indexa.model.SearchHistoryEntry;
import com.indexa.model.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builds the Search History screen for logged-in users: a list of
 * past queries (most recent first) with Search Again / Delete per
 * row, plus a Clear History button. Only reachable when a user is
 * logged in (HomeController only shows the "History" link then).
 */
public class HistoryController {

    private final HistoryDAO historyDAO;
    private final User currentUser;
    private final Consumer<String> onSearchAgain; // re-runs a query back on Home
    private final Runnable onBack;

    private VBox listContainer;

    public HistoryController(HistoryDAO historyDAO, User currentUser,
                              Consumer<String> onSearchAgain, Runnable onBack) {
        this.historyDAO = historyDAO;
        this.currentUser = currentUser;
        this.onSearchAgain = onSearchAgain;
        this.onBack = onBack;
    }

    public Parent getView() {
        Button backButton = LoginController.textLinkButton("\u2190 Back to Search");
        backButton.setOnAction(e -> onBack.run());

        Label title = new Label("Search History");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Main.COLOR_LIGHT_PURPLE);

        Button clearButton = LoginController.outlinedButton("Clear History");
        clearButton.setPrefWidth(140);
        clearButton.setPrefHeight(36);
        clearButton.setOnAction(e -> clearHistory());

        BorderPane headerBar = new BorderPane();
        headerBar.setLeft(title);
        headerBar.setRight(clearButton);
        BorderPane.setAlignment(title, Pos.CENTER_LEFT);
        BorderPane.setAlignment(clearButton, Pos.CENTER_RIGHT);

        VBox headerArea = new VBox(14, backButton, headerBar);
        headerArea.setPadding(new Insets(30, 40, 20, 40));

        listContainer = new VBox(10);
        listContainer.setPadding(new Insets(0, 40, 30, 40));

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        loadHistory();

        VBox root = new VBox(0, headerArea, scrollPane);
        root.setBackground(new Background(
                new BackgroundFill(Main.COLOR_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)
        ));
        return root;
    }

    private void loadHistory() {
        listContainer.getChildren().clear();
        try {
            List<SearchHistoryEntry> history = historyDAO.getHistoryForUser(currentUser.getId());
            if (history.isEmpty()) {
                Label empty = new Label("No searches yet. Your search history will appear here.");
                empty.setFont(Font.font("Segoe UI", 13));
                empty.setTextFill(Main.COLOR_TEXT_SECONDARY);
                listContainer.getChildren().add(empty);
                return;
            }
            for (SearchHistoryEntry entry : history) {
                listContainer.getChildren().add(buildHistoryRow(entry));
            }
        } catch (SQLException e) {
            System.err.println("[INDEXA] Failed to load search history: " + e.getMessage());
            Label error = new Label("Could not load search history. Please try again.");
            error.setTextFill(Main.COLOR_TEXT_SECONDARY);
            listContainer.getChildren().add(error);
        }
    }

    private HBox buildHistoryRow(SearchHistoryEntry entry) {
        Label queryLabel = new Label(entry.getQuery());
        queryLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        queryLabel.setTextFill(Main.COLOR_TEXT_MAIN);

        Label dateLabel = new Label(entry.getSearchedAt());
        dateLabel.setFont(Font.font("Segoe UI", 11));
        dateLabel.setTextFill(Main.COLOR_TEXT_SECONDARY);

        VBox textBox = new VBox(2, queryLabel, dateLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button searchAgainButton = LoginController.filledButton("Search Again");
        searchAgainButton.setPrefWidth(120);
        searchAgainButton.setPrefHeight(34);
        searchAgainButton.setOnAction(e -> onSearchAgain.accept(entry.getQuery()));

        Button deleteButton = LoginController.outlinedButton("Delete");
        deleteButton.setPrefWidth(90);
        deleteButton.setPrefHeight(34);
        deleteButton.setOnAction(e -> deleteEntry(entry.getId()));

        HBox row = new HBox(12, textBox, searchAgainButton, deleteButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setBackground(new Background(
                new BackgroundFill(Main.COLOR_CARD, new CornerRadii(10), Insets.EMPTY)
        ));
        row.setBorder(new Border(new BorderStroke(
                Main.COLOR_PRIMARY_PURPLE.deriveColor(0, 1, 1, 0.4), BorderStrokeStyle.SOLID,
                new CornerRadii(10), new BorderWidths(1)
        )));
        return row;
    }

    private void deleteEntry(int historyId) {
        try {
            historyDAO.deleteEntry(historyId);
            loadHistory(); // rebuild list to reflect the deletion
        } catch (SQLException e) {
            System.err.println("[INDEXA] Failed to delete history entry: " + e.getMessage());
            showError("Could not delete this entry. Please try again.");
        }
    }

    private void clearHistory() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear History");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete your entire search history? This cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                try {
                    historyDAO.clearHistoryForUser(currentUser.getId());
                    loadHistory();
                } catch (SQLException e) {
                    System.err.println("[INDEXA] Failed to clear history: " + e.getMessage());
                    showError("Could not clear history. Please try again.");
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
