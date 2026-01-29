package com.utilitybill.model;

import java.io.Serial;
import java.math.BigDecimal;

public class GasTariff extends Tariff {

    @Serial
    private static final long serialVersionUID = 1L;
    public static final double DEFAULT_CORRECTION_FACTOR = 1.02264;
    public static final BigDecimal KWH_DIVISOR = new BigDecimal("3.6");
    public static final double DEFAULT_CALORIFIC_VALUE = 39.4;
    public static final double IMPERIAL_TO_METRIC = 2.83;

    private BigDecimal unitRatePence;
    private boolean showInCubicMeters;
    private double calorificValue;
    private double correctionFactor;

    public GasTariff() {
        super();
        this.meterType = MeterType.GAS;
        this.showInCubicMeters = false;
        this.calorificValue = DEFAULT_CALORIFIC_VALUE;
        this.correctionFactor = DEFAULT_CORRECTION_FACTOR;
    }

    // Constructor for gas tariff
    public GasTariff(String name, BigDecimal standingCharge, BigDecimal unitRatePence) {
        super(name, standingCharge, MeterType.GAS);
        this.unitRatePence = unitRatePence;
        this.showInCubicMeters = false;
        this.calorificValue = DEFAULT_CALORIFIC_VALUE;
        this.correctionFactor = DEFAULT_CORRECTION_FACTOR;
    }

    @Override
    public BigDecimal calculateUnitCost(double units) {
        BigDecimal cost = unitRatePence.multiply(BigDecimal.valueOf(units));
        return cost.divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getUnitRate() {
        return unitRatePence;
    }

    @Override
    public String getPricingDescription() {
        return String.format("%.2fp per kWh (CV: %.1f)", unitRatePence, calorificValue);
    }

    public BigDecimal getUnitRatePence() {
        return unitRatePence;
    }

    public void setUnitRatePence(BigDecimal unitRatePence) {
        this.unitRatePence = unitRatePence;
    }

    public boolean isShowInCubicMeters() {
        return showInCubicMeters;
    }

    public void setShowInCubicMeters(boolean showInCubicMeters) {
        this.showInCubicMeters = showInCubicMeters;
    }

    public double getCalorificValue() {
        return calorificValue;
    }

    public void setCalorificValue(double calorificValue) {
        this.calorificValue = calorificValue;
    }

    public double getCorrectionFactor() {
        // Handle backward compatibility for deserialized objects where correctionFactor
        // is 0.0
        return correctionFactor == 0.0 ? DEFAULT_CORRECTION_FACTOR : correctionFactor;
    }

    public void setCorrectionFactor(double correctionFactor) {
        this.correctionFactor = correctionFactor;
    }

    /**
     * Convert gas meter units to kWh
     * 
     * @param meterUnits Raw meter reading difference
     * @param isImperial Whether meter is imperial (hundreds of cubic feet)
     * @return kWh consumed
     */
    public double convertToKwh(double meterUnits, boolean isImperial) {
        // Step 1: Convert to cubic meters if imperial
        double cubicMeters = isImperial ? meterUnits * IMPERIAL_TO_METRIC : meterUnits;

        // Step 2: Apply volume correction factor
        double correctedVolume = cubicMeters * getCorrectionFactor();

        // Step 3: Convert to kWh using calorific value
        double kwh = (correctedVolume * calorificValue) / KWH_DIVISOR.doubleValue();

        return kwh;
    }

    @Override
    public String toString() {
        return String.format("GasTariff{name='%s', rate=%.2fp/kWh, CV=%.1f, CF=%.5f}",
                name, unitRatePence, calorificValue, getCorrectionFactor());
    }
}
