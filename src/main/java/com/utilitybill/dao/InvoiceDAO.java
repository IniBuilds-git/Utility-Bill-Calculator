package com.utilitybill.dao;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InvoiceDAO extends AbstractBinaryDAO<Invoice, String> {

    private static volatile InvoiceDAO instance;

    private InvoiceDAO() {
        super("invoices.dat");
    }

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