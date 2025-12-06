package com.utilitybill.model;

import java.math.BigDecimal;

/**
 * Represents an electricity tariff with flat-rate or tiered pricing.
 * This class extends the abstract Tariff class, demonstrating inheritance.
 *
 * <p>Supports two pricing models:</p>
 * <ul>
 *   <li>Flat Rate - Single unit rate for all consumption</li>
 *   <li>Tiered - Different rates based on consumption levels</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class ElectricityTariff extends Tariff {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Unit rate in pence per kWh */
    private BigDecimal unitRatePence;

    /** Tier 1 threshold (units) - used for tiered pricing */
    private double tier1Threshold;

    /** Tier 1 rate (pence per kWh) - for first units up to threshold */
    private BigDecimal tier1Rate;

    /** Tier 2 rate (pence per kWh) - for units above threshold */
    private BigDecimal tier2Rate;

    /** Whether this tariff uses tiered pricing */
    private boolean tieredPricing;

    /**
     * Default constructor required for JSON deserialization.
     */
    public ElectricityTariff() {
        super();
        this.meterType = MeterType.ELECTRICITY;
        this.tieredPricing = false;
    }

    /**
     * Constructs a flat-rate electricity tariff.
     *
     * @param name           the tariff name
     * @param standingCharge the daily standing charge in pounds
     * @param unitRatePence  the unit rate in pence per kWh
     */
    public ElectricityTariff(String name, BigDecimal standingCharge, BigDecimal unitRatePence) {
        super(name, standingCharge, MeterType.ELECTRICITY);
        this.unitRatePence = unitRatePence;
        this.tieredPricing = false;
    }

    /**
     * Constructs a tiered electricity tariff.
     *
     * @param name           the tariff name
     * @param standingCharge the daily standing charge in pounds
     * @param tier1Threshold the threshold for tier 1 (in kWh)
     * @param tier1Rate      the rate for tier 1 (pence per kWh)
     * @param tier2Rate      the rate for tier 2 (pence per kWh)
     */
    public ElectricityTariff(String name, BigDecimal standingCharge,
                             double tier1Threshold, BigDecimal tier1Rate, BigDecimal tier2Rate) {
        super(name, standingCharge, MeterType.ELECTRICITY);
        this.tier1Threshold = tier1Threshold;
        this.tier1Rate = tier1Rate;
        this.tier2Rate = tier2Rate;
        this.tieredPricing = true;
        this.unitRatePence = tier1Rate; // Use tier 1 as primary rate for display
    }

    // ==================== Abstract Method Implementations ====================

    /**
     * Calculates the cost for electricity consumption.
     * Uses flat-rate or tiered pricing based on configuration.
     *
     * @param units the number of kWh consumed
     * @return the cost in pounds (converted from pence)
     */
    @Override
    public BigDecimal calculateUnitCost(double units) {
        BigDecimal cost;

        if (tieredPricing) {
            cost = calculateTieredCost(units);
        } else {
            // Flat rate calculation
            cost = unitRatePence.multiply(BigDecimal.valueOf(units));
        }

        // Convert from pence to pounds
        return cost.divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculates the cost using tiered pricing.
     *
     * @param units the number of kWh consumed
     * @return the cost in pence
     */
    private BigDecimal calculateTieredCost(double units) {
        BigDecimal cost = BigDecimal.ZERO;

        if (units <= tier1Threshold) {
            // All units at tier 1 rate
            cost = tier1Rate.multiply(BigDecimal.valueOf(units));
        } else {
            // First units at tier 1 rate, remainder at tier 2 rate
            BigDecimal tier1Cost = tier1Rate.multiply(BigDecimal.valueOf(tier1Threshold));
            BigDecimal tier2Units = BigDecimal.valueOf(units - tier1Threshold);
            BigDecimal tier2Cost = tier2Rate.multiply(tier2Units);
            cost = tier1Cost.add(tier2Cost);
        }

        return cost;
    }

    /**
     * Gets the primary unit rate.
     *
     * @return the unit rate in pence per kWh
     */
    @Override
    public BigDecimal getUnitRate() {
        return unitRatePence;
    }

    /**
     * Gets a description of the pricing structure.
     *
     * @return human-readable pricing description
     */
    @Override
    public String getPricingDescription() {
        if (tieredPricing) {
            return String.format("First %.0f kWh at %.2fp/kWh, then %.2fp/kWh",
                    tier1Threshold, tier1Rate, tier2Rate);
        } else {
            return String.format("Flat rate: %.2fp per kWh", unitRatePence);
        }
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the unit rate in pence.
     *
     * @return the unit rate
     */
    public BigDecimal getUnitRatePence() {
        return unitRatePence;
    }

    /**
     * Sets the unit rate in pence.
     *
     * @param unitRatePence the unit rate to set
     */
    public void setUnitRatePence(BigDecimal unitRatePence) {
        this.unitRatePence = unitRatePence;
    }

    /**
     * Gets the tier 1 threshold.
     *
     * @return the tier 1 threshold in kWh
     */
    public double getTier1Threshold() {
        return tier1Threshold;
    }

    /**
     * Sets the tier 1 threshold.
     *
     * @param tier1Threshold the threshold to set
     */
    public void setTier1Threshold(double tier1Threshold) {
        this.tier1Threshold = tier1Threshold;
    }

    /**
     * Gets the tier 1 rate.
     *
     * @return the tier 1 rate in pence
     */
    public BigDecimal getTier1Rate() {
        return tier1Rate;
    }

    /**
     * Sets the tier 1 rate.
     *
     * @param tier1Rate the rate to set
     */
    public void setTier1Rate(BigDecimal tier1Rate) {
        this.tier1Rate = tier1Rate;
    }

    /**
     * Gets the tier 2 rate.
     *
     * @return the tier 2 rate in pence
     */
    public BigDecimal getTier2Rate() {
        return tier2Rate;
    }

    /**
     * Sets the tier 2 rate.
     *
     * @param tier2Rate the rate to set
     */
    public void setTier2Rate(BigDecimal tier2Rate) {
        this.tier2Rate = tier2Rate;
    }

    /**
     * Checks if tiered pricing is enabled.
     *
     * @return true if using tiered pricing
     */
    public boolean isTieredPricing() {
        return tieredPricing;
    }

    /**
     * Sets whether to use tiered pricing.
     *
     * @param tieredPricing true to enable tiered pricing
     */
    public void setTieredPricing(boolean tieredPricing) {
        this.tieredPricing = tieredPricing;
    }

    @Override
    public String toString() {
        return String.format("ElectricityTariff{name='%s', tiered=%s, %s}",
                name, tieredPricing, getPricingDescription());
    }
}

