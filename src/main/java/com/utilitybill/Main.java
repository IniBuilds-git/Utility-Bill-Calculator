package com.utilitybill;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Main application class for the Utility Bill Management System.
 * This class serves as the entry point for the JavaFX application.
 *
 * <p>The application demonstrates:</p>
 * <ul>
 *   <li>OOP principles - Encapsulation, Inheritance, Polymorphism, Abstraction</li>
 *   <li>Design Patterns - Singleton, Factory, DAO, Observer</li>
 *   <li>File-based persistence using JSON</li>
 *   <li>MVC architecture with FXML views</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class Main extends Application {

    /** The primary stage for the application */
    private static Stage primaryStage;

    /** Application title */
    private static final String APP_TITLE = "Utility Bill Management System";

    /** Window dimensions */
    private static final double LOGIN_WIDTH = 500;
    private static final double LOGIN_HEIGHT = 650;
    private static final double DASHBOARD_WIDTH = 1400;
    private static final double DASHBOARD_HEIGHT = 850;

    /**
     * The main entry point for the JavaFX application.
     *
     * @param stage the primary stage
     * @throws IOException if FXML loading fails
     */
    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle(APP_TITLE);

        // Set application icon (if available)
        try {
            primaryStage.getIcons().add(new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/com/utilitybill/images/icon.png"))
            ));
        } catch (Exception e) {
            // Icon not found, continue without it
            System.out.println("Application icon not found, using default");
        }

        // Show login screen
        showLogin();
    }

    /**
     * Shows the login screen.
     */
    public static void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/utilitybill/view/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, LOGIN_WIDTH, LOGIN_HEIGHT);

            // Optional: Load CSS if available (uses inline styles as fallback)
            try {
                String css = Objects.requireNonNull(Main.class.getResource("/com/utilitybill/css/styles.css")).toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("CSS not loaded - using inline styles");
            }

            primaryStage.setScene(scene);
            primaryStage.setMinWidth(LOGIN_WIDTH);
            primaryStage.setMinHeight(LOGIN_HEIGHT);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Failed to load login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows the main dashboard.
     */
    public static void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/utilitybill/view/dashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, DASHBOARD_WIDTH, DASHBOARD_HEIGHT);

            // Optional: Load CSS if available (uses inline styles as fallback)
            try {
                String css = Objects.requireNonNull(Main.class.getResource("/com/utilitybill/css/styles.css")).toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("CSS not loaded - using inline styles");
            }

            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(700);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Failed to load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the primary stage.
     *
     * @return the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Main method - entry point for the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Print startup message
        System.out.println("=".repeat(50));
        System.out.println("  Utility Bill Management System");
        System.out.println("  Version 1.0");
        System.out.println("=".repeat(50));
        System.out.println();

        // Launch the JavaFX application
        launch(args);
    }
}

