package com.utilitybill.exception;

/**
 * Exception thrown when user authentication fails due to invalid credentials.
 * This exception is thrown during login attempts when the username or password
 * does not match the stored credentials.
 *
 * <p>Error Codes:</p>
 * <ul>
 *   <li>AUTH001 - Invalid username</li>
 *   <li>AUTH002 - Invalid password</li>
 *   <li>AUTH003 - Account locked</li>
 *   <li>AUTH004 - Account disabled</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class InvalidCredentialsException extends UtilityBillException {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** The username that failed authentication */
    private final String username;

    /** Number of failed attempts */
    private final int failedAttempts;

    /**
     * Constructs a new InvalidCredentialsException with a default message.
     *
     * @param username the username that failed authentication
     */
    public InvalidCredentialsException(String username) {
        super("Invalid credentials for user: " + username, "AUTH001");
        this.username = username;
        this.failedAttempts = 1;
    }

    /**
     * Constructs a new InvalidCredentialsException with a custom message.
     *
     * @param username the username that failed authentication
     * @param message  custom error message
     */
    public InvalidCredentialsException(String username, String message) {
        super(message, "AUTH001");
        this.username = username;
        this.failedAttempts = 1;
    }

    /**
     * Constructs a new InvalidCredentialsException with failed attempt tracking.
     *
     * @param username       the username that failed authentication
     * @param failedAttempts the number of consecutive failed login attempts
     */
    public InvalidCredentialsException(String username, int failedAttempts) {
        super(String.format("Invalid credentials for user: %s (Attempt %d)", username, failedAttempts),
                failedAttempts >= 3 ? "AUTH003" : "AUTH001");
        this.username = username;
        this.failedAttempts = failedAttempts;
    }

    /**
     * Gets the username that failed authentication.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the number of failed authentication attempts.
     *
     * @return the number of failed attempts
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Checks if the account should be locked based on failed attempts.
     *
     * @return true if failed attempts exceed the threshold (3)
     */
    public boolean shouldLockAccount() {
        return failedAttempts >= 3;
    }
}

