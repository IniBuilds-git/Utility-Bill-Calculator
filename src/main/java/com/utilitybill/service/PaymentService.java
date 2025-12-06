package com.utilitybill.service;

import com.utilitybill.dao.InvoiceDAO;
import com.utilitybill.dao.PaymentDAO;
import com.utilitybill.exception.CustomerNotFoundException;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.InsufficientPaymentException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.Customer;
import com.utilitybill.model.Invoice;
import com.utilitybill.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for payment processing operations.
 * Handles payment recording, invoice application, and payment history.
 *
 * <p>Design Pattern: Singleton - Only one instance manages payment operations.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class PaymentService {

    /** Singleton instance */
    private static volatile PaymentService instance;

    /** Data access objects */
    private final PaymentDAO paymentDAO;
    private final InvoiceDAO invoiceDAO;
    private final CustomerService customerService;

    /**
     * Private constructor for singleton pattern.
     */
    private PaymentService() {
        this.paymentDAO = PaymentDAO.getInstance();
        this.invoiceDAO = InvoiceDAO.getInstance();
        this.customerService = CustomerService.getInstance();
    }

    /**
     * Gets the singleton instance.
     *
     * @return the PaymentService instance
     */
    public static PaymentService getInstance() {
        if (instance == null) {
            synchronized (PaymentService.class) {
                if (instance == null) {
                    instance = new PaymentService();
                }
            }
        }
        return instance;
    }

    /**
     * Records a payment against an invoice (with customer ID).
     *
     * @param customerId    the customer ID
     * @param invoiceId     the invoice ID
     * @param amount        the payment amount
     * @param paymentMethod the payment method
     * @return the created payment
     * @throws ValidationException          if validation fails
     * @throws CustomerNotFoundException    if customer not found
     * @throws InsufficientPaymentException if payment amount is invalid
     * @throws DataPersistenceException     if data access fails
     */
    public Payment recordPayment(String customerId, String invoiceId, BigDecimal amount, Payment.PaymentMethod paymentMethod)
            throws ValidationException, CustomerNotFoundException, InsufficientPaymentException, DataPersistenceException {
        return recordPayment(invoiceId, amount, paymentMethod, null);
    }

    /**
     * Records a payment against an invoice.
     *
     * @param invoiceId     the invoice ID
     * @param amount        the payment amount
     * @param paymentMethod the payment method
     * @param notes         optional notes
     * @return the created payment
     * @throws ValidationException          if validation fails
     * @throws CustomerNotFoundException    if customer not found
     * @throws InsufficientPaymentException if payment amount is invalid
     * @throws DataPersistenceException     if data access fails
     */
    public Payment recordPayment(String invoiceId, BigDecimal amount, Payment.PaymentMethod paymentMethod, String notes)
            throws ValidationException, CustomerNotFoundException, InsufficientPaymentException, DataPersistenceException {

        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InsufficientPaymentException.invalidAmount(amount);
        }

        // Get invoice
        Optional<Invoice> invoiceOpt = invoiceDAO.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            throw new ValidationException("invoice", "Invoice not found");
        }
        Invoice invoice = invoiceOpt.get();

        // Check invoice can be paid
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new ValidationException("invoice", "Invoice is already fully paid");
        }
        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
            throw new ValidationException("invoice", "Cannot pay a cancelled invoice");
        }

        // Get customer
        Customer customer = customerService.getCustomerById(invoice.getCustomerId());

        // Create payment
        Payment payment = new Payment(
                invoice.getCustomerId(),
                invoice.getAccountNumber(),
                invoiceId,
                invoice.getInvoiceNumber(),
                amount,
                paymentMethod
        );
        payment.setNotes(notes);

        // Save payment
        paymentDAO.save(payment);

        // Apply payment to invoice
        invoice.applyPayment(amount);
        invoiceDAO.update(invoice);

        // Credit customer account
        customer.creditAccount(amount);
        customerService.updateCustomer(customer);

        return payment;
    }

    /**
     * Records a payment to a customer account (not tied to specific invoice).
     *
     * @param customerId    the customer ID
     * @param amount        the payment amount
     * @param paymentMethod the payment method
     * @return the created payment
     * @throws ValidationException          if validation fails
     * @throws CustomerNotFoundException    if customer not found
     * @throws InsufficientPaymentException if payment amount is invalid
     * @throws DataPersistenceException     if data access fails
     */
    public Payment recordAccountPayment(String customerId, BigDecimal amount, Payment.PaymentMethod paymentMethod)
            throws ValidationException, CustomerNotFoundException, InsufficientPaymentException, DataPersistenceException {
        return recordAccountPayment(customerId, amount, paymentMethod, null);
    }

    /**
     * Records a payment to a customer account (not tied to specific invoice).
     *
     * @param customerId    the customer ID
     * @param amount        the payment amount
     * @param paymentMethod the payment method
     * @param notes         optional notes
     * @return the created payment
     * @throws ValidationException          if validation fails
     * @throws CustomerNotFoundException    if customer not found
     * @throws InsufficientPaymentException if payment amount is invalid
     * @throws DataPersistenceException     if data access fails
     */
    public Payment recordAccountPayment(String customerId, BigDecimal amount,
                                         Payment.PaymentMethod paymentMethod, String notes)
            throws ValidationException, CustomerNotFoundException, InsufficientPaymentException, DataPersistenceException {

        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InsufficientPaymentException.invalidAmount(amount);
        }

        // Get customer
        Customer customer = customerService.getCustomerById(customerId);

        // Create payment
        Payment payment = new Payment(
                customerId,
                customer.getAccountNumber(),
                amount,
                paymentMethod
        );
        payment.setNotes(notes);

        // Save payment
        paymentDAO.save(payment);

        // Credit customer account
        customer.creditAccount(amount);
        customerService.updateCustomer(customer);

        return payment;
    }

    /**
     * Gets a payment by ID.
     *
     * @param paymentId the payment ID
     * @return the payment, or null if not found
     * @throws DataPersistenceException if data access fails
     */
    public Payment getPaymentById(String paymentId) throws DataPersistenceException {
        return paymentDAO.findById(paymentId).orElse(null);
    }

    /**
     * Gets a payment by reference number.
     *
     * @param referenceNumber the payment reference number
     * @return the payment, or null if not found
     * @throws DataPersistenceException if data access fails
     */
    public Payment getPaymentByReference(String referenceNumber) throws DataPersistenceException {
        return paymentDAO.findByReferenceNumber(referenceNumber).orElse(null);
    }

    /**
     * Gets all payments for a customer.
     *
     * @param customerId the customer ID
     * @return list of payments
     * @throws DataPersistenceException if data access fails
     */
    public List<Payment> getCustomerPayments(String customerId) throws DataPersistenceException {
        return paymentDAO.findByCustomerId(customerId);
    }

    /**
     * Gets all payments for an invoice.
     *
     * @param invoiceId the invoice ID
     * @return list of payments
     * @throws DataPersistenceException if data access fails
     */
    public List<Payment> getInvoicePayments(String invoiceId) throws DataPersistenceException {
        return paymentDAO.findByInvoiceId(invoiceId);
    }

    /**
     * Gets payments within a date range.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of payments
     * @throws DataPersistenceException if data access fails
     */
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate)
            throws DataPersistenceException {
        return paymentDAO.findByDateRange(startDate, endDate);
    }

    /**
     * Gets total payments for a customer.
     *
     * @param customerId the customer ID
     * @return total payment amount
     * @throws DataPersistenceException if data access fails
     */
    public BigDecimal getTotalPaymentsByCustomer(String customerId) throws DataPersistenceException {
        return paymentDAO.getTotalPaymentsByCustomer(customerId);
    }

    /**
     * Gets total payments for a date range.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return total payment amount
     * @throws DataPersistenceException if data access fails
     */
    public BigDecimal getTotalPaymentsByDateRange(LocalDate startDate, LocalDate endDate)
            throws DataPersistenceException {
        return paymentDAO.getTotalPaymentsByDateRange(startDate, endDate);
    }

    /**
     * Refunds a payment.
     *
     * @param paymentId the payment ID
     * @param reason    the reason for refund
     * @throws ValidationException       if validation fails
     * @throws CustomerNotFoundException if customer not found
     * @throws DataPersistenceException  if data access fails
     */
    public void refundPayment(String paymentId, String reason)
            throws ValidationException, CustomerNotFoundException, DataPersistenceException {

        Optional<Payment> paymentOpt = paymentDAO.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new ValidationException("payment", "Payment not found");
        }

        Payment payment = paymentOpt.get();

        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            throw new ValidationException("payment", "Payment has already been refunded");
        }

        // Mark payment as refunded
        payment.markAsRefunded(reason);
        paymentDAO.update(payment);

        // Debit customer account
        Customer customer = customerService.getCustomerById(payment.getCustomerId());
        customer.debitAccount(payment.getAmount());
        customerService.updateCustomer(customer);

        // If invoice payment, update invoice
        if (payment.isInvoicePayment()) {
            Optional<Invoice> invoiceOpt = invoiceDAO.findById(payment.getInvoiceId());
            if (invoiceOpt.isPresent()) {
                Invoice invoice = invoiceOpt.get();
                invoice.setAmountPaid(invoice.getAmountPaid().subtract(payment.getAmount()));
                invoice.setBalanceDue(invoice.getTotalAmount().subtract(invoice.getAmountPaid()));
                invoice.updateStatus();
                invoiceDAO.update(invoice);
            }
        }
    }

    /**
     * Gets all payments.
     *
     * @return list of all payments
     * @throws DataPersistenceException if data access fails
     */
    public List<Payment> getAllPayments() throws DataPersistenceException {
        return paymentDAO.findAll();
    }
}

