package com.utilitybill.dao;

import com.google.gson.reflect.TypeToken;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Customer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Customer entities.
 * Handles persistence of customer data to JSON files.
 *
 * <p>Design Pattern: Singleton - Only one instance manages customer data.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class CustomerDAO extends AbstractJsonDAO<Customer, String> {

    /** Singleton instance */
    private static volatile CustomerDAO instance;

    /** Type token for JSON deserialization */
    private static final Type CUSTOMER_LIST_TYPE = new TypeToken<List<Customer>>(){}.getType();

    /**
     * Private constructor for singleton pattern.
     */
    private CustomerDAO() {
        super("customers.json");
    }

    /**
     * Gets the singleton instance.
     *
     * @return the CustomerDAO instance
     */
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

    /**
     * Finds a customer by account number.
     *
     * @param accountNumber the account number to search
     * @return an Optional containing the customer if found
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Finds a customer by email.
     *
     * @param email the email to search
     * @return an Optional containing the customer if found
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Finds all active customers.
     *
     * @return list of active customers
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Searches customers by name (partial match).
     *
     * @param name the name to search (case-insensitive)
     * @return list of matching customers
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Finds customers by postcode.
     *
     * @param postcode the postcode to search
     * @return list of customers at that postcode
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Finds customers with outstanding debt.
     *
     * @return list of customers with negative balance
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Finds customers by type.
     *
     * @param type the customer type
     * @return list of customers of that type
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Checks if an account number already exists.
     *
     * @param accountNumber the account number to check
     * @return true if the account number exists
     * @throws DataPersistenceException if the operation fails
     */
    public boolean accountNumberExists(String accountNumber) throws DataPersistenceException {
        return findByAccountNumber(accountNumber).isPresent();
    }
}

