package com.indexa.controller;

import com.indexa.Main;
import com.indexa.dao.BookmarkDAO;
import com.indexa.model.Document;
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

/**
 * Builds the Bookmarks screen for logged-in users: every document
 * they've saved, with its file type and saved date, plus Open and
 * Remove buttons - exactly the layout from the project's "BOOKMARKS"
 * spec section.
 */
public class BookmarkController {

    private final BookmarkDAO bookmarkDAO;
    private final User currentUser;
    private final Runnable onBack;

    private VBox listContainer;

    public BookmarkController(BookmarkDAO bookmarkDAO, User currentUser, Runnable onBack) {
        this.bookmarkDAO = bookmarkDAO;
        this.currentUser = currentUser;
        this.onBack = onBack;
    }

    public Parent getView() {
        Button backButton = LoginController.textLinkButton("\u2190 Back to Search");
        backButton.setOnAction(e -> onBack.run());

        Label title = new Label("Bookmarked Documents");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Main.COLOR_LIGHT_PURPLE);

        VBox headerArea = new VBox(14, backButton, title);
        headerArea.setPadding(new Insets(30, 40, 20, 40));

        listContainer = new VBox(10);
        listContainer.setPadding(new Insets(0, 40, 30, 40));

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        loadBookmarks();

        VBox root = new VBox(0, headerArea, scrollPane);
        root.setBackground(new Background(
                new BackgroundFill(Main.COLOR_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)
        ));
        return root;
    }

    private void loadBookmarks() {
        listContainer.getChildren().clear();
        try {
            List<Document> documents = bookmarkDAO.getBookmarkedDocuments(currentUser.getId());
            if (documents.isEmpty()) {
                Label empty = new Label("No bookmarks yet. Save documents from your search results to see them here.");
                empty.setFont(Font.font("Segoe UI", 13));
                empty.setTextFill(Main.COLOR_TEXT_SECONDARY);
                empty.setWrapText(true);
                listContainer.getChildren().add(empty);
                return;
            }
            for (Document doc : documents) {
                listContainer.getChildren().add(buildBookmarkRow(doc));
            }
        } catch (SQLException e) {
            System.err.println("[INDEXA] Failed to load bookmarks: " + e.getMessage());
            Label error = new Label("Could not load bookmarks. Please try again.");
            error.setTextFill(Main.COLOR_TEXT_SECONDARY);
            listContainer.getChildren().add(error);
        }
    }

    private HBox buildBookmarkRow(Document doc) {
        Label titleLabel = new Label(doc.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Main.COLOR_TEXT_MAIN);

        Label metaLabel = new Label(doc.getFileType() + " \u00b7 saved " + doc.getCreatedAt());
        metaLabel.setFont(Font.font("Segoe UI", 11));
        metaLabel.setTextFill(Main.COLOR_TEXT_SECONDARY);

        VBox textBox = new VBox(2, titleLabel, metaLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button openButton = LoginController.filledButton("Open");
        openButton.setPrefWidth(90);
        openButton.setPrefHeight(34);
        openButton.setOnAction(e -> SearchController.openDocument(doc));

        Button removeButton = LoginController.outlinedButton("Remove");
        removeButton.setPrefWidth(90);
        removeButton.setPrefHeight(34);
        removeButton.setOnAction(e -> removeBookmark(doc));

        HBox row = new HBox(12, textBox, openButton, removeButton);
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

    private void removeBookmark(Document doc) {
        try {
            bookmarkDAO.removeBookmark(currentUser.getId(), doc.getId());
            loadBookmarks(); // rebuild list to reflect the removal
        } catch (SQLException e) {
            System.err.println("[INDEXA] Failed to remove bookmark: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not remove this bookmark. Please try again.");
            alert.showAndWait();
        }
    }
}
