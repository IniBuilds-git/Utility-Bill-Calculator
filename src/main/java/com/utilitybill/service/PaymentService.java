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

public class PaymentService {

    private static volatile PaymentService instance;
    private final PaymentDAO paymentDAO;
    private final InvoiceDAO invoiceDAO;
    private final CustomerService customerService;

    private PaymentService() {
        this.paymentDAO = PaymentDAO.getInstance();
        this.invoiceDAO = InvoiceDAO.getInstance();
        this.customerService = CustomerService.getInstance();
    }

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

    public Payment recordPayment(String customerId, String invoiceId, BigDecimal amount, Payment.PaymentMethod paymentMethod)
            throws ValidationException, CustomerNotFoundException, InsufficientPaymentException, DataPersistenceException {
        return recordPayment(invoiceId, amount, paymentMethod, null);
    }

    public Payment recordPayment(String invoiceId, BigDecimal amount, Payment.PaymentMethod paymentMethod, String notes)
            throws ValidationException, CustomerNotFoundException, InsufficientPaymentException, DataPersistenceException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InsufficientPaymentException.invalidAmount(amount);
        }

        Optional<Invoice> invoiceOpt = invoiceDAO.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            throw new ValidationException("invoice", "Invoice not found");
        }
        Invoice invoice = invoiceOpt.get();

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new ValidationException("invoice", "Invoice is already fully paid");
        }
        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
            throw new ValidationException("invoice", "Cannot pay a cancelled invoice");
        }

        Customer customer = customerService.getCustomerById(invoice.getCustomerId());

        Payment payment = new Payment(
                invoice.getCustomerId(),
                invoice.getAccountNumber(),
                invoiceId,
                invoice.getInvoiceNumber(),
                amount,
                paymentMethod
        );
        payment.setNotes(notes);

        paymentDAO.save(payment);

        invoice.applyPayment(amount);
        invoiceDAO.update(invoice);

        customer.creditAccount(amount);
        customerService.updateCustomer(customer);

        return payment;
    }

    public Payment recordAccountPayment(String customerId, BigDecimal amount, Payment.PaymentMethod paymentMethod)
            throws ValidationException, CustomerNotFoundException, InsufficientPaymentException, DataPersistenceException {
        return recordAccountPayment(customerId, amount, paymentMethod, null);
    }

    public Payment recordAccountPayment(String customerId, BigDecimal amount,
                                         Payment.PaymentMethod paymentMethod, String notes)
            throws ValidationException, CustomerNotFoundException, InsufficientPaymentException, DataPersistenceException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InsufficientPaymentException.invalidAmount(amount);
        }

        Customer customer = customerService.getCustomerById(customerId);

        Payment payment = new Payment(
                customerId,
                customer.getAccountNumber(),
                amount,
                paymentMethod
        );
        payment.setNotes(notes);

        paymentDAO.save(payment);

        customer.creditAccount(amount);
        customerService.updateCustomer(customer);

        return payment;
    }

    public Payment getPaymentById(String paymentId) throws DataPersistenceException {
        return paymentDAO.findById(paymentId).orElse(null);
    }

    public Payment getPaymentByReference(String referenceNumber) throws DataPersistenceException {
        return paymentDAO.findByReferenceNumber(referenceNumber).orElse(null);
    }

    public List<Payment> getCustomerPayments(String customerId) throws DataPersistenceException {
        return paymentDAO.findByCustomerId(customerId);
    }

    public List<Payment> getInvoicePayments(String invoiceId) throws DataPersistenceException {
        return paymentDAO.findByInvoiceId(invoiceId);
    }

    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate)
            throws DataPersistenceException {
        return paymentDAO.findByDateRange(startDate, endDate);
    }

    public BigDecimal getTotalPaymentsByCustomer(String customerId) throws DataPersistenceException {
        return paymentDAO.getTotalPaymentsByCustomer(customerId);
    }

    public BigDecimal getTotalPaymentsByDateRange(LocalDate startDate, LocalDate endDate)
            throws DataPersistenceException {
        return paymentDAO.getTotalPaymentsByDateRange(startDate, endDate);
    }

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

        payment.markAsRefunded(reason);
        paymentDAO.update(payment);

        Customer customer = customerService.getCustomerById(payment.getCustomerId());
        customer.debitAccount(payment.getAmount());
        customerService.updateCustomer(customer);

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

    public List<Payment> getAllPayments() throws DataPersistenceException {
        return paymentDAO.findAll();
    }
}

