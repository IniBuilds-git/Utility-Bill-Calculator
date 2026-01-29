package com.utilitybill.dao;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.User;

import java.util.List;
import java.util.Optional;

public class UserDAO extends AbstractBinaryDAO<User, String> {

    private static volatile UserDAO instance;

    private UserDAO() {
        super("users.dat");
    }

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

    public boolean usernameExists(String username) throws DataPersistenceException {
        return findByUsername(username).isPresent();
    }

    public boolean emailExists(String email) throws DataPersistenceException {
        return findByEmail(email).isPresent();
    }
}
