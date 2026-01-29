package com.utilitybill.service;

import com.utilitybill.dao.UserDAO;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.InvalidCredentialsException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.User;
import com.utilitybill.util.PasswordUtil;
import com.utilitybill.util.ValidationUtil;

import com.utilitybill.util.AppLogger;
import java.util.Optional;

public class AuthenticationService {

    private static final String CLASS_NAME = AuthenticationService.class.getName();

    private static volatile AuthenticationService instance;
    private final UserDAO userDAO;
    private User currentUser;

    private AuthenticationService() {
        this.userDAO = UserDAO.getInstance();
        initializeDefaultAdmin();
    }

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

    private void initializeDefaultAdmin() {
        try {
            if (userDAO.count() == 0) {
                User admin = new User(
                        "admin",
                        PasswordUtil.hashPassword("Admin123"),
                        "System Administrator",
                        "admin@utilitybill.com",
                        User.UserRole.ADMIN);
                userDAO.save(admin);
                AppLogger.info(CLASS_NAME, "Default admin user created. Username: admin, Password: Admin123");
            }
        } catch (DataPersistenceException e) {
            AppLogger.warning(CLASS_NAME, "Could not initialize default admin: " + e.getMessage(), e);
        }
    }

    public User login(String username, String password) throws InvalidCredentialsException, DataPersistenceException {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidCredentialsException(username, "Username is required");
        }
        if (password == null || password.isEmpty()) {
            throw new InvalidCredentialsException(username, "Password is required");
        }

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException(username);
        }

        User user = userOpt.get();

        if (user.isLocked()) {
            throw new InvalidCredentialsException(username, "Account is locked due to multiple failed attempts");
        }

        if (!user.isActive()) {
            throw new InvalidCredentialsException(username, "Account is deactivated");
        }

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            boolean shouldLock = user.recordFailedLogin();
            userDAO.update(user);

            if (shouldLock) {
                throw new InvalidCredentialsException(username, user.getFailedLoginAttempts());
            }
            throw new InvalidCredentialsException(username);
        }

        user.recordSuccessfulLogin();
        userDAO.update(user);
        currentUser = user;

        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    public boolean canWrite() {
        return currentUser != null && currentUser.getRole().canWrite();
    }

    public boolean canDelete() {
        return currentUser != null && currentUser.getRole().canDelete();
    }

    public User registerUser(String username, String password, String fullName, String email, User.UserRole role)
            throws ValidationException, DataPersistenceException {

        ValidationUtil.requireNonEmpty(username, "username");
        ValidationUtil.validatePassword(password);
        ValidationUtil.requireNonEmpty(fullName, "fullName");
        ValidationUtil.validateEmail(email);

        if (userDAO.usernameExists(username)) {
            throw new ValidationException("username", "Username already exists");
        }
        if (userDAO.emailExists(email)) {
            throw new ValidationException("email", "Email already registered");
        }

        User user = new User(
                username.trim(),
                PasswordUtil.hashPassword(password),
                fullName.trim(),
                email.trim().toLowerCase(),
                role);
        userDAO.save(user);

        return user;
    }

    public void changePassword(String userId, String currentPassword, String newPassword)
            throws InvalidCredentialsException, ValidationException, DataPersistenceException {

        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("unknown", "User not found");
        }

        User user = userOpt.get();

        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException(user.getUsername(), "Current password is incorrect");
        }

        ValidationUtil.validatePassword(newPassword);

        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        userDAO.update(user);
    }

    public void unlockAccount(String userId) throws DataPersistenceException {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.unlock();
            userDAO.update(user);
        }
    }

    public void deactivateAccount(String userId) throws DataPersistenceException {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            userDAO.update(user);
        }
    }

    public void reactivateAccount(String userId) throws DataPersistenceException {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(true);
            userDAO.update(user);
        }
    }
}
