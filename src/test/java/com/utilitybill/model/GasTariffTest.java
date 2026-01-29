package com.utilitybill.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the GasTariff model class.
 * Tests gas unit conversion, kWh calculation, and cost calculation.
 */
@DisplayName("GasTariff Model Tests")
class GasTariffTest {

    private GasTariff tariff;

    @BeforeEach
    void setUp() {
        tariff = new GasTariff("Standard Gas", new BigDecimal("30.00"), new BigDecimal("7.42"));
    }

    @Nested
    @DisplayName("Tariff Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create tariff with correct values")
        void shouldCreateTariff() {
            assertEquals("Standard Gas", tariff.getName());
            assertEquals(new BigDecimal("30.00"), tariff.getStandingCharge());
            assertEquals(new BigDecimal("7.42"), tariff.getUnitRatePence());
        }

        @Test
        @DisplayName("Should have GAS meter type")
        void shouldHaveGasMeterType() {
            assertEquals(MeterType.GAS, tariff.getMeterType());
        }

        @Test
        @DisplayName("Should have default calorific value of 39.4")
        void shouldHaveDefaultCalorificValue() {
            assertEquals(39.4, tariff.getCalorificValue());
        }

        @Test
        @DisplayName("Should have correct constants")
        void shouldHaveCorrectConstants() {
            assertEquals(1.02264, GasTariff.DEFAULT_CORRECTION_FACTOR);
            assertEquals(new BigDecimal("3.6"), GasTariff.KWH_DIVISOR);
            assertEquals(39.4, GasTariff.DEFAULT_CALORIFIC_VALUE);
            assertEquals(2.83, GasTariff.IMPERIAL_TO_METRIC);
        }
    }

    @Nested
    @DisplayName("Unit Cost Calculation Tests")
    class UnitCostTests {

        @Test
        @DisplayName("Should calculate unit cost correctly")
        void shouldCalculateUnitCost() {
            // 500 kWh at 7.42p/kWh = £37.10
            BigDecimal cost = tariff.calculateUnitCost(500);
            assertEquals(new BigDecimal("37.10"), cost);
        }

        @Test
        @DisplayName("Should handle zero units")
        void shouldHandleZeroUnits() {
            BigDecimal cost = tariff.calculateUnitCost(0);
            assertEquals(new BigDecimal("0.00"), cost);
        }

        @ParameterizedTest
        @CsvSource({
                "100, 7.42",
                "250, 18.55",
                "1000, 74.20"
        })
        @DisplayName("Should calculate various unit costs correctly")
        void shouldCalculateVariousUnitCosts(double units, String expectedCost) {
            BigDecimal cost = tariff.calculateUnitCost(units);
            assertEquals(new BigDecimal(expectedCost), cost);
        }
    }

    @Nested
    @DisplayName("kWh Conversion Tests")
    class KwhConversionTests {

        @Test
        @DisplayName("Should convert imperial units to kWh correctly")
        void shouldConvertImperialToKwh() {
            // 100 imperial units -> 283 m³ -> 289.4 corrected -> kWh
            // (283 * 1.02264 * 39.4) / 3.6 = 3171.8 kWh (approximately)
            double kwh = tariff.convertToKwh(100, true);

            // Calculate expected: (100 * 2.83 * 1.02264 * 39.4) / 3.6
            double expected = (100 * 2.83 * 1.02264 * 39.4) / 3.6;
            assertEquals(expected, kwh, 0.01);
        }

        @Test
        @DisplayName("Should convert metric units to kWh correctly")
        void shouldConvertMetricToKwh() {
            // 100 m³ direct -> kWh
            double kwh = tariff.convertToKwh(100, false);

            // Calculate expected: (100 * 1.02264 * 39.4) / 3.6
            double expected = (100 * 1.02264 * 39.4) / 3.6;
            assertEquals(expected, kwh, 0.01);
        }

        @Test
        @DisplayName("Should handle zero units in conversion")
        void shouldHandleZeroUnitsConversion() {
            double kwh = tariff.convertToKwh(0, true);
            assertEquals(0.0, kwh);
        }

        @Test
        @DisplayName("Should use custom calorific value")
        void shouldUseCustomCalorificValue() {
            tariff.setCalorificValue(40.0);

            double kwh = tariff.convertToKwh(100, false);
            double expected = (100 * 1.02264 * 40.0) / 3.6;
            assertEquals(expected, kwh, 0.01);
        }
    }

    @Nested
    @DisplayName("Pricing Description Tests")
    class PricingDescriptionTests {

        @Test
        @DisplayName("Should return correct pricing description")
        void shouldReturnPricingDescription() {
            String description = tariff.getPricingDescription();
            assertTrue(description.contains("7.42"));
            assertTrue(description.contains("kWh"));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should format toString correctly")
        void shouldFormatToString() {
            String str = tariff.toString();
            assertTrue(str.contains("Standard Gas"));
            assertTrue(str.contains("7.42"));
            assertTrue(str.contains("39.4"));
        }
    }

    @Nested
    @DisplayName("Display Mode Tests")
    class DisplayModeTests {

        @Test
        @DisplayName("Should toggle cubic meters display mode")
        void shouldToggleCubicMetersMode() {
            assertFalse(tariff.isShowInCubicMeters());
            tariff.setShowInCubicMeters(true);
            assertTrue(tariff.isShowInCubicMeters());
        }
    }
}
