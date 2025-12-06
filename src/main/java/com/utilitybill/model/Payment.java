package com.utilitybill.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a payment in the Utility Bill Management System.
 * A payment records money received from a customer against an invoice or account.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class Payment implements Serializable {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Counter for generating unique payment reference numbers */
    private static final AtomicLong PAYMENT_COUNTER = new AtomicLong(100000);

    /** Unique payment identifier */
    private String paymentId;

    /** Payment reference number for display (PAY-XXXXXX) */
    private String referenceNumber;

    /** Customer ID who made the payment */
    private String customerId;

    /** Customer account number */
    private String accountNumber;

    /** Invoice ID this payment is for (optional - can be account payment) */
    private String invoiceId;

    /** Invoice number */
    private String invoiceNumber;

    /** Payment amount */
    private BigDecimal amount;

    /** Payment date */
    private LocalDate paymentDate;

    /** Timestamp when payment was recorded */
    private LocalDateTime recordedAt;

    /** Payment method used */
    private PaymentMethod paymentMethod;

    /** Payment status */
    private PaymentStatus status;

    /** Transaction reference from payment provider */
    private String transactionReference;

    /** Notes about the payment */
    private String notes;

    /** User who recorded the payment */
    private String recordedBy;

    /**
     * Enum representing payment methods.
     */
    public enum PaymentMethod {
        /** Cash payment */
        CASH("Cash"),
        /** Cheque payment */
        CHEQUE("Cheque"),
        /** Bank transfer */
        BANK_TRANSFER("Bank Transfer"),
        /** Debit card */
        DEBIT_CARD("Debit Card"),
        /** Credit card */
        CREDIT_CARD("Credit Card"),
        /** Direct debit */
        DIRECT_DEBIT("Direct Debit"),
        /** Standing order */
        STANDING_ORDER("Standing Order"),
        /** Online payment */
        ONLINE("Online"),
        /** Payment point (Paypoint, PayZone) */
        PAYMENT_POINT("Payment Point"),
        /** Other method */
        OTHER("Other");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum representing payment status.
     */
    public enum PaymentStatus {
        /** Payment completed successfully */
        COMPLETED("Completed"),
        /** Payment is pending processing */
        PENDING("Pending"),
        /** Payment failed */
        FAILED("Failed"),
        /** Payment was refunded */
        REFUNDED("Refunded"),
        /** Payment was cancelled */
        CANCELLED("Cancelled");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Default constructor required for JSON deserialization.
     */
    public Payment() {
        this.paymentId = UUID.randomUUID().toString();
        this.referenceNumber = generateReferenceNumber();
        this.paymentDate = LocalDate.now();
        this.recordedAt = LocalDateTime.now();
        this.status = PaymentStatus.COMPLETED;
        this.paymentMethod = PaymentMethod.CASH;
    }

    /**
     * Constructs a new Payment with essential details.
     *
     * @param customerId    the customer ID
     * @param accountNumber the account number
     * @param amount        the payment amount
     * @param paymentMethod the payment method
     */
    public Payment(String customerId, String accountNumber, BigDecimal amount, PaymentMethod paymentMethod) {
        this();
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    /**
     * Constructs a new Payment against an invoice.
     *
     * @param customerId    the customer ID
     * @param accountNumber the account number
     * @param invoiceId     the invoice ID
     * @param invoiceNumber the invoice number
     * @param amount        the payment amount
     * @param paymentMethod the payment method
     */
    public Payment(String customerId, String accountNumber, String invoiceId,
                   String invoiceNumber, BigDecimal amount, PaymentMethod paymentMethod) {
        this(customerId, accountNumber, amount, paymentMethod);
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
    }

    /**
     * Generates a unique payment reference number.
     *
     * @return the reference number
     */
    private static String generateReferenceNumber() {
        return String.format("PAY-%06d", PAYMENT_COUNTER.getAndIncrement());
    }

    // ==================== Getters and Setters ====================

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    // ==================== Business Methods ====================

    /**
     * Checks if the payment is successful.
     *
     * @return true if completed
     */
    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED;
    }

    /**
     * Checks if the payment is against a specific invoice.
     *
     * @return true if invoice payment
     */
    public boolean isInvoicePayment() {
        return invoiceId != null && !invoiceId.isEmpty();
    }

    /**
     * Marks the payment as refunded.
     *
     * @param reason the reason for refund
     */
    public void markAsRefunded(String reason) {
        this.status = PaymentStatus.REFUNDED;
        this.notes = (this.notes != null ? this.notes + " | " : "") + "Refund: " + reason;
    }

    /**
     * Marks the payment as failed.
     *
     * @param reason the reason for failure
     */
    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.notes = (this.notes != null ? this.notes + " | " : "") + "Failed: " + reason;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    @Override
    public String toString() {
        return String.format("Payment{ref='%s', account='%s', amount=£%.2f, method=%s, status=%s}",
                referenceNumber, accountNumber, amount, paymentMethod, status);
    }
}

