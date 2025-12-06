package com.utilitybill;

/**
 * Launcher class for the Utility Bill Management System.
 * This class is used as the main entry point to work around module system issues.
 *
 * <p>Some Java module configurations require a separate launcher class
 * that doesn't extend Application to properly start the JavaFX application.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class Launcher {

    /**
     * Main method - delegates to the Main application class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}

