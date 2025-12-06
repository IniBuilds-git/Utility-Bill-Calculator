package com.utilitybill.dao;

import com.google.gson.reflect.TypeToken;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.User;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User entities.
 * Handles persistence of user data to JSON files.
 *
 * <p>Design Pattern: Singleton - Only one instance manages user data.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class UserDAO extends AbstractJsonDAO<User, String> {

    /** Singleton instance */
    private static volatile UserDAO instance;

    /** Type token for JSON deserialization */
    private static final Type USER_LIST_TYPE = new TypeToken<List<User>>(){}.getType();

    /**
     * Private constructor for singleton pattern.
     */
    private UserDAO() {
        super("users.json");
    }

    /**
     * Gets the singleton instance.
     *
     * @return the UserDAO instance
     */
    public static UserDAO getInstance() {
        if (instance == null) {
            synchronized (UserDAO.class) {
                if (instance == null) {
                    instance = new UserDAO();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getId(User entity) {
        return entity.getUserId();
    }

    @Override
    protected Type getEntityListType() {
        return USER_LIST_TYPE;
    }

    /**
     * Finds a user by username.
     *
     * @param username the username to search
     * @return an Optional containing the user if found
     * @throws DataPersistenceException if the operation fails
     */
    public Optional<User> findByUsername(String username) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase(username))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds a user by email.
     *
     * @param email the email to search
     * @return an Optional containing the user if found
     * @throws DataPersistenceException if the operation fails
     */
    public Optional<User> findByEmail(String email) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all active users.
     *
     * @return list of active users
     * @throws DataPersistenceException if the operation fails
     */
    public List<User> findAllActive() throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(User::isActive)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all users with a specific role.
     *
     * @param role the role to filter by
     * @return list of users with the role
     * @throws DataPersistenceException if the operation fails
     */
    public List<User> findByRole(User.UserRole role) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(u -> u.getRole() == role)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if a username already exists.
     *
     * @param username the username to check
     * @return true if the username exists
     * @throws DataPersistenceException if the operation fails
     */
    public boolean usernameExists(String username) throws DataPersistenceException {
        return findByUsername(username).isPresent();
    }

    /**
     * Checks if an email already exists.
     *
     * @param email the email to check
     * @return true if the email exists
     * @throws DataPersistenceException if the operation fails
     */
    public boolean emailExists(String email) throws DataPersistenceException {
        return findByEmail(email).isPresent();
    }
}

