package com.utilitybill.dao;

import com.google.gson.reflect.TypeToken;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Payment;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PaymentDAO extends AbstractJsonDAO<Payment, String> {

    private static volatile PaymentDAO instance;
    private static final Type PAYMENT_LIST_TYPE = new TypeToken<List<Payment>>(){}.getType();

    private PaymentDAO() {
        super("payments.json");
    }

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

    public BigDecimal getTotalPaymentsByCustomer(String customerId) throws DataPersistenceException {
        return findByCustomerId(customerId).stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalPaymentsByDateRange(LocalDate startDate, LocalDate endDate) throws DataPersistenceException {
        return findByDateRange(startDate, endDate).stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

