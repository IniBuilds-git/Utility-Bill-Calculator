package com.utilitybill.util;

import com.utilitybill.model.ElectricityTariff;
import com.utilitybill.model.GasTariff;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BillCalculator class.
 * Tests billing calculations including unit costs, standing charges, and VAT.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
@DisplayName("BillCalculator Tests")
class BillCalculatorTest {

    @Nested
    @DisplayName("Consumption Calculation Tests")
    class ConsumptionTests {

        @Test
        @DisplayName("Should calculate consumption correctly")
        void shouldCalculateConsumption() {
            assertEquals(100.0, BillCalculator.calculateConsumption(500, 400));
            assertEquals(0.0, BillCalculator.calculateConsumption(100, 100));
            assertEquals(50.5, BillCalculator.calculateConsumption(100.5, 50));
        }

        @Test
        @DisplayName("Should calculate billing days correctly")
        void shouldCalculateBillingDays() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 31);

            assertEquals(31, BillCalculator.calculateBillingDays(start, end));

            // Same day
            assertEquals(1, BillCalculator.calculateBillingDays(start, start));

            // Month period
            assertEquals(30, BillCalculator.calculateBillingDays(
                    LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 30)));
        }

        @Test
        @DisplayName("Should handle null dates")
        void shouldHandleNullDates() {
            assertEquals(0, BillCalculator.calculateBillingDays(null, LocalDate.now()));
            assertEquals(0, BillCalculator.calculateBillingDays(LocalDate.now(), null));
            assertEquals(0, BillCalculator.calculateBillingDays(null, null));
        }
    }

    @Nested
    @DisplayName("Unit Cost Calculation Tests")
    class UnitCostTests {

        @Test
        @DisplayName("Should calculate unit cost correctly")
        void shouldCalculateUnitCost() {
            // 100 units at 28.62p per kWh = £28.62
            BigDecimal cost = BillCalculator.calculateUnitCost(100, new BigDecimal("28.62"));
            assertEquals(new BigDecimal("28.62"), cost);

            // 250 units at 30p per kWh = £75.00
            cost = BillCalculator.calculateUnitCost(250, new BigDecimal("30.00"));
            assertEquals(new BigDecimal("75.00"), cost);
        }

        @Test
        @DisplayName("Should handle zero units")
        void shouldHandleZeroUnits() {
            BigDecimal cost = BillCalculator.calculateUnitCost(0, new BigDecimal("28.62"));
            assertEquals(new BigDecimal("0.00"), cost);
        }
    }

    @Nested
    @DisplayName("Standing Charge Tests")
    class StandingChargeTests {

        @Test
        @DisplayName("Should calculate standing charge correctly")
        void shouldCalculateStandingCharge() {
            // 30 days at £0.45/day = £13.50
            BigDecimal charge = BillCalculator.calculateStandingCharge(new BigDecimal("0.45"), 30);
            assertEquals(new BigDecimal("13.50"), charge);

            // 31 days at £0.30/day = £9.30
            charge = BillCalculator.calculateStandingCharge(new BigDecimal("0.30"), 31);
            assertEquals(new BigDecimal("9.30"), charge);
        }

        @Test
        @DisplayName("Should handle zero days")
        void shouldHandleZeroDays() {
            BigDecimal charge = BillCalculator.calculateStandingCharge(new BigDecimal("0.45"), 0);
            assertEquals(new BigDecimal("0.00"), charge);
        }
    }

    @Nested
    @DisplayName("VAT Calculation Tests")
    class VatTests {

        @Test
        @DisplayName("Should calculate VAT at 5%")
        void shouldCalculateVat() {
            // 5% VAT on £100 = £5.00
            BigDecimal vat = BillCalculator.calculateVAT(new BigDecimal("100.00"), new BigDecimal("0.05"));
            assertEquals(new BigDecimal("5.00"), vat);

            // 5% VAT on £42.12 = £2.11
            vat = BillCalculator.calculateVAT(new BigDecimal("42.12"), new BigDecimal("0.05"));
            assertEquals(new BigDecimal("2.11"), vat);
        }
    }

    @Nested
    @DisplayName("Electricity Bill Calculation Tests")
    class ElectricityBillTests {

        @Test
        @DisplayName("Should calculate flat-rate electricity bill")
        void shouldCalculateFlatRateBill() {
            ElectricityTariff tariff = new ElectricityTariff(
                    "Test Tariff",
                    new BigDecimal("0.45"),  // 45p standing charge per day
                    new BigDecimal("28.62")   // 28.62p per kWh
            );

            // 100 kWh over 30 days
            BillCalculator.BillBreakdown breakdown = BillCalculator.calculateElectricityBill(tariff, 100, 30);

            assertEquals(100.0, breakdown.getUnitsConsumed());
            assertEquals(30, breakdown.getBillingDays());

            // Unit cost: 100 * 28.62p = £28.62
            assertEquals(new BigDecimal("28.62"), breakdown.getUnitCost());

            // Standing charge: 30 * £0.45 = £13.50
            assertEquals(new BigDecimal("13.50"), breakdown.getStandingCharge());

            // Subtotal: £28.62 + £13.50 = £42.12
            assertEquals(new BigDecimal("42.12"), breakdown.getSubtotal());

            // VAT (5%): £42.12 * 0.05 = £2.11
            assertEquals(new BigDecimal("2.11"), breakdown.getVatAmount());

            // Total: £42.12 + £2.11 = £44.23
            assertEquals(new BigDecimal("44.23"), breakdown.getTotal());
        }

        @Test
        @DisplayName("Should calculate tiered electricity bill")
        void shouldCalculateTieredBill() {
            ElectricityTariff tariff = new ElectricityTariff(
                    "Tiered Tariff",
                    new BigDecimal("0.40"),   // 40p standing charge per day
                    500,                        // First 500 kWh at tier 1 rate
                    new BigDecimal("25.00"),   // 25p per kWh for tier 1
                    new BigDecimal("30.00")    // 30p per kWh for tier 2
            );

            // 600 kWh - should use both tiers
            // First 500 kWh: 500 * 25p = £125
            // Next 100 kWh: 100 * 30p = £30
            // Total unit cost: £155

            BillCalculator.BillBreakdown breakdown = BillCalculator.calculateElectricityBill(tariff, 600, 30);

            assertEquals(new BigDecimal("155.00"), breakdown.getUnitCost());
        }
    }

    @Nested
    @DisplayName("Gas Bill Calculation Tests")
    class GasBillTests {

        @Test
        @DisplayName("Should calculate gas bill")
        void shouldCalculateGasBill() {
            GasTariff tariff = new GasTariff(
                    "Test Gas Tariff",
                    new BigDecimal("0.30"),  // 30p standing charge per day
                    new BigDecimal("7.42")    // 7.42p per kWh
            );

            // 500 kWh over 31 days
            BillCalculator.BillBreakdown breakdown = BillCalculator.calculateGasBill(tariff, 500, 31);

            assertEquals(500.0, breakdown.getUnitsConsumed());
            assertEquals(31, breakdown.getBillingDays());

            // Unit cost: 500 * 7.42p = £37.10
            assertEquals(new BigDecimal("37.10"), breakdown.getUnitCost());

            // Standing charge: 31 * £0.30 = £9.30
            assertEquals(new BigDecimal("9.30"), breakdown.getStandingCharge());
        }
    }

    @Nested
    @DisplayName("Average Usage Tests")
    class AverageUsageTests {

        @Test
        @DisplayName("Should calculate average daily usage")
        void shouldCalculateAverageDailyUsage() {
            // 300 units over 30 days = 10 units/day
            assertEquals(10.0, BillCalculator.calculateAverageDailyUsage(300, 30));

            // Handle zero days
            assertEquals(0.0, BillCalculator.calculateAverageDailyUsage(300, 0));
        }

        @Test
        @DisplayName("Should estimate consumption")
        void shouldEstimateConsumption() {
            // 10 units/day for 30 days = 300 units
            assertEquals(300.0, BillCalculator.estimateConsumption(10, 30));
        }
    }

    @Nested
    @DisplayName("Bill Breakdown Tests")
    class BillBreakdownTests {

        @Test
        @DisplayName("Should format breakdown correctly")
        void shouldFormatBreakdown() {
            BillCalculator.BillBreakdown breakdown = new BillCalculator.BillBreakdown();
            breakdown.setUnitsConsumed(100);
            breakdown.setBillingDays(30);
            breakdown.setUnitRate(new BigDecimal("28.62"));
            breakdown.setUnitCost(new BigDecimal("28.62"));
            breakdown.setStandingCharge(new BigDecimal("13.50"));
            breakdown.setSubtotal(new BigDecimal("42.12"));
            breakdown.setVatRate(new BigDecimal("0.05"));
            breakdown.setVatAmount(new BigDecimal("2.11"));
            breakdown.setTotal(new BigDecimal("44.23"));

            String formatted = breakdown.toString();

            assertTrue(formatted.contains("100.00"));
            assertTrue(formatted.contains("28.62"));
            assertTrue(formatted.contains("13.50"));
            assertTrue(formatted.contains("42.12"));
            assertTrue(formatted.contains("5.0%"));
            assertTrue(formatted.contains("44.23"));
        }

        @Test
        @DisplayName("Should calculate VAT percentage")
        void shouldCalculateVatPercentage() {
            BillCalculator.BillBreakdown breakdown = new BillCalculator.BillBreakdown();
            breakdown.setVatRate(new BigDecimal("0.05"));

            assertEquals(5.0, breakdown.getVatPercentage());
        }
    }
}

