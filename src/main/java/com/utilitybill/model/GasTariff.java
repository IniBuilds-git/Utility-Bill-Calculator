package com.utilitybill.model;

import java.math.BigDecimal;

public class GasTariff extends Tariff {

    private static final long serialVersionUID = 1L;
    public static final BigDecimal VOLUME_CORRECTION = new BigDecimal("1.02264");
    public static final BigDecimal KWH_DIVISOR = new BigDecimal("3.6");

    private BigDecimal unitRatePence;
    private BigDecimal calorificValue;
    private boolean showInCubicMeters;

    public GasTariff() {
        super();
        this.meterType = MeterType.GAS;
        this.calorificValue = new BigDecimal("39.5");
        this.showInCubicMeters = false;
    }

    public GasTariff(String name, BigDecimal standingCharge, BigDecimal unitRatePence) {
        super(name, standingCharge, MeterType.GAS);
        this.unitRatePence = unitRatePence;
        this.calorificValue = new BigDecimal("39.5");
    }

    public GasTariff(String name, BigDecimal standingCharge, BigDecimal unitRatePence,
                     BigDecimal calorificValue) {
        super(name, standingCharge, MeterType.GAS);
        this.unitRatePence = unitRatePence;
        this.calorificValue = calorificValue;
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
        return String.format("%.2fp per kWh (Calorific Value: %.1f MJ/m³)",
                unitRatePence, calorificValue);
    }

    public BigDecimal convertCubicMetersToKwh(BigDecimal cubicMeters) {
        return cubicMeters
                .multiply(VOLUME_CORRECTION)
                .multiply(calorificValue)
                .divide(KWH_DIVISOR, 2, java.math.RoundingMode.HALF_UP);
    }

    public double convertCubicMetersToKwh(double cubicMeters) {
        return convertCubicMetersToKwh(BigDecimal.valueOf(cubicMeters)).doubleValue();
    }

    public BigDecimal calculateCostFromCubicMeters(double cubicMeters) {
        double kWh = convertCubicMetersToKwh(cubicMeters);
        return calculateUnitCost(kWh);
    }

    public BigDecimal getConversionFactor() {
        return VOLUME_CORRECTION.multiply(calorificValue).divide(KWH_DIVISOR, 4, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getUnitRatePence() {
        return unitRatePence;
    }

    public void setUnitRatePence(BigDecimal unitRatePence) {
        this.unitRatePence = unitRatePence;
    }

    public BigDecimal getCalorificValue() {
        return calorificValue;
    }

    public void setCalorificValue(BigDecimal calorificValue) {
        this.calorificValue = calorificValue;
    }

    public boolean isShowInCubicMeters() {
        return showInCubicMeters;
    }

    public void setShowInCubicMeters(boolean showInCubicMeters) {
        this.showInCubicMeters = showInCubicMeters;
    }

    @Override
    public String toString() {
        return String.format("GasTariff{name='%s', rate=%.2fp/kWh, CV=%.1f}",
                name, unitRatePence, calorificValue);
    }
}
