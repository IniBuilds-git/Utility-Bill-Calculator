package com.utilitybill.util;

import com.utilitybill.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class BillCalculator {

    public static final BigDecimal DEFAULT_VAT_RATE = new BigDecimal("0.05");
    private static final int MONETARY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private BillCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static double calculateConsumption(double currentReading, double previousReading) {
        return currentReading - previousReading;
    }

    public static BigDecimal calculateUnitCost(double units, BigDecimal ratePence) {
        return ratePence.multiply(BigDecimal.valueOf(units))
                .divide(BigDecimal.valueOf(100), MONETARY_SCALE, ROUNDING_MODE);
    }

    public static BigDecimal calculateStandingCharge(BigDecimal dailyCharge, int days) {
        return dailyCharge.multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(100), MONETARY_SCALE, ROUNDING_MODE);
    }

    public static BigDecimal calculateVAT(BigDecimal subtotal, BigDecimal vatRate) {
        return subtotal.multiply(vatRate).setScale(MONETARY_SCALE, ROUNDING_MODE);
    }

    public static BillBreakdown calculateElectricityBill(ElectricityTariff tariff, double units, int billingDays) {
        BillBreakdown breakdown = new BillBreakdown();
        breakdown.setUnitsConsumed(units);
        breakdown.setBillingDays(billingDays);
        breakdown.setUnitRate(tariff.getUnitRate());

        BigDecimal unitCost = tariff.calculateUnitCost(units);
        breakdown.setUnitCost(unitCost);

        BigDecimal standingCharge = calculateStandingCharge(tariff.getStandingCharge(), billingDays);
        breakdown.setStandingCharge(standingCharge);

        BigDecimal subtotal = unitCost.add(standingCharge);
        breakdown.setSubtotal(subtotal);

        BigDecimal vat = calculateVAT(subtotal, tariff.getVatRate());
        breakdown.setVatAmount(vat);
        breakdown.setVatRate(tariff.getVatRate());

        BigDecimal total = subtotal.add(vat);
        breakdown.setTotal(total);

        return breakdown;
    }

    public static BillBreakdown calculateGasBill(GasTariff tariff, double units, int billingDays) {
        BillBreakdown breakdown = new BillBreakdown();
        breakdown.setUnitsConsumed(units);
        breakdown.setBillingDays(billingDays);
        breakdown.setUnitRate(tariff.getUnitRate());

        BigDecimal unitCost = tariff.calculateUnitCost(units);
        breakdown.setUnitCost(unitCost);

        BigDecimal standingCharge = calculateStandingCharge(tariff.getStandingCharge(), billingDays);
        breakdown.setStandingCharge(standingCharge);

        BigDecimal subtotal = unitCost.add(standingCharge);
        breakdown.setSubtotal(subtotal);

        BigDecimal vat = calculateVAT(subtotal, tariff.getVatRate());
        breakdown.setVatAmount(vat);
        breakdown.setVatRate(tariff.getVatRate());

        BigDecimal total = subtotal.add(vat);
        breakdown.setTotal(total);

        return breakdown;
    }

    public static int calculateBillingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static double estimateConsumption(double averageDailyUsage, int days) {
        return averageDailyUsage * days;
    }

    public static double calculateAverageDailyUsage(double totalUnits, int days) {
        if (days <= 0) {
            return 0;
        }
        return totalUnits / days;
    }

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

        public double getUnitsConsumed() {
            return unitsConsumed;
        }

        public void setUnitsConsumed(double unitsConsumed) {
            this.unitsConsumed = unitsConsumed;
        }

        public int getBillingDays() {
            return billingDays;
        }

        public void setBillingDays(int billingDays) {
            this.billingDays = billingDays;
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

        public BigDecimal getStandingCharge() {
            return standingCharge;
        }

        public void setStandingCharge(BigDecimal standingCharge) {
            this.standingCharge = standingCharge;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }

        public BigDecimal getVatRate() {
            return vatRate;
        }

        public void setVatRate(BigDecimal vatRate) {
            this.vatRate = vatRate;
        }

        public BigDecimal getVatAmount() {
            return vatAmount;
        }

        public void setVatAmount(BigDecimal vatAmount) {
            this.vatAmount = vatAmount;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

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
