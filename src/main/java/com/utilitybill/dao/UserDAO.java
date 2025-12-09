package com.utilitybill.dao;

import com.google.gson.reflect.TypeToken;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.User;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class UserDAO extends AbstractJsonDAO<User, String> {

    private static volatile UserDAO instance;
    private static final Type USER_LIST_TYPE = new TypeToken<List<User>>(){}.getType();

    private UserDAO() {
        super("users.json");
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

    @Override
    protected Type getEntityListType() {
        return USER_LIST_TYPE;
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

