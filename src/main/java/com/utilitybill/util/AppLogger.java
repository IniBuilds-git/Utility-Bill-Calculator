package com.utilitybill.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Centralized logging utility for the Utility Bill Management System.
 * Provides consistent, formatted logging across all application components.
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>{@code
 * AppLogger.info("Customer created: " + customerId);
 * AppLogger.error("Failed to generate invoice", exception);
 * }</pre>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public final class AppLogger {

    private static final Logger LOGGER = Logger.getLogger("UtilityBillApp");
    private static final String LOG_FILE = "utility-bill.log";
    private static boolean initialized = false;

    static {
        initialize();
    }

    private AppLogger() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Initializes the logging configuration with console and file handlers.
     */
    private static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            // Remove default handlers
            Logger rootLogger = Logger.getLogger("");
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // Create custom formatter
            Formatter formatter = new CustomFormatter();

            // Console handler - INFO level and above
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(formatter);
            LOGGER.addHandler(consoleHandler);

            // File handler - ALL levels for debugging
            try {
                FileHandler fileHandler = new FileHandler(LOG_FILE, 1024 * 1024, 3, true);
                fileHandler.setLevel(Level.ALL);
                fileHandler.setFormatter(formatter);
                LOGGER.addHandler(fileHandler);
            } catch (IOException e) {
                System.err.println("Could not create log file: " + e.getMessage());
            }

            LOGGER.setLevel(Level.ALL);
            LOGGER.setUseParentHandlers(false);

            initialized = true;
            LOGGER.info("Logging initialized successfully");

        } catch (Exception e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
        }
    }

    /**
     * Logs an informational message.
     *
     * @param message the message to log
     */
    public static void info(String message) {
        LOGGER.info(message);
    }

    /**
     * Logs an informational message with context.
     *
     * @param context the class or context name
     * @param message the message to log
     */
    public static void info(String context, String message) {
        LOGGER.info("[" + context + "] " + message);
    }

    /**
     * Logs a warning message.
     *
     * @param message the message to log
     */
    public static void warning(String message) {
        LOGGER.warning(message);
    }

    /**
     * Logs a warning message with exception details.
     *
     * @param message   the message to log
     * @param throwable the exception to log
     */
    public static void warning(String message, Throwable throwable) {
        LOGGER.log(Level.WARNING, message, throwable);
    }

    /**
     * Logs a warning message with context.
     *
     * @param context the class or context name
     * @param message the message to log
     */
    public static void warning(String context, String message) {
        LOGGER.warning("[" + context + "] " + message);
    }

    /**
     * Logs a warning message with context and exception details.
     *
     * @param context   the class or context name
     * @param message   the message to log
     * @param throwable the exception to log
     */
    public static void warning(String context, String message, Throwable throwable) {
        LOGGER.log(Level.WARNING, "[" + context + "] " + message, throwable);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     */
    public static void error(String message) {
        LOGGER.severe(message);
    }

    /**
     * Logs an error message with context.
     *
     * @param context the class or context name
     * @param message the message to log
     */
    public static void error(String context, String message) {
        LOGGER.severe("[" + context + "] " + message);
    }

    /**
     * Logs an error message with exception details.
     *
     * @param message   the message to log
     * @param throwable the exception to log
     */
    public static void error(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }

    /**
     * Logs an error message with context and exception details.
     *
     * @param context   the class or context name
     * @param message   the message to log
     * @param throwable the exception to log
     */
    public static void error(String context, String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "[" + context + "] " + message, throwable);
    }

    /**
     * Logs a debug message (FINE level).
     *
     * @param message the message to log
     */
    public static void debug(String message) {
        LOGGER.fine(message);
    }

    /**
     * Logs a debug message with context.
     *
     * @param context the class or context name
     * @param message the message to log
     */
    public static void debug(String context, String message) {
        LOGGER.fine("[" + context + "] " + message);
    }

    /**
     * Logs entry into a method (for tracing).
     *
     * @param className  the class name
     * @param methodName the method name
     */
    public static void entering(String className, String methodName) {
        LOGGER.entering(className, methodName);
    }

    /**
     * Logs exit from a method (for tracing).
     *
     * @param className  the class name
     * @param methodName the method name
     */
    public static void exiting(String className, String methodName) {
        LOGGER.exiting(className, methodName);
    }

    /**
     * Custom log formatter with timestamp and level formatting.
     */
    private static class CustomFormatter extends Formatter {

        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();

            // Timestamp
            sb.append(LocalDateTime.now().format(DATE_FORMAT));
            sb.append(" ");

            // Level with padding
            sb.append(String.format("%-7s", record.getLevel().getName()));
            sb.append(" ");

            // Message
            sb.append(formatMessage(record));
            sb.append(System.lineSeparator());

            // Exception if present
            if (record.getThrown() != null) {
                sb.append("  Exception: ");
                sb.append(record.getThrown().getClass().getName());
                sb.append(": ");
                sb.append(record.getThrown().getMessage());
                sb.append(System.lineSeparator());

                for (StackTraceElement element : record.getThrown().getStackTrace()) {
                    sb.append("    at ");
                    sb.append(element.toString());
                    sb.append(System.lineSeparator());
                }
            }

            return sb.toString();
        }
    }
}
