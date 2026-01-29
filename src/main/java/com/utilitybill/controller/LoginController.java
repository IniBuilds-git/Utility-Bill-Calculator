package com.utilitybill.controller;

import com.utilitybill.Main;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.InvalidCredentialsException;
import com.utilitybill.model.User;
import com.utilitybill.service.AuthenticationService;
import com.utilitybill.util.AppLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private static final String CLASS_NAME = LoginController.class.getName();

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final AuthenticationService authService;

    public LoginController() {
        this.authService = AuthenticationService.getInstance();
    }

    @FXML
    public void initialize() {
        hideError();
        usernameField.requestFocus();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

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
            User user = authService.login(username, password);
            AppLogger.info(CLASS_NAME, "Login successful for user: " + user.getUsername());
            Main.showDashboard();

        } catch (InvalidCredentialsException e) {
            if (e.shouldLockAccount()) {
                showError("Account locked due to multiple failed attempts. Contact administrator.");
            } else {
                showError("Invalid username or password. Please try again.");
            }
            passwordField.clear();
            passwordField.requestFocus();

        } catch (DataPersistenceException e) {
            showError("System error. Please try again later.");
            AppLogger.error(CLASS_NAME, "Login error: " + e.getMessage(), e);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
