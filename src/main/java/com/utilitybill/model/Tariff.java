package com.utilitybill.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class representing a utility tariff.
 * This class demonstrates abstraction and serves as the base for specific tariff types.
 *
 * <p>Design Pattern: Template Method - Defines the skeleton of the billing algorithm
 * in calculateBill(), letting subclasses override specific steps.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public abstract class Tariff implements Serializable {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Standard VAT rate for utilities */
    public static final BigDecimal STANDARD_VAT_RATE = new BigDecimal("0.05"); // 5% reduced rate

    /** Unique tariff identifier */
    protected String tariffId;

    /** Tariff name */
    protected String name;

    /** Tariff description */
    protected String description;

    /** Standing charge per day (in pounds) */
    protected BigDecimal standingCharge;

    /** VAT rate (as decimal, e.g., 0.05 for 5%) */
    protected BigDecimal vatRate;

    /** Whether the tariff is currently active */
    protected boolean active;

    /** Start date of tariff validity */
    protected LocalDate startDate;

    /** End date of tariff validity (null for indefinite) */
    protected LocalDate endDate;

    /** Meter type this tariff applies to */
    protected MeterType meterType;

    /**
     * Default constructor required for JSON deserialization.
     */
    protected Tariff() {
        this.tariffId = UUID.randomUUID().toString();
        this.vatRate = STANDARD_VAT_RATE;
        this.active = true;
        this.startDate = LocalDate.now();
    }

    /**
     * Constructs a new Tariff with basic details.
     *
     * @param name           the tariff name
     * @param standingCharge the daily standing charge
     * @param meterType      the meter type this tariff applies to
     */
    protected Tariff(String name, BigDecimal standingCharge, MeterType meterType) {
        this();
        this.name = name;
        this.standingCharge = standingCharge;
        this.meterType = meterType;
    }

    // ==================== Abstract Methods ====================

    /**
     * Calculates the cost for the given number of units.
     * This method must be implemented by subclasses to define their pricing logic.
     *
     * @param units the number of units consumed
     * @return the cost before VAT
     */
    public abstract BigDecimal calculateUnitCost(double units);

    /**
     * Gets the unit rate for display purposes.
     * For tiered tariffs, this should return the primary rate.
     *
     * @return the unit rate in pence per kWh
     */
    public abstract BigDecimal getUnitRate();

    /**
     * Gets a description of the tariff pricing structure.
     *
     * @return human-readable pricing description
     */
    public abstract String getPricingDescription();

    // ==================== Template Method ====================

    /**
     * Calculates the total bill including standing charge and VAT.
     * This is a template method that defines the billing algorithm.
     *
     * @param units         the number of units consumed
     * @param billingDays   the number of days in the billing period
     * @return the total bill amount
     */
    public final BigDecimal calculateBill(double units, int billingDays) {
        // Calculate unit cost (implementation varies by subclass)
        BigDecimal unitCost = calculateUnitCost(units);

        // Calculate standing charge for the period
        BigDecimal totalStandingCharge = standingCharge.multiply(BigDecimal.valueOf(billingDays));

        // Calculate subtotal
        BigDecimal subtotal = unitCost.add(totalStandingCharge);

        // Calculate VAT
        BigDecimal vat = subtotal.multiply(vatRate);

        // Return total
        return subtotal.add(vat).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculates the VAT amount for a given subtotal.
     *
     * @param subtotal the subtotal amount
     * @return the VAT amount
     */
    public BigDecimal calculateVAT(BigDecimal subtotal) {
        return subtotal.multiply(vatRate).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the tariff ID.
     *
     * @return the tariff ID
     */
    public String getTariffId() {
        return tariffId;
    }

    /**
     * Sets the tariff ID.
     *
     * @param tariffId the tariff ID to set
     */
    public void setTariffId(String tariffId) {
        this.tariffId = tariffId;
    }

    /**
     * Gets the tariff name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the tariff name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the standing charge.
     *
     * @return the standing charge per day
     */
    public BigDecimal getStandingCharge() {
        return standingCharge;
    }

    /**
     * Sets the standing charge.
     *
     * @param standingCharge the standing charge to set
     */
    public void setStandingCharge(BigDecimal standingCharge) {
        this.standingCharge = standingCharge;
    }

    /**
     * Gets the VAT rate.
     *
     * @return the VAT rate as a decimal
     */
    public BigDecimal getVatRate() {
        return vatRate;
    }

    /**
     * Sets the VAT rate.
     *
     * @param vatRate the VAT rate to set
     */
    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    /**
     * Checks if the tariff is active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active status.
     *
     * @param active the active status to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the start date to set
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date.
     *
     * @param endDate the end date to set
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Gets the meter type.
     *
     * @return the meter type
     */
    public MeterType getMeterType() {
        return meterType;
    }

    /**
     * Sets the meter type.
     *
     * @param meterType the meter type to set
     */
    public void setMeterType(MeterType meterType) {
        this.meterType = meterType;
    }

    // ==================== Business Methods ====================

    /**
     * Checks if the tariff is valid on a given date.
     *
     * @param date the date to check
     * @return true if tariff is valid on that date
     */
    public boolean isValidOn(LocalDate date) {
        if (!active) return false;
        if (date.isBefore(startDate)) return false;
        if (endDate != null && date.isAfter(endDate)) return false;
        return true;
    }

    /**
     * Checks if the tariff is currently valid.
     *
     * @return true if tariff is currently valid
     */
    public boolean isCurrentlyValid() {
        return isValidOn(LocalDate.now());
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tariff tariff = (Tariff) o;
        return Objects.equals(tariffId, tariff.tariffId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tariffId);
    }

    @Override
    public String toString() {
        return String.format("Tariff{id='%s', name='%s', type=%s, standing=£%.2f/day}",
                tariffId, name, meterType, standingCharge);
    }
}

