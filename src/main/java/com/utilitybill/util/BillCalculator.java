package com.utilitybill.util;

import com.utilitybill.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for bill calculations.
 * Implements the Strategy pattern for different calculation methods.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public final class BillCalculator {

    /** Default VAT rate (5% for energy) */
    public static final BigDecimal DEFAULT_VAT_RATE = new BigDecimal("0.05");

    /** Scale for monetary calculations */
    private static final int MONETARY_SCALE = 2;

    /** Rounding mode for monetary calculations */
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Private constructor to prevent instantiation.
     */
    private BillCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculates consumption from meter readings.
     *
     * @param currentReading  the current meter reading
     * @param previousReading the previous meter reading
     * @return the units consumed
     */
    public static double calculateConsumption(double currentReading, double previousReading) {
        return currentReading - previousReading;
    }

    /**
     * Calculates the cost of units at a given rate.
     *
     * @param units        the number of units consumed
     * @param ratePence    the rate in pence per unit
     * @return the cost in pounds
     */
    public static BigDecimal calculateUnitCost(double units, BigDecimal ratePence) {
        return ratePence.multiply(BigDecimal.valueOf(units))
                .divide(BigDecimal.valueOf(100), MONETARY_SCALE, ROUNDING_MODE);
    }

    /**
     * Calculates standing charge for a billing period.
     *
     * @param dailyCharge the daily standing charge in pounds
     * @param days        the number of days in the billing period
     * @return the total standing charge
     */
    public static BigDecimal calculateStandingCharge(BigDecimal dailyCharge, int days) {
        return dailyCharge.multiply(BigDecimal.valueOf(days))
                .setScale(MONETARY_SCALE, ROUNDING_MODE);
    }

    /**
     * Calculates VAT on a subtotal.
     *
     * @param subtotal the subtotal amount
     * @param vatRate  the VAT rate as a decimal (e.g., 0.05 for 5%)
     * @return the VAT amount
     */
    public static BigDecimal calculateVAT(BigDecimal subtotal, BigDecimal vatRate) {
        return subtotal.multiply(vatRate).setScale(MONETARY_SCALE, ROUNDING_MODE);
    }

    /**
     * Calculates the total bill for an electricity tariff.
     *
     * @param tariff      the electricity tariff
     * @param units       the units consumed
     * @param billingDays the number of billing days
     * @return a BillBreakdown with all cost components
     */
    public static BillBreakdown calculateElectricityBill(ElectricityTariff tariff, double units, int billingDays) {
        BillBreakdown breakdown = new BillBreakdown();
        breakdown.setUnitsConsumed(units);
        breakdown.setBillingDays(billingDays);
        breakdown.setUnitRate(tariff.getUnitRate());

        // Calculate unit cost
        BigDecimal unitCost = tariff.calculateUnitCost(units);
        breakdown.setUnitCost(unitCost);

        // Calculate standing charge
        BigDecimal standingCharge = calculateStandingCharge(tariff.getStandingCharge(), billingDays);
        breakdown.setStandingCharge(standingCharge);

        // Calculate subtotal
        BigDecimal subtotal = unitCost.add(standingCharge);
        breakdown.setSubtotal(subtotal);

        // Calculate VAT
        BigDecimal vat = calculateVAT(subtotal, tariff.getVatRate());
        breakdown.setVatAmount(vat);
        breakdown.setVatRate(tariff.getVatRate());

        // Calculate total
        BigDecimal total = subtotal.add(vat);
        breakdown.setTotal(total);

        return breakdown;
    }

    /**
     * Calculates the total bill for a gas tariff.
     *
     * @param tariff      the gas tariff
     * @param units       the units consumed (in kWh)
     * @param billingDays the number of billing days
     * @return a BillBreakdown with all cost components
     */
    public static BillBreakdown calculateGasBill(GasTariff tariff, double units, int billingDays) {
        BillBreakdown breakdown = new BillBreakdown();
        breakdown.setUnitsConsumed(units);
        breakdown.setBillingDays(billingDays);
        breakdown.setUnitRate(tariff.getUnitRate());

        // Calculate unit cost
        BigDecimal unitCost = tariff.calculateUnitCost(units);
        breakdown.setUnitCost(unitCost);

        // Calculate standing charge
        BigDecimal standingCharge = calculateStandingCharge(tariff.getStandingCharge(), billingDays);
        breakdown.setStandingCharge(standingCharge);

        // Calculate subtotal
        BigDecimal subtotal = unitCost.add(standingCharge);
        breakdown.setSubtotal(subtotal);

        // Calculate VAT
        BigDecimal vat = calculateVAT(subtotal, tariff.getVatRate());
        breakdown.setVatAmount(vat);
        breakdown.setVatRate(tariff.getVatRate());

        // Calculate total
        BigDecimal total = subtotal.add(vat);
        breakdown.setTotal(total);

        return breakdown;
    }

    /**
     * Calculates the number of billing days between two dates.
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return the number of days
     */
    public static int calculateBillingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Estimates consumption based on historical data.
     *
     * @param averageDailyUsage the average daily usage
     * @param days              the number of days to estimate
     * @return the estimated consumption
     */
    public static double estimateConsumption(double averageDailyUsage, int days) {
        return averageDailyUsage * days;
    }

    /**
     * Calculates average daily usage from historical consumption.
     *
     * @param totalUnits the total units consumed
     * @param days       the number of days
     * @return the average daily usage
     */
    public static double calculateAverageDailyUsage(double totalUnits, int days) {
        if (days <= 0) {
            return 0;
        }
        return totalUnits / days;
    }

    /**
     * Represents a breakdown of bill components.
     */
    public static class BillBreakdown {
        private double unitsConsumed;
        private int billingDays;
        private BigDecimal unitRate;
        private BigDecimal unitCost;
        private BigDecimal standingCharge;
        private BigDecimal subtotal;
        private BigDecimal vatRate;
        private BigDecimal vatAmount;
        private BigDecimal total;

        // Getters and setters
        public double getUnitsConsumed() { return unitsConsumed; }
        public void setUnitsConsumed(double unitsConsumed) { this.unitsConsumed = unitsConsumed; }

        public int getBillingDays() { return billingDays; }
        public void setBillingDays(int billingDays) { this.billingDays = billingDays; }

        public BigDecimal getUnitRate() { return unitRate; }
        public void setUnitRate(BigDecimal unitRate) { this.unitRate = unitRate; }

        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

        public BigDecimal getStandingCharge() { return standingCharge; }
        public void setStandingCharge(BigDecimal standingCharge) { this.standingCharge = standingCharge; }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

        public BigDecimal getVatRate() { return vatRate; }
        public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }

        public BigDecimal getVatAmount() { return vatAmount; }
        public void setVatAmount(BigDecimal vatAmount) { this.vatAmount = vatAmount; }

        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }

        /**
         * Gets VAT rate as percentage.
         *
         * @return VAT percentage (e.g., 5.0 for 5%)
         */
        public double getVatPercentage() {
            return vatRate.multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        @Override
        public String toString() {
            return String.format(
                    "Bill Breakdown:%n" +
                            "  Units: %.2f @ %.2fp/unit = £%.2f%n" +
                            "  Standing Charge (%d days): £%.2f%n" +
                            "  Subtotal: £%.2f%n" +
                            "  VAT (%.1f%%): £%.2f%n" +
                            "  Total: £%.2f",
                    unitsConsumed, unitRate, unitCost,
                    billingDays, standingCharge,
                    subtotal,
                    getVatPercentage(), vatAmount,
                    total);
        }
    }
}

