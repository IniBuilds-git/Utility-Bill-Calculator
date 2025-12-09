package com.utilitybill.dao;

import com.google.gson.reflect.TypeToken;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Customer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class CustomerDAO extends AbstractJsonDAO<Customer, String> {

    private static volatile CustomerDAO instance;
    private static final Type CUSTOMER_LIST_TYPE = new TypeToken<List<Customer>>(){}.getType();

    private CustomerDAO() {
        super("customers.json");
    }

    public static CustomerDAO getInstance() {
        if (instance == null) {
            synchronized (CustomerDAO.class) {
                if (instance == null) {
                    instance = new CustomerDAO();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getId(Customer entity) {
        return entity.getCustomerId();
    }

    @Override
    protected Type getEntityListType() {
        return CUSTOMER_LIST_TYPE;
    }

    public Optional<Customer> findByAccountNumber(String accountNumber) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(c -> c.getAccountNumber().equals(accountNumber))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Customer> findByEmail(String email) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(c -> c.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Customer> findAllActive() throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(Customer::isActive)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Customer> searchByName(String name) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            String searchTerm = name.toLowerCase();
            return cache.values().stream()
                    .filter(c -> c.getFullName().toLowerCase().contains(searchTerm))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Customer> findByPostcode(String postcode) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            String searchPostcode = postcode.toUpperCase().replace(" ", "");
            return cache.values().stream()
                    .filter(c -> c.getServiceAddress() != null &&
                            c.getServiceAddress().getPostcode() != null &&
                            c.getServiceAddress().getPostcode().toUpperCase()
                                    .replace(" ", "").startsWith(searchPostcode))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Customer> findCustomersWithDebt() throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(Customer::hasDebt)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Customer> findByType(Customer.CustomerType type) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(c -> c.getCustomerType() == type)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean emailExists(String email) throws DataPersistenceException {
        return findByEmail(email).isPresent();
    }

    public boolean accountNumberExists(String accountNumber) throws DataPersistenceException {
        return findByAccountNumber(accountNumber).isPresent();
    }
}

