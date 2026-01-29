package com.utilitybill.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ElectricityTariff extends Tariff {

    private static final long serialVersionUID = 1L;

    private BigDecimal dayRate;
    private BigDecimal nightRate;
    private BigDecimal unitRatePence;
    private double tier1Threshold;
    private BigDecimal tier1Rate;
    private BigDecimal tier2Rate;
    private boolean tieredPricing;

    public ElectricityTariff() {
        super();
        this.meterType = MeterType.ELECTRICITY;
        this.tieredPricing = false;
    }

    // Constructor for day/night rate tariff
    public ElectricityTariff(String name, BigDecimal standingCharge,
                             BigDecimal dayRate, BigDecimal nightRate) {
        super(name, standingCharge, MeterType.ELECTRICITY);
        this.dayRate = dayRate;
        this.nightRate = nightRate;
        this.tieredPricing = false;
        this.unitRatePence = dayRate; // Default to day rate
    }

    // Constructor for flat rate tariff
    public ElectricityTariff(String name, BigDecimal standingCharge, BigDecimal unitRatePence) {
        super(name, standingCharge, MeterType.ELECTRICITY);
        this.unitRatePence = unitRatePence;
        this.tieredPricing = false;
    }

    // Factory method for tiered pricing (to avoid constructor signature collision)
    public static ElectricityTariff createTieredTariff(String name, BigDecimal standingCharge,
                                                        double tier1Threshold, BigDecimal tier1Rate,
                                                        BigDecimal tier2Rate) {
        ElectricityTariff tariff = new ElectricityTariff();
        tariff.setName(name);
        tariff.setStandingCharge(standingCharge);
        tariff.setMeterType(MeterType.ELECTRICITY);
        tariff.tier1Threshold = tier1Threshold;
        tariff.tier1Rate = tier1Rate;
        tariff.tier2Rate = tier2Rate;
        tariff.tieredPricing = true;
        tariff.unitRatePence = tier1Rate;
        return tariff;
    }

    @Override
    public BigDecimal calculateUnitCost(double units) {
        BigDecimal cost;

        if (dayRate != null && nightRate != null) {
            // Day/night rate calculation: assume 60/40 day/night split
            double dayUnits = units * 0.6;
            double nightUnits = units * 0.4;
            cost = dayRate.multiply(BigDecimal.valueOf(dayUnits))
                    .add(nightRate.multiply(BigDecimal.valueOf(nightUnits)));
        } else if (tieredPricing) {
            cost = calculateTieredCost(units);
        } else {
            cost = unitRatePence.multiply(BigDecimal.valueOf(units));
        }

        return cost.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTieredCost(double units) {
        BigDecimal cost = BigDecimal.ZERO;

        if (units <= tier1Threshold) {
            cost = tier1Rate.multiply(BigDecimal.valueOf(units));
        } else {
            BigDecimal tier1Cost = tier1Rate.multiply(BigDecimal.valueOf(tier1Threshold));
            BigDecimal tier2Units = BigDecimal.valueOf(units - tier1Threshold);
            BigDecimal tier2Cost = tier2Rate.multiply(tier2Units);
            cost = tier1Cost.add(tier2Cost);
        }

        return cost;
    }

    @Override
    public BigDecimal getUnitRate() {
        if (dayRate != null) {
            return dayRate; // Return day rate as primary rate
        }
        return unitRatePence;
    }

    @Override
    public String getPricingDescription() {
        if (dayRate != null && nightRate != null) {
            return String.format("Day: %.2fp/kWh, Night: %.2fp/kWh", dayRate, nightRate);
        } else if (tieredPricing) {
            return String.format("First %.0f kWh at %.2fp/kWh, then %.2fp/kWh",
                    tier1Threshold, tier1Rate, tier2Rate);
        } else {
            return String.format("Flat rate: %.2fp per kWh", unitRatePence);
        }
    }

    public BigDecimal getDayRate() {
        return dayRate;
    }

    public void setDayRate(BigDecimal dayRate) {
        this.dayRate = dayRate;
    }

    public BigDecimal getNightRate() {
        return nightRate;
    }

    public void setNightRate(BigDecimal nightRate) {
        this.nightRate = nightRate;
    }

    public BigDecimal getUnitRatePence() {
        return unitRatePence;
    }

    public void setUnitRatePence(BigDecimal unitRatePence) {
        this.unitRatePence = unitRatePence;
        if (dayRate != null) {
            this.dayRate = unitRatePence;
        }
    }

    public double getTier1Threshold() {
        return tier1Threshold;
    }

    public void setTier1Threshold(double tier1Threshold) {
        this.tier1Threshold = tier1Threshold;
    }

    public BigDecimal getTier1Rate() {
        return tier1Rate;
    }

    public void setTier1Rate(BigDecimal tier1Rate) {
        this.tier1Rate = tier1Rate;
    }

    public BigDecimal getTier2Rate() {
        return tier2Rate;
    }

    public void setTier2Rate(BigDecimal tier2Rate) {
        this.tier2Rate = tier2Rate;
    }

    public boolean isTieredPricing() {
        return tieredPricing;
    }

    public void setTieredPricing(boolean tieredPricing) {
        this.tieredPricing = tieredPricing;
    }

    @Override
    public String toString() {
        return String.format("ElectricityTariff{name='%s', tiered=%s, %s}",
                name, tieredPricing, getPricingDescription());
    }
}
