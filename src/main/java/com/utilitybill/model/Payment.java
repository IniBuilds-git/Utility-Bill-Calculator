package com.utilitybill.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final AtomicLong PAYMENT_COUNTER = new AtomicLong(100000);

    private String paymentId;
    private String referenceNumber;
    private String customerId;
    private String accountNumber;
    private String invoiceId;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private LocalDateTime recordedAt;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionReference;
    private String notes;
    private String recordedBy;

    public enum PaymentMethod {
        CASH("Cash"),
        CHEQUE("Cheque"),
        BANK_TRANSFER("Bank Transfer"),
        DEBIT_CARD("Debit Card"),
        CREDIT_CARD("Credit Card"),
        DIRECT_DEBIT("Direct Debit"),
        STANDING_ORDER("Standing Order"),
        ONLINE("Online"),
        PAYMENT_POINT("Payment Point"),
        OTHER("Other");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentStatus {
        COMPLETED("Completed"),
        PENDING("Pending"),
        FAILED("Failed"),
        REFUNDED("Refunded"),
        CANCELLED("Cancelled");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Payment() {
        this.paymentId = UUID.randomUUID().toString();
        this.referenceNumber = generateReferenceNumber();
        this.paymentDate = LocalDate.now();
        this.recordedAt = LocalDateTime.now();
        this.status = PaymentStatus.COMPLETED;
        this.paymentMethod = PaymentMethod.CASH;
    }

    public Payment(String customerId, String accountNumber, BigDecimal amount, PaymentMethod paymentMethod) {
        this();
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public Payment(String customerId, String accountNumber, String invoiceId,
                   String invoiceNumber, BigDecimal amount, PaymentMethod paymentMethod) {
        this(customerId, accountNumber, amount, paymentMethod);
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
    }

    private static String generateReferenceNumber() {
        return String.format("PAY-%06d", PAYMENT_COUNTER.getAndIncrement());
    }

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

    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isInvoicePayment() {
        return invoiceId != null && !invoiceId.isEmpty();
    }

    public void markAsRefunded(String reason) {
        this.status = PaymentStatus.REFUNDED;
        this.notes = (this.notes != null ? this.notes + " | " : "") + "Refund: " + reason;
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.notes = (this.notes != null ? this.notes + " | " : "") + "Failed: " + reason;
    }

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
