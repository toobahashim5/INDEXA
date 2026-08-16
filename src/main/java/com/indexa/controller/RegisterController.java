package com.indexa.controller;

import com.indexa.Main;
import com.indexa.dao.UserDAO;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;

/**
 * Builds the Register screen: Name, Email, Password, Confirm Password,
 * plus validation for all the error cases listed in the project spec
 * (empty fields, mismatched passwords, duplicate email).
 */
public class RegisterController {

    private final UserDAO userDAO;
    private final Runnable onRegisterSuccess;
    private final Runnable onBackToLogin;

    private TextField nameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Label errorLabel;

    public RegisterController(UserDAO userDAO, Runnable onRegisterSuccess, Runnable onBackToLogin) {
        this.userDAO = userDAO;
        this.onRegisterSuccess = onRegisterSuccess;
        this.onBackToLogin = onBackToLogin;
    }

    public Parent getView() {
        Label title = new Label("Create Account");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Main.COLOR_LIGHT_PURPLE);

        Label subtitle = new Label("Join INDEXA to save history and bookmarks");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Main.COLOR_TEXT_SECONDARY);

        nameField = styledTextField("Full Name");
        emailField = styledTextField("Email");
        passwordField = styledPasswordField("Password");
        confirmPasswordField = styledPasswordField("Confirm Password");

        errorLabel = new Label("");
        errorLabel.setFont(Font.font("Segoe UI", 12));
        errorLabel.setTextFill(Color.web("#F87171"));
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button createButton = LoginController.filledButton("Create Account");
        createButton.setPrefWidth(320);
        createButton.setOnAction(e -> attemptRegister());

        Button backButton = LoginController.textLinkButton("Back to Login");
        backButton.setOnAction(e -> onBackToLogin.run());

        VBox card = new VBox(14, title, subtitle, nameField, emailField, passwordField,
                confirmPasswordField, errorLabel, createButton, backButton);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(400);
        card.setBackground(new Background(
                new BackgroundFill(Main.COLOR_CARD, new CornerRadii(16), Insets.EMPTY)
        ));
        card.setBorder(new Border(new BorderStroke(
                Main.COLOR_PRIMARY_PURPLE, BorderStrokeStyle.SOLID,
                new CornerRadii(16), new BorderWidths(1)
        )));

        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setBackground(new Background(
                new BackgroundFill(Main.COLOR_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)
        ));

        return root;
    }

    /**
     * Validates every case from the spec's error-handling list:
     * empty fields, passwords that don't match, and duplicate email
     * (checked here AND enforced by the database's UNIQUE constraint
     * as a second line of defense).
     */
    private void attemptRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (isBlank(name) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)) {
            showError("Please fill in all fields.");
            return;
        }

        if (!isValidEmailFormat(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        try {
            if (userDAO.emailExists(email.trim())) {
                showError("An account with this email already exists.");
                return;
            }
            userDAO.registerUser(name.trim(), email.trim(), password);
            onRegisterSuccess.run();
        } catch (SQLException e) {
            System.err.println("[INDEXA] Registration database error: " + e.getMessage());
            showError("Something went wrong. Please try again.");
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private boolean isValidEmailFormat(String email) {
        return email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    // ---------- Shared styling helpers (same pattern as LoginController) ----------

    private TextField styledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(40);
        field.setPrefWidth(320);
        field.setFont(Font.font("Segoe UI", 13));
        field.setBackground(new Background(
                new BackgroundFill(Main.COLOR_BACKGROUND, new CornerRadii(8), Insets.EMPTY)
        ));
        field.setBorder(new Border(new BorderStroke(
                Main.COLOR_PRIMARY_PURPLE.deriveColor(0, 1, 1, 0.5), BorderStrokeStyle.SOLID,
                new CornerRadii(8), new BorderWidths(1)
        )));
        field.setStyle(
            "-fx-background-color: #0F0B14;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(139,92,246,0.5);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #F5F3FF;" +
            "-fx-prompt-text-fill: #A1A1AA;"
        );
        field.setPadding(new Insets(0, 12, 0, 12));
        return field;
    }

    private PasswordField styledPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefHeight(40);
        field.setPrefWidth(320);
        field.setFont(Font.font("Segoe UI", 13));
        field.setBackground(new Background(
                new BackgroundFill(Main.COLOR_BACKGROUND, new CornerRadii(8), Insets.EMPTY)
        ));
        field.setBorder(new Border(new BorderStroke(
                Main.COLOR_PRIMARY_PURPLE.deriveColor(0, 1, 1, 0.5), BorderStrokeStyle.SOLID,
                new CornerRadii(8), new BorderWidths(1)
        )));
        field.setStyle(
            "-fx-background-color: #0F0B14;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(139,92,246,0.5);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #F5F3FF;" +
            "-fx-prompt-text-fill: #A1A1AA;"
        );
        field.setPadding(new Insets(0, 12, 0, 12));
        return field;
    }
}
