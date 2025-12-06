package com.utilitybill.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when a payment amount is insufficient to cover the required amount.
 * This exception is raised during payment processing when the payment does not
 * meet the minimum required amount or is less than the invoice total.
 *
 * <p>Error Codes:</p>
 * <ul>
 *   <li>PAY001 - Payment less than invoice total</li>
 *   <li>PAY002 - Payment less than minimum required</li>
 *   <li>PAY003 - Zero or negative payment amount</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class InsufficientPaymentException extends UtilityBillException {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** The payment amount attempted */
    private final BigDecimal paymentAmount;

    /** The required amount */
    private final BigDecimal requiredAmount;

    /** The invoice ID (if applicable) */
    private final String invoiceId;

    /**
     * Constructs a new InsufficientPaymentException.
     *
     * @param paymentAmount  the payment amount attempted
     * @param requiredAmount the required payment amount
     */
    public InsufficientPaymentException(BigDecimal paymentAmount, BigDecimal requiredAmount) {
        super(String.format("Insufficient payment: £%.2f provided, £%.2f required",
                paymentAmount, requiredAmount), "PAY001");
        this.paymentAmount = paymentAmount;
        this.requiredAmount = requiredAmount;
        this.invoiceId = null;
    }

    /**
     * Constructs a new InsufficientPaymentException with invoice reference.
     *
     * @param paymentAmount  the payment amount attempted
     * @param requiredAmount the required payment amount
     * @param invoiceId      the invoice ID being paid
     */
    public InsufficientPaymentException(BigDecimal paymentAmount, BigDecimal requiredAmount, String invoiceId) {
        super(String.format("Insufficient payment for invoice %s: £%.2f provided, £%.2f required",
                invoiceId, paymentAmount, requiredAmount), "PAY001");
        this.paymentAmount = paymentAmount;
        this.requiredAmount = requiredAmount;
        this.invoiceId = invoiceId;
    }

    /**
     * Factory method for zero or negative payment.
     *
     * @param paymentAmount the invalid payment amount
     * @return a new InsufficientPaymentException
     */
    public static InsufficientPaymentException invalidAmount(BigDecimal paymentAmount) {
        return new InsufficientPaymentException(paymentAmount, BigDecimal.ONE) {
            @Override
            public String getMessage() {
                return String.format("Payment amount must be positive: £%.2f provided", paymentAmount);
            }

            @Override
            public String getErrorCode() {
                return "PAY003";
            }
        };
    }

    /**
     * Gets the payment amount that was attempted.
     *
     * @return the payment amount
     */
    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    /**
     * Gets the required payment amount.
     *
     * @return the required amount
     */
    public BigDecimal getRequiredAmount() {
        return requiredAmount;
    }

    /**
     * Gets the invoice ID if available.
     *
     * @return the invoice ID, or null if not applicable
     */
    public String getInvoiceId() {
        return invoiceId;
    }

    /**
     * Calculates the shortfall amount.
     *
     * @return the difference between required and payment amounts
     */
    public BigDecimal getShortfall() {
        return requiredAmount.subtract(paymentAmount);
    }

    /**
     * Calculates the percentage of the required amount that was paid.
     *
     * @return the payment percentage (0-100)
     */
    public double getPaymentPercentage() {
        if (requiredAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return paymentAmount.divide(requiredAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}

