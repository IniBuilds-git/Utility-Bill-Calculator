package com.utilitybill.dao;

import com.google.gson.reflect.TypeToken;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Invoice;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Invoice entities.
 * Handles persistence of invoice data to JSON files.
 *
 * <p>Design Pattern: Singleton - Only one instance manages invoice data.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class InvoiceDAO extends AbstractJsonDAO<Invoice, String> {

    /** Singleton instance */
    private static volatile InvoiceDAO instance;

    /** Type token for JSON deserialization */
    private static final Type INVOICE_LIST_TYPE = new TypeToken<List<Invoice>>(){}.getType();

    /**
     * Private constructor for singleton pattern.
     */
    private InvoiceDAO() {
        super("invoices.json");
    }

    /**
     * Gets the singleton instance.
     *
     * @return the InvoiceDAO instance
     */
    public static InvoiceDAO getInstance() {
        if (instance == null) {
            synchronized (InvoiceDAO.class) {
                if (instance == null) {
                    instance = new InvoiceDAO();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getId(Invoice entity) {
        return entity.getInvoiceId();
    }

    @Override
    protected Type getEntityListType() {
        return INVOICE_LIST_TYPE;
    }

    /**
     * Finds an invoice by invoice number.
     *
     * @param invoiceNumber the invoice number to search
     * @return an Optional containing the invoice if found
     * @throws DataPersistenceException if the operation fails
     */
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(i -> i.getInvoiceNumber().equals(invoiceNumber))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all invoices for a customer.
     *
     * @param customerId the customer ID
     * @return list of invoices for the customer
     * @throws DataPersistenceException if the operation fails
     */
    public List<Invoice> findByCustomerId(String customerId) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(i -> i.getCustomerId().equals(customerId))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all invoices for an account number.
     *
     * @param accountNumber the account number
     * @return list of invoices for the account
     * @throws DataPersistenceException if the operation fails
     */
    public List<Invoice> findByAccountNumber(String accountNumber) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(i -> i.getAccountNumber().equals(accountNumber))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all invoices with a specific status.
     *
     * @param status the invoice status
     * @return list of invoices with that status
     * @throws DataPersistenceException if the operation fails
     */
    public List<Invoice> findByStatus(Invoice.InvoiceStatus status) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(i -> i.getStatus() == status)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all overdue invoices.
     *
     * @return list of overdue invoices
     * @throws DataPersistenceException if the operation fails
     */
    public List<Invoice> findOverdue() throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(Invoice::isOverdue)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all unpaid invoices (pending or partial).
     *
     * @return list of unpaid invoices
     * @throws DataPersistenceException if the operation fails
     */
    public List<Invoice> findUnpaid() throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PENDING ||
                            i.getStatus() == Invoice.InvoiceStatus.PARTIAL ||
                            i.getStatus() == Invoice.InvoiceStatus.OVERDUE)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds invoices within a date range (by issue date).
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return list of invoices in the range
     * @throws DataPersistenceException if the operation fails
     */
    public List<Invoice> findByDateRange(LocalDate startDate, LocalDate endDate) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(i -> i.getIssueDate() != null &&
                            !i.getIssueDate().isBefore(startDate) &&
                            !i.getIssueDate().isAfter(endDate))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds the most recent invoice for a customer.
     *
     * @param customerId the customer ID
     * @return an Optional containing the most recent invoice
     * @throws DataPersistenceException if the operation fails
     */
    public Optional<Invoice> findLatestByCustomerId(String customerId) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(i -> i.getCustomerId().equals(customerId))
                    .max((i1, i2) -> i1.getIssueDate().compareTo(i2.getIssueDate()));
        } finally {
            lock.readLock().unlock();
        }
    }
}

