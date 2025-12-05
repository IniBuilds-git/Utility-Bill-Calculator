package com.utilitybill;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.utilitybill.view.MainView;
import com.utilitybill.service.DataService;

/**
 * Main entry point for the Utility Bill Management System.
 * Launches the JavaFX application and initializes core services.
 *
 * @author Utility Bill System
 * @version 1.0
 */
public class LandingPage extends Application {

    /** Application window title */
    private static final String APP_TITLE = "Utility Bill Management System";

    /** Default window dimensions */
    private static final double DEFAULT_WIDTH = 1400;
    private static final double DEFAULT_HEIGHT = 850;

    /** Minimum window dimensions */
    private static final double MIN_WIDTH = 1200;
    private static final double MIN_HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize data service and load data
            initializeServices();

            // Create the main view
            MainView mainView = new MainView();

            // Create scene with the main view
            Scene scene = new Scene(mainView.getRoot(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

            // Load CSS stylesheet
            String css = getClass().getResource("/styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);

            // Configure the primary stage
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);

            // Center on screen
            primaryStage.centerOnScreen();

            // Show the application
            primaryStage.show();

            System.out.println("Application started successfully!");

        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes application services.
     */
    private void initializeServices() {
        try {
            // Initialize and load data
            DataService dataService = DataService.getInstance();
            dataService.loadAllData();
            System.out.println("Data services initialized.");
        } catch (Exception e) {
            System.err.println("Warning: Could not load existing data. Starting fresh.");
            // Application can continue - will create new data files
        }
    }

    @Override
    public void stop() {
        try {
            // Save any unsaved data before closing
            DataService dataService = DataService.getInstance();
            if (dataService.hasUnsavedChanges()) {
                dataService.saveAllData();
                System.out.println("Data saved successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error saving data on exit: " + e.getMessage());
        }
        System.out.println("Application closed.");
    }

    /**
     * Main method - entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}