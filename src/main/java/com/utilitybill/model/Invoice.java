package com.utilitybill.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents an invoice in the Utility Bill Management System.
 * An invoice contains all billing details for a specific period.
 *
 * <p>This class includes:</p>
 * <ul>
 *   <li>Billing period information</li>
 *   <li>Meter readings and consumption data</li>
 *   <li>Itemized charges breakdown</li>
 *   <li>Payment status tracking</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class Invoice implements Serializable {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Counter for generating unique invoice numbers */
    private static final AtomicLong INVOICE_COUNTER = new AtomicLong(1000);

    /** Unique invoice identifier */
    private String invoiceId;

    /** Invoice number for display (INV-XXXXXX) */
    private String invoiceNumber;

    /** Customer ID this invoice belongs to */
    private String customerId;

    /** Customer account number */
    private String accountNumber;

    /** Billing period start date */
    private LocalDate periodStart;

    /** Billing period end date */
    private LocalDate periodEnd;

    /** Issue date of the invoice */
    private LocalDate issueDate;

    /** Due date for payment */
    private LocalDate dueDate;

    /** Meter type for this invoice */
    private MeterType meterType;

    /** Opening meter reading */
    private double openingReading;

    /** Closing meter reading */
    private double closingReading;

    /** Units consumed */
    private double unitsConsumed;

    /** Unit rate applied (pence per kWh) */
    private BigDecimal unitRate;

    /** Cost of units consumed */
    private BigDecimal unitCost;

    /** Standing charge total for the period */
    private BigDecimal standingChargeTotal;

    /** Subtotal before VAT */
    private BigDecimal subtotal;

    /** VAT amount */
    private BigDecimal vatAmount;

    /** VAT rate applied */
    private BigDecimal vatRate;

    /** Total amount due */
    private BigDecimal totalAmount;

    /** Amount already paid */
    private BigDecimal amountPaid;

    /** Balance remaining */
    private BigDecimal balanceDue;

    /** Invoice status */
    private InvoiceStatus status;

    /** List of line items on the invoice */
    private List<InvoiceLineItem> lineItems;

    /** Tariff ID used for this invoice */
    private String tariffId;

    /** Tariff name */
    private String tariffName;

    /** Creation timestamp */
    private LocalDateTime createdAt;

    /** Last update timestamp */
    private LocalDateTime updatedAt;

    /** Notes on the invoice */
    private String notes;

    /**
     * Enum representing invoice status.
     */
    public enum InvoiceStatus {
        /** Invoice generated, awaiting payment */
        PENDING("Pending"),
        /** Partially paid */
        PARTIAL("Partially Paid"),
        /** Fully paid */
        PAID("Paid"),
        /** Payment overdue */
        OVERDUE("Overdue"),
        /** Invoice cancelled */
        CANCELLED("Cancelled"),
        /** Invoice disputed */
        DISPUTED("Disputed");

        private final String displayName;

        InvoiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Represents a line item on an invoice.
     */
    public static class InvoiceLineItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String description;
        private double quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal amount;

        public InvoiceLineItem() {}

        public InvoiceLineItem(String description, double quantity, String unit,
                               BigDecimal unitPrice, BigDecimal amount) {
            this.description = description;
            this.quantity = quantity;
            this.unit = unit;
            this.unitPrice = unitPrice;
            this.amount = amount;
        }

        // Getters and setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        @Override
        public String toString() {
            return String.format("%s: %.2f %s @ £%.4f = £%.2f",
                    description, quantity, unit, unitPrice, amount);
        }
    }

    /**
     * Default constructor required for JSON deserialization.
     */
    public Invoice() {
        this.invoiceId = UUID.randomUUID().toString();
        this.invoiceNumber = generateInvoiceNumber();
        this.lineItems = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = InvoiceStatus.PENDING;
        this.amountPaid = BigDecimal.ZERO;
        this.vatRate = Tariff.STANDARD_VAT_RATE;
    }

    /**
     * Constructs a new Invoice for a customer and billing period.
     *
     * @param customerId    the customer ID
     * @param accountNumber the account number
     * @param periodStart   the billing period start
     * @param periodEnd     the billing period end
     */
    public Invoice(String customerId, String accountNumber, LocalDate periodStart, LocalDate periodEnd) {
        this();
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.issueDate = LocalDate.now();
        this.dueDate = issueDate.plusDays(14); // 14 days to pay
    }

    /**
     * Generates a unique invoice number.
     *
     * @return the invoice number
     */
    private static String generateInvoiceNumber() {
        return String.format("INV-%06d", INVOICE_COUNTER.getAndIncrement());
    }

    // ==================== Getters and Setters ====================

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public MeterType getMeterType() { return meterType; }
    public void setMeterType(MeterType meterType) { this.meterType = meterType; }

    public double getOpeningReading() { return openingReading; }
    public void setOpeningReading(double openingReading) { this.openingReading = openingReading; }

    public double getClosingReading() { return closingReading; }
    public void setClosingReading(double closingReading) { this.closingReading = closingReading; }

    public double getUnitsConsumed() { return unitsConsumed; }
    public void setUnitsConsumed(double unitsConsumed) { this.unitsConsumed = unitsConsumed; }

    public BigDecimal getUnitRate() { return unitRate; }
    public void setUnitRate(BigDecimal unitRate) { this.unitRate = unitRate; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getStandingChargeTotal() { return standingChargeTotal; }
    public void setStandingChargeTotal(BigDecimal standingChargeTotal) {
        this.standingChargeTotal = standingChargeTotal;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getVatAmount() { return vatAmount; }
    public void setVatAmount(BigDecimal vatAmount) { this.vatAmount = vatAmount; }

    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getBalanceDue() { return balanceDue; }
    public void setBalanceDue(BigDecimal balanceDue) { this.balanceDue = balanceDue; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public List<InvoiceLineItem> getLineItems() { return Collections.unmodifiableList(lineItems); }
    public void setLineItems(List<InvoiceLineItem> lineItems) {
        this.lineItems = lineItems != null ? new ArrayList<>(lineItems) : new ArrayList<>();
    }

    public String getTariffId() { return tariffId; }
    public void setTariffId(String tariffId) { this.tariffId = tariffId; }

    public String getTariffName() { return tariffName; }
    public void setTariffName(String tariffName) { this.tariffName = tariffName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // ==================== Business Methods ====================

    /**
     * Adds a line item to the invoice.
     *
     * @param item the line item to add
     */
    public void addLineItem(InvoiceLineItem item) {
        lineItems.add(item);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the number of days in the billing period.
     *
     * @return the number of days
     */
    public int getBillingDays() {
        if (periodStart == null || periodEnd == null) return 0;
        return (int) ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
    }

    /**
     * Applies a payment to this invoice.
     *
     * @param amount the payment amount
     */
    public void applyPayment(BigDecimal amount) {
        this.amountPaid = this.amountPaid.add(amount);
        this.balanceDue = this.totalAmount.subtract(this.amountPaid);

        if (this.balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = InvoiceStatus.PAID;
            this.balanceDue = BigDecimal.ZERO;
        } else if (this.amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            this.status = InvoiceStatus.PARTIAL;
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the invoice is overdue.
     *
     * @return true if overdue
     */
    public boolean isOverdue() {
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED) {
            return false;
        }
        return dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    /**
     * Gets the number of days until due (negative if overdue).
     *
     * @return days until due
     */
    public long getDaysUntilDue() {
        if (dueDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    /**
     * Calculates the totals for this invoice.
     * Call this after setting consumption and rates.
     */
    public void calculateTotals() {
        // Calculate unit cost
        if (unitRate != null && unitsConsumed > 0) {
            // Unit rate is in pence, convert to pounds
            this.unitCost = unitRate.multiply(BigDecimal.valueOf(unitsConsumed))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.unitCost = BigDecimal.ZERO;
        }

        // Calculate subtotal
        this.subtotal = unitCost;
        if (standingChargeTotal != null) {
            this.subtotal = this.subtotal.add(standingChargeTotal);
        }

        // Calculate VAT
        this.vatAmount = subtotal.multiply(vatRate)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // Calculate total
        this.totalAmount = subtotal.add(vatAmount)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // Calculate balance due
        this.balanceDue = totalAmount.subtract(amountPaid);

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the invoice status based on due date and payment.
     */
    public void updateStatus() {
        if (status == InvoiceStatus.CANCELLED) return;

        if (balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
            status = InvoiceStatus.PAID;
        } else if (isOverdue()) {
            status = InvoiceStatus.OVERDUE;
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            status = InvoiceStatus.PARTIAL;
        } else {
            status = InvoiceStatus.PENDING;
        }
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(invoiceId, invoice.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        return String.format("Invoice{number='%s', customer='%s', total=£%.2f, status=%s}",
                invoiceNumber, accountNumber, totalAmount, status);
    }
}

