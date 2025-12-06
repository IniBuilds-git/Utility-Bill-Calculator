package com.utilitybill.model;

import java.math.BigDecimal;

/**
 * Represents a gas tariff with calorific value conversion.
 * This class extends the abstract Tariff class, demonstrating inheritance.
 *
 * <p>Gas billing involves converting cubic meters to kWh using:</p>
 * <ul>
 *   <li>Volume Correction Factor (typically 1.02264)</li>
 *   <li>Calorific Value (energy content, typically ~39.5 MJ/m³)</li>
 *   <li>kWh Conversion Factor (3.6 MJ = 1 kWh)</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class GasTariff extends Tariff {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Standard volume correction factor */
    public static final BigDecimal VOLUME_CORRECTION = new BigDecimal("1.02264");

    /** Standard kWh conversion divisor */
    public static final BigDecimal KWH_DIVISOR = new BigDecimal("3.6");

    /** Unit rate in pence per kWh */
    private BigDecimal unitRatePence;

    /** Calorific value (energy content in MJ/m³) */
    private BigDecimal calorificValue;

    /** Whether to show consumption in cubic meters or kWh */
    private boolean showInCubicMeters;

    /**
     * Default constructor required for JSON deserialization.
     */
    public GasTariff() {
        super();
        this.meterType = MeterType.GAS;
        this.calorificValue = new BigDecimal("39.5"); // Default calorific value
        this.showInCubicMeters = false;
    }

    /**
     * Constructs a gas tariff with standard conversion factors.
     *
     * @param name           the tariff name
     * @param standingCharge the daily standing charge in pounds
     * @param unitRatePence  the unit rate in pence per kWh
     */
    public GasTariff(String name, BigDecimal standingCharge, BigDecimal unitRatePence) {
        super(name, standingCharge, MeterType.GAS);
        this.unitRatePence = unitRatePence;
        this.calorificValue = new BigDecimal("39.5");
    }

    /**
     * Constructs a gas tariff with custom calorific value.
     *
     * @param name           the tariff name
     * @param standingCharge the daily standing charge in pounds
     * @param unitRatePence  the unit rate in pence per kWh
     * @param calorificValue the calorific value in MJ/m³
     */
    public GasTariff(String name, BigDecimal standingCharge, BigDecimal unitRatePence,
                     BigDecimal calorificValue) {
        super(name, standingCharge, MeterType.GAS);
        this.unitRatePence = unitRatePence;
        this.calorificValue = calorificValue;
    }

    // ==================== Abstract Method Implementations ====================

    /**
     * Calculates the cost for gas consumption.
     * Assumes units are already in kWh.
     *
     * @param units the number of kWh consumed
     * @return the cost in pounds (converted from pence)
     */
    @Override
    public BigDecimal calculateUnitCost(double units) {
        BigDecimal cost = unitRatePence.multiply(BigDecimal.valueOf(units));
        // Convert from pence to pounds
        return cost.divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
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
        return String.format("%.2fp per kWh (Calorific Value: %.1f MJ/m³)",
                unitRatePence, calorificValue);
    }

    // ==================== Gas-Specific Methods ====================

    /**
     * Converts cubic meters to kWh using the standard formula.
     * Formula: kWh = Volume × Correction Factor × Calorific Value ÷ 3.6
     *
     * @param cubicMeters the volume in cubic meters
     * @return the equivalent energy in kWh
     */
    public BigDecimal convertCubicMetersToKwh(BigDecimal cubicMeters) {
        return cubicMeters
                .multiply(VOLUME_CORRECTION)
                .multiply(calorificValue)
                .divide(KWH_DIVISOR, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Converts cubic meters to kWh using the standard formula.
     *
     * @param cubicMeters the volume in cubic meters
     * @return the equivalent energy in kWh
     */
    public double convertCubicMetersToKwh(double cubicMeters) {
        return convertCubicMetersToKwh(BigDecimal.valueOf(cubicMeters)).doubleValue();
    }

    /**
     * Calculates the cost from cubic meters reading.
     *
     * @param cubicMeters the volume consumed in cubic meters
     * @return the cost in pounds
     */
    public BigDecimal calculateCostFromCubicMeters(double cubicMeters) {
        double kWh = convertCubicMetersToKwh(cubicMeters);
        return calculateUnitCost(kWh);
    }

    /**
     * Gets the conversion factor from m³ to kWh.
     *
     * @return the conversion factor
     */
    public BigDecimal getConversionFactor() {
        return VOLUME_CORRECTION.multiply(calorificValue).divide(KWH_DIVISOR, 4, java.math.RoundingMode.HALF_UP);
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
     * Gets the calorific value.
     *
     * @return the calorific value in MJ/m³
     */
    public BigDecimal getCalorificValue() {
        return calorificValue;
    }

    /**
     * Sets the calorific value.
     *
     * @param calorificValue the calorific value to set
     */
    public void setCalorificValue(BigDecimal calorificValue) {
        this.calorificValue = calorificValue;
    }

    /**
     * Checks if consumption should be shown in cubic meters.
     *
     * @return true if showing in cubic meters
     */
    public boolean isShowInCubicMeters() {
        return showInCubicMeters;
    }

    /**
     * Sets whether to show consumption in cubic meters.
     *
     * @param showInCubicMeters true to show in cubic meters
     */
    public void setShowInCubicMeters(boolean showInCubicMeters) {
        this.showInCubicMeters = showInCubicMeters;
    }

    @Override
    public String toString() {
        return String.format("GasTariff{name='%s', rate=%.2fp/kWh, CV=%.1f}",
                name, unitRatePence, calorificValue);
    }
}

