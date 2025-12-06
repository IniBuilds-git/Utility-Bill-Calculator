package com.utilitybill.service;

import com.utilitybill.dao.UserDAO;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.InvalidCredentialsException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.User;
import com.utilitybill.util.PasswordUtil;
import com.utilitybill.util.ValidationUtil;

import java.util.Optional;

/**
 * Service class for user authentication and session management.
 * Implements the Singleton pattern to ensure only one instance manages authentication.
 *
 * <p>Design Pattern: Singleton - Only one instance handles authentication state.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class AuthenticationService {

    /** Singleton instance */
    private static volatile AuthenticationService instance;

    /** Data access object for users */
    private final UserDAO userDAO;

    /** Currently logged-in user */
    private User currentUser;

    /**
     * Private constructor for singleton pattern.
     */
    private AuthenticationService() {
        this.userDAO = UserDAO.getInstance();
        initializeDefaultAdmin();
    }

    /**
     * Gets the singleton instance.
     *
     * @return the AuthenticationService instance
     */
    public static AuthenticationService getInstance() {
        if (instance == null) {
            synchronized (AuthenticationService.class) {
                if (instance == null) {
                    instance = new AuthenticationService();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes a default admin user if no users exist.
     */
    private void initializeDefaultAdmin() {
        try {
            if (userDAO.count() == 0) {
                User admin = new User(
                        "admin",
                        PasswordUtil.hashPassword("Admin123"),
                        "System Administrator",
                        "admin@utilitybill.com",
                        User.UserRole.ADMIN
                );
                userDAO.save(admin);
                System.out.println("Default admin user created. Username: admin, Password: Admin123");
            }
        } catch (DataPersistenceException e) {
            System.err.println("Warning: Could not initialize default admin: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user with username and password.
     *
     * @param username the username
     * @param password the password
     * @return the authenticated user
     * @throws InvalidCredentialsException if authentication fails
     * @throws DataPersistenceException    if data access fails
     */
    public User login(String username, String password) throws InvalidCredentialsException, DataPersistenceException {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidCredentialsException(username, "Username is required");
        }
        if (password == null || password.isEmpty()) {
            throw new InvalidCredentialsException(username, "Password is required");
        }

        // Find user by username
        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException(username);
        }

        User user = userOpt.get();

        // Check if account is locked
        if (user.isLocked()) {
            throw new InvalidCredentialsException(username, "Account is locked due to multiple failed attempts");
        }

        // Check if account is active
        if (!user.isActive()) {
            throw new InvalidCredentialsException(username, "Account is deactivated");
        }

        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            // Record failed attempt
            boolean shouldLock = user.recordFailedLogin();
            userDAO.update(user);

            if (shouldLock) {
                throw new InvalidCredentialsException(username, user.getFailedLoginAttempts());
            }
            throw new InvalidCredentialsException(username);
        }

        // Successful login
        user.recordSuccessfulLogin();
        userDAO.update(user);
        currentUser = user;

        return user;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Gets the currently logged-in user.
     *
     * @return the current user, or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Checks if the current user has admin privileges.
     *
     * @return true if the current user is an admin
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    /**
     * Checks if the current user can perform write operations.
     *
     * @return true if the user can write
     */
    public boolean canWrite() {
        return currentUser != null && currentUser.getRole().canWrite();
    }

    /**
     * Checks if the current user can delete records.
     *
     * @return true if the user can delete
     */
    public boolean canDelete() {
        return currentUser != null && currentUser.getRole().canDelete();
    }

    /**
     * Registers a new user.
     *
     * @param username the username
     * @param password the password
     * @param fullName the user's full name
     * @param email    the user's email
     * @param role     the user's role
     * @return the created user
     * @throws ValidationException      if validation fails
     * @throws DataPersistenceException if data access fails
     */
    public User registerUser(String username, String password, String fullName, String email, User.UserRole role)
            throws ValidationException, DataPersistenceException {

        // Validate inputs
        ValidationUtil.requireNonEmpty(username, "username");
        ValidationUtil.validatePassword(password);
        ValidationUtil.requireNonEmpty(fullName, "fullName");
        ValidationUtil.validateEmail(email);

        // Check for duplicates
        if (userDAO.usernameExists(username)) {
            throw new ValidationException("username", "Username already exists");
        }
        if (userDAO.emailExists(email)) {
            throw new ValidationException("email", "Email already registered");
        }

        // Create and save user
        User user = new User(
                username.trim(),
                PasswordUtil.hashPassword(password),
                fullName.trim(),
                email.trim().toLowerCase(),
                role
        );
        userDAO.save(user);

        return user;
    }

    /**
     * Changes a user's password.
     *
     * @param userId         the user ID
     * @param currentPassword the current password (for verification)
     * @param newPassword     the new password
     * @throws InvalidCredentialsException if current password is wrong
     * @throws ValidationException         if new password is weak
     * @throws DataPersistenceException    if data access fails
     */
    public void changePassword(String userId, String currentPassword, String newPassword)
            throws InvalidCredentialsException, ValidationException, DataPersistenceException {

        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("unknown", "User not found");
        }

        User user = userOpt.get();

        // Verify current password
        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException(user.getUsername(), "Current password is incorrect");
        }

        // Validate new password
        ValidationUtil.validatePassword(newPassword);

        // Update password
        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        userDAO.update(user);
    }

    /**
     * Unlocks a user account (admin only).
     *
     * @param userId the user ID to unlock
     * @throws DataPersistenceException if data access fails
     */
    public void unlockAccount(String userId) throws DataPersistenceException {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.unlock();
            userDAO.update(user);
        }
    }

    /**
     * Deactivates a user account (admin only).
     *
     * @param userId the user ID to deactivate
     * @throws DataPersistenceException if data access fails
     */
    public void deactivateAccount(String userId) throws DataPersistenceException {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            userDAO.update(user);
        }
    }

    /**
     * Reactivates a user account (admin only).
     *
     * @param userId the user ID to reactivate
     * @throws DataPersistenceException if data access fails
     */
    public void reactivateAccount(String userId) throws DataPersistenceException {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(true);
            userDAO.update(user);
        }
    }
}

