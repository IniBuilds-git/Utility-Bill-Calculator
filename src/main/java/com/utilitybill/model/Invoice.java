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

public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final AtomicLong INVOICE_COUNTER = new AtomicLong(1000);

    private String invoiceId;
    private String invoiceNumber;
    private String customerId;
    private String accountNumber;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private MeterType meterType;
    private double openingReading;
    private double closingReading;
    private double unitsConsumed;
    private BigDecimal unitRate;
    private BigDecimal unitCost;
    private BigDecimal standingChargeTotal;
    private BigDecimal subtotal;
    private BigDecimal vatAmount;
    private BigDecimal vatRate;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private InvoiceStatus status;
    private List<InvoiceLineItem> lineItems;
    private String tariffId;
    private String tariffName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Breakdown for electricity day/night
    private Double dayOpeningReading;
    private Double dayClosingReading;
    private Double dayUnitsConsumed;
    private Double nightOpeningReading;
    private Double nightClosingReading;
    private Double nightUnitsConsumed;
    private BigDecimal dayUnitRate;
    private BigDecimal nightUnitRate;

    // Breakdown for gas
    private Double meterUnits; // Raw meter reading difference
    private Double cubicMeters; // After imperial conversion (if applicable)
    private Double correctedVolume; // After applying correction factor
    private Double calorificValue; // CV used for conversion
    private Double kwhFromGas; // Final kWh after full conversion
    private boolean imperialMeter;
    private String notes;

    // Account integration
    private BigDecimal previousBalance;
    private BigDecimal paymentsReceived;
    private BigDecimal accountBalanceAfter;

    public enum InvoiceStatus {
        PENDING("Pending"),
        PARTIAL("Partially Paid"),
        PAID("Paid"),
        OVERDUE("Overdue"),
        CANCELLED("Cancelled"),
        DISPUTED("Disputed");

        private final String displayName;

        InvoiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class InvoiceLineItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String description;
        private double quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal amount;

        public InvoiceLineItem() {
        }

        public InvoiceLineItem(String description, double quantity, String unit,
                BigDecimal unitPrice, BigDecimal amount) {
            this.description = description;
            this.quantity = quantity;
            this.unit = unit;
            this.unitPrice = unitPrice;
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        @Override
        public String toString() {
            return String.format("%s: %.2f %s @ £%.4f = £%.2f",
                    description, quantity, unit, unitPrice, amount);
        }
    }

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

    public Invoice(String customerId, String accountNumber, LocalDate periodStart, LocalDate periodEnd) {
        this();
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.issueDate = LocalDate.now();
        this.dueDate = issueDate.plusDays(14);
    }

    private static String generateInvoiceNumber() {
        return String.format("INV-%06d", INVOICE_COUNTER.getAndIncrement());
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public MeterType getMeterType() {
        return meterType;
    }

    public void setMeterType(MeterType meterType) {
        this.meterType = meterType;
    }

    public double getOpeningReading() {
        return openingReading;
    }

    public void setOpeningReading(double openingReading) {
        this.openingReading = openingReading;
    }

    public double getClosingReading() {
        return closingReading;
    }

    public void setClosingReading(double closingReading) {
        this.closingReading = closingReading;
    }

    public double getUnitsConsumed() {
        return unitsConsumed;
    }

    public void setUnitsConsumed(double unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }

    public BigDecimal getUnitRate() {
        return unitRate;
    }

    public void setUnitRate(BigDecimal unitRate) {
        this.unitRate = unitRate;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getStandingChargeTotal() {
        return standingChargeTotal;
    }

    public void setStandingChargeTotal(BigDecimal standingChargeTotal) {
        this.standingChargeTotal = standingChargeTotal;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getBalanceDue() {
        return balanceDue;
    }

    public void setBalanceDue(BigDecimal balanceDue) {
        this.balanceDue = balanceDue;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public List<InvoiceLineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }

    public void setLineItems(List<InvoiceLineItem> lineItems) {
        this.lineItems = lineItems != null ? new ArrayList<>(lineItems) : new ArrayList<>();
    }

    public String getTariffId() {
        return tariffId;
    }

    public void setTariffId(String tariffId) {
        this.tariffId = tariffId;
    }

    public String getTariffName() {
        return tariffName;
    }

    public void setTariffName(String tariffName) {
        this.tariffName = tariffName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void addLineItem(InvoiceLineItem item) {
        lineItems.add(item);
        this.updatedAt = LocalDateTime.now();
    }

    public int getBillingDays() {
        if (periodStart == null || periodEnd == null)
            return 0;
        return (int) ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
    }

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

    public boolean isOverdue() {
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED) {
            return false;
        }
        return dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    public long getDaysUntilDue() {
        if (dueDate == null)
            return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    public void calculateTotals() {
        // Only calculate unitCost if not already set (allows manual override)
        if ((unitCost == null || unitCost.equals(BigDecimal.ZERO)) && unitRate != null && unitsConsumed > 0) {
            this.unitCost = unitRate.multiply(BigDecimal.valueOf(unitsConsumed))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else if (unitCost == null) {
            this.unitCost = BigDecimal.ZERO;
        }

        this.subtotal = unitCost;
        if (standingChargeTotal != null) {
            this.subtotal = this.subtotal.add(standingChargeTotal);
        }

        this.vatAmount = subtotal.multiply(vatRate)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        this.totalAmount = subtotal.add(vatAmount)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        this.balanceDue = totalAmount.subtract(amountPaid);

        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus() {
        if (status == InvoiceStatus.CANCELLED)
            return;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
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

    // Getters and setters for enhanced breakdown fields

    // Electricity day/night
    public Double getDayOpeningReading() {
        return dayOpeningReading;
    }

    public void setDayOpeningReading(Double dayOpeningReading) {
        this.dayOpeningReading = dayOpeningReading;
    }

    public Double getDayClosingReading() {
        return dayClosingReading;
    }

    public void setDayClosingReading(Double dayClosingReading) {
        this.dayClosingReading = dayClosingReading;
    }

    public Double getDayUnitsConsumed() {
        return dayUnitsConsumed;
    }

    public void setDayUnitsConsumed(Double dayUnitsConsumed) {
        this.dayUnitsConsumed = dayUnitsConsumed;
    }

    public Double getNightOpeningReading() {
        return nightOpeningReading;
    }

    public void setNightOpeningReading(Double nightOpeningReading) {
        this.nightOpeningReading = nightOpeningReading;
    }

    public Double getNightClosingReading() {
        return nightClosingReading;
    }

    public void setNightClosingReading(Double nightClosingReading) {
        this.nightClosingReading = nightClosingReading;
    }

    public Double getNightUnitsConsumed() {
        return nightUnitsConsumed;
    }

    public void setNightUnitsConsumed(Double nightUnitsConsumed) {
        this.nightUnitsConsumed = nightUnitsConsumed;
    }

    public BigDecimal getDayUnitRate() {
        return dayUnitRate;
    }

    public void setDayUnitRate(BigDecimal dayUnitRate) {
        this.dayUnitRate = dayUnitRate;
    }

    public BigDecimal getNightUnitRate() {
        return nightUnitRate;
    }

    public void setNightUnitRate(BigDecimal nightUnitRate) {
        this.nightUnitRate = nightUnitRate;
    }

    public boolean hasDayNightBreakdown() {
        return dayUnitsConsumed != null && nightUnitsConsumed != null;
    }

    // Gas conversion details
    public Double getMeterUnits() {
        return meterUnits;
    }

    public void setMeterUnits(Double meterUnits) {
        this.meterUnits = meterUnits;
    }

    public Double getCubicMeters() {
        return cubicMeters;
    }

    public void setCubicMeters(Double cubicMeters) {
        this.cubicMeters = cubicMeters;
    }

    public Double getCorrectedVolume() {
        return correctedVolume;
    }

    public void setCorrectedVolume(Double correctedVolume) {
        this.correctedVolume = correctedVolume;
    }

    public Double getCalorificValue() {
        return calorificValue;
    }

    public void setCalorificValue(Double calorificValue) {
        this.calorificValue = calorificValue;
    }

    public Double getKwhFromGas() {
        return kwhFromGas;
    }

    public void setKwhFromGas(Double kwhFromGas) {
        this.kwhFromGas = kwhFromGas;
    }

    public boolean isImperialMeter() {
        return imperialMeter;
    }

    public void setImperialMeter(boolean imperialMeter) {
        this.imperialMeter = imperialMeter;
    }

    // Account balance
    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public BigDecimal getPaymentsReceived() {
        return paymentsReceived;
    }

    public void setPaymentsReceived(BigDecimal paymentsReceived) {
        this.paymentsReceived = paymentsReceived;
    }

    public BigDecimal getAccountBalanceAfter() {
        return accountBalanceAfter;
    }

    public void setAccountBalanceAfter(BigDecimal accountBalanceAfter) {
        this.accountBalanceAfter = accountBalanceAfter;
    }
}
