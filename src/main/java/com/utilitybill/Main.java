package com.utilitybill;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    private static Stage primaryStage;
    private static final String APP_TITLE = "Utility Bill Management System";
    private static final double LOGIN_WIDTH = 500;
    private static final double LOGIN_HEIGHT = 650;
    private static final double DASHBOARD_WIDTH = 1400;
    private static final double DASHBOARD_HEIGHT = 850;
    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle(APP_TITLE);

        try {
            primaryStage.getIcons().add(new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/com/utilitybill/images/icon.png"))
            ));
        } catch (Exception e) {
            System.out.println("Application icon not found, using default");
        }

        showLogin();
    }

    public static void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/utilitybill/view/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, LOGIN_WIDTH, LOGIN_HEIGHT);

            try {
                String style = Objects.requireNonNull(Main.class.getResource("/com/utilitybill/css/styles.css")).toExternalForm();
                scene.getStylesheets().add(style);
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

    public static void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/utilitybill/view/dashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, DASHBOARD_WIDTH, DASHBOARD_HEIGHT);

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

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(50));
        System.out.println("  Utility Bill Management System");
        System.out.println("  Version 1.0");
        System.out.println("=".repeat(50));
        System.out.println();

        launch(args);
    }
}

