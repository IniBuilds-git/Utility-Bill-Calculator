package com.utilitybill.exception;

/**
 * Base exception class for all utility bill application exceptions.
 * This class serves as the root of the exception hierarchy for the application,
 * enabling consistent exception handling across all modules.
 *
 * <p>Design Pattern: Exception Hierarchy - All custom exceptions extend this base class,
 * allowing for both specific and general exception catching.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class UtilityBillException extends Exception {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Error code for programmatic error identification */
    private final String errorCode;

    /**
     * Constructs a new UtilityBillException with the specified detail message.
     *
     * @param message the detail message explaining the exception
     */
    public UtilityBillException(String message) {
        super(message);
        this.errorCode = "UB000";
    }

    /**
     * Constructs a new UtilityBillException with the specified detail message and error code.
     *
     * @param message   the detail message explaining the exception
     * @param errorCode a unique code identifying the error type
     */
    public UtilityBillException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new UtilityBillException with the specified detail message and cause.
     *
     * @param message the detail message explaining the exception
     * @param cause   the underlying cause of this exception
     */
    public UtilityBillException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UB000";
    }

    /**
     * Constructs a new UtilityBillException with the specified detail message, error code, and cause.
     *
     * @param message   the detail message explaining the exception
     * @param errorCode a unique code identifying the error type
     * @param cause     the underlying cause of this exception
     */
    public UtilityBillException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Gets the error code associated with this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns a formatted string representation of this exception.
     *
     * @return formatted exception details including error code and message
     */
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", errorCode, getClass().getSimpleName(), getMessage());
    }
}

