package com.utilitybill.controller;

import com.utilitybill.Main;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.InvalidCredentialsException;
import com.utilitybill.model.User;
import com.utilitybill.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the login view.
 * Handles user authentication and navigation to the dashboard.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    /** Authentication service instance */
    private final AuthenticationService authService;

    /**
     * Constructs a new LoginController.
     */
    public LoginController() {
        this.authService = AuthenticationService.getInstance();
    }

    /**
     * Initializes the controller.
     * Called automatically after FXML loading.
     */
    @FXML
    public void initialize() {
        // Clear any previous error
        hideError();

        // Focus on username field
        usernameField.requestFocus();
    }

    /**
     * Handles the login button action.
     * Validates credentials and navigates to dashboard on success.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Basic validation
        if (username.isEmpty()) {
            showError("Please enter your username");
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }

        try {
            // Attempt authentication
            User user = authService.login(username, password);

            // Success - navigate to dashboard
            System.out.println("Login successful for user: " + user.getUsername());
            Main.showDashboard();

        } catch (InvalidCredentialsException e) {
            // Handle authentication failure
            if (e.shouldLockAccount()) {
                showError("Account locked due to multiple failed attempts. Contact administrator.");
            } else {
                showError("Invalid username or password. Please try again.");
            }
            passwordField.clear();
            passwordField.requestFocus();

        } catch (DataPersistenceException e) {
            // Handle data access error
            showError("System error. Please try again later.");
            System.err.println("Login error: " + e.getMessage());
        }
    }

    /**
     * Displays an error message.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Hides the error message.
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}

