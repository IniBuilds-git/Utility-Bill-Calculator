package com.utilitybill.dao;

import com.google.gson.reflect.TypeToken;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Payment;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Payment entities.
 * Handles persistence of payment data to JSON files.
 *
 * <p>Design Pattern: Singleton - Only one instance manages payment data.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class PaymentDAO extends AbstractJsonDAO<Payment, String> {

    /** Singleton instance */
    private static volatile PaymentDAO instance;

    /** Type token for JSON deserialization */
    private static final Type PAYMENT_LIST_TYPE = new TypeToken<List<Payment>>(){}.getType();

    /**
     * Private constructor for singleton pattern.
     */
    private PaymentDAO() {
        super("payments.json");
    }

    /**
     * Gets the singleton instance.
     *
     * @return the PaymentDAO instance
     */
    public static PaymentDAO getInstance() {
        if (instance == null) {
            synchronized (PaymentDAO.class) {
                if (instance == null) {
                    instance = new PaymentDAO();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getId(Payment entity) {
        return entity.getPaymentId();
    }

    @Override
    protected Type getEntityListType() {
        return PAYMENT_LIST_TYPE;
    }

    /**
     * Finds a payment by reference number.
     *
     * @param referenceNumber the reference number to search
     * @return an Optional containing the payment if found
     * @throws DataPersistenceException if the operation fails
     */
    public Optional<Payment> findByReferenceNumber(String referenceNumber) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(p -> p.getReferenceNumber().equals(referenceNumber))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all payments for a customer.
     *
     * @param customerId the customer ID
     * @return list of payments for the customer
     * @throws DataPersistenceException if the operation fails
     */
    public List<Payment> findByCustomerId(String customerId) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(p -> p.getCustomerId().equals(customerId))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all payments for an account number.
     *
     * @param accountNumber the account number
     * @return list of payments for the account
     * @throws DataPersistenceException if the operation fails
     */
    public List<Payment> findByAccountNumber(String accountNumber) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(p -> p.getAccountNumber().equals(accountNumber))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all payments for a specific invoice.
     *
     * @param invoiceId the invoice ID
     * @return list of payments for the invoice
     * @throws DataPersistenceException if the operation fails
     */
    public List<Payment> findByInvoiceId(String invoiceId) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(p -> invoiceId.equals(p.getInvoiceId()))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds payments by status.
     *
     * @param status the payment status
     * @return list of payments with that status
     * @throws DataPersistenceException if the operation fails
     */
    public List<Payment> findByStatus(Payment.PaymentStatus status) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(p -> p.getStatus() == status)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds payments by method.
     *
     * @param method the payment method
     * @return list of payments using that method
     * @throws DataPersistenceException if the operation fails
     */
    public List<Payment> findByMethod(Payment.PaymentMethod method) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(p -> p.getPaymentMethod() == method)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds payments within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return list of payments in the range
     * @throws DataPersistenceException if the operation fails
     */
    public List<Payment> findByDateRange(LocalDate startDate, LocalDate endDate) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(p -> p.getPaymentDate() != null &&
                            !p.getPaymentDate().isBefore(startDate) &&
                            !p.getPaymentDate().isAfter(endDate))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Calculates total payments for a customer.
     *
     * @param customerId the customer ID
     * @return total amount paid
     * @throws DataPersistenceException if the operation fails
     */
    public BigDecimal getTotalPaymentsByCustomer(String customerId) throws DataPersistenceException {
        return findByCustomerId(customerId).stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates total payments for a date range.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return total amount received
     * @throws DataPersistenceException if the operation fails
     */
    public BigDecimal getTotalPaymentsByDateRange(LocalDate startDate, LocalDate endDate) throws DataPersistenceException {
        return findByDateRange(startDate, endDate).stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

