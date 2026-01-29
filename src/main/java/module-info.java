/**
 * Module descriptor for the Utility Bill Management System.
 * This module defines the dependencies and exports for the JavaFX application.
 */
module com.utilitybill {
    // JavaFX modules - using transitive for public API exposure
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    // Java logging
    requires java.logging;

    // JSON processing - using transitive for adapter classes
    requires transitive com.google.gson;

    // Enhanced UI controls
    requires org.controlsfx.controls;

    // Icons
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    // PDF Generation
    requires kernel;
    requires layout;
    requires io;

    // Email
    requires jakarta.mail;

    // Open packages to JavaFX for FXML injection
    opens com.utilitybill to javafx.fxml;
    opens com.utilitybill.controller to javafx.fxml;
    opens com.utilitybill.model to javafx.fxml, com.google.gson;
    opens com.utilitybill.dao to com.google.gson;

    // Export packages
    exports com.utilitybill;
    exports com.utilitybill.controller;
    exports com.utilitybill.model;
    exports com.utilitybill.service;
    exports com.utilitybill.dao;
    exports com.utilitybill.util;
    exports com.utilitybill.exception;
}
