package com.indexa.controller;

import com.indexa.Main;
import com.indexa.dao.UserDAO;
import com.indexa.model.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Builds the Login screen. Login is entirely OPTIONAL per the project
 * spec - guests reach this screen only if they choose to log in from
 * the Home screen; "Continue as Guest" always returns them to Home
 * without an account.
 */
public class LoginController {

    private final UserDAO userDAO;
    private final Consumer<User> onLoginSuccess;
    private final Runnable onContinueAsGuest;
    private final Runnable onGoToRegister;

    private TextField emailField;
    private PasswordField passwordField;
    private Label errorLabel;

    public LoginController(UserDAO userDAO, Consumer<User> onLoginSuccess,
                            Runnable onContinueAsGuest, Runnable onGoToRegister) {
        this.userDAO = userDAO;
        this.onLoginSuccess = onLoginSuccess;
        this.onContinueAsGuest = onContinueAsGuest;
        this.onGoToRegister = onGoToRegister;
    }

    public Parent getView() {
        Label title = new Label("INDEXA");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Main.COLOR_LIGHT_PURPLE);

        Label subtitle = new Label("Log in to save search history and bookmarks");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Main.COLOR_TEXT_SECONDARY);

        emailField = styledTextField("Email");
        passwordField = styledPasswordField("Password");

        errorLabel = new Label("");
        errorLabel.setFont(Font.font("Segoe UI", 12));
        errorLabel.setTextFill(Color.web("#F87171")); // soft red, error-only use
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button loginButton = filledButton("Login");
        loginButton.setPrefWidth(320);
        loginButton.setOnAction(e -> attemptLogin());

        Button guestButton = outlinedButton("Continue as Guest");
        guestButton.setPrefWidth(320);
        guestButton.setOnAction(e -> onContinueAsGuest.run());

        Label registerPrompt = new Label("Don't have an account?");
        registerPrompt.setFont(Font.font("Segoe UI", 12));
        registerPrompt.setTextFill(Main.COLOR_TEXT_SECONDARY);

        Button createAccountLink = textLinkButton("Create Account");
        createAccountLink.setOnAction(e -> onGoToRegister.run());

        Button forgotPasswordLink = textLinkButton("Forgot Password?");
        forgotPasswordLink.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Forgot Password");
            alert.setHeaderText(null);
            alert.setContentText("Password reset isn't available in this version. Please contact support or create a new account.");
            alert.showAndWait();
        });

        HBox registerRow = new HBox(6, registerPrompt, createAccountLink);
        registerRow.setAlignment(Pos.CENTER);

        VBox card = new VBox(14, title, subtitle, emailField, passwordField, errorLabel,
                loginButton, guestButton, forgotPasswordLink, registerRow);
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
     * Validates the login attempt against UserDAO. Handles the two
     * error cases from the spec (empty fields, invalid credentials)
     * without ever showing a raw exception to the user.
     */
    private void attemptLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            showError("Please enter both email and password.");
            return;
        }

        try {
            Optional<User> userOpt = userDAO.validateLogin(email.trim(), password);
            if (userOpt.isPresent()) {
                onLoginSuccess.accept(userOpt.get());
            } else {
                showError("Invalid email or password.");
            }
        } catch (SQLException e) {
            // Per the error-handling rule: never show a raw stack
            // trace. Log the real cause for developers, show a clean
            // message to the user.
            System.err.println("[INDEXA] Login database error: " + e.getMessage());
            showError("Something went wrong. Please try again.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    // ---------- Shared styling helpers ----------

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

    static Button filledButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(42);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setCursor(javafx.scene.Cursor.HAND);
        button.setStyle("-fx-background-color: #8B5CF6; -fx-background-radius: 10; -fx-text-fill: #F5F3FF;");
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #A78BFA; -fx-background-radius: 10; -fx-text-fill: #F5F3FF;"));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: #8B5CF6; -fx-background-radius: 10; -fx-text-fill: #F5F3FF;"));
        return button;
    }

    static Button outlinedButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(42);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setCursor(javafx.scene.Cursor.HAND);
        button.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;" +
                "-fx-border-color: #8B5CF6; -fx-border-radius: 10; -fx-border-width: 1.2; -fx-text-fill: #A78BFA;");
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: rgba(139,92,246,0.15); -fx-background-radius: 10;" +
                "-fx-border-color: #8B5CF6; -fx-border-radius: 10; -fx-border-width: 1.2; -fx-text-fill: #A78BFA;"));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 10;" +
                "-fx-border-color: #8B5CF6; -fx-border-radius: 10; -fx-border-width: 1.2; -fx-text-fill: #A78BFA;"));
        return button;
    }

    static Button textLinkButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", 12));
        button.setCursor(javafx.scene.Cursor.HAND);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #A78BFA; -fx-underline: true;");
        return button;
    }
}
