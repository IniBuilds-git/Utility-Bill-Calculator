package com.utilitybill.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ElectricityTariff Tests")
class ElectricityTariffTest {

    @Test
    @DisplayName("Should calculate flat rate cost correctly")
    void shouldCalculateFlatRateCost() {
        ElectricityTariff tariff = new ElectricityTariff("Flat Rate", BigDecimal.TEN, new BigDecimal("20.00")); // 20p/kWh
        
        // 100 units * 20p = 2000p = £20.00
        BigDecimal cost = tariff.calculateUnitCost(100.0);
        
        assertEquals(new BigDecimal("20.00"), cost);
    }

    @Test
    @DisplayName("Should calculate Day/Night split (60/40) when using single unit input")
    void shouldCalculateDayNightSplitCost() {
        // Day: 20p, Night: 10p
        ElectricityTariff tariff = new ElectricityTariff("Day/Night", BigDecimal.TEN, 
                new BigDecimal("20.00"), new BigDecimal("10.00"));
        
        // 100 units total
        // 60 units * 20p = 1200p
        // 40 units * 10p = 400p
        // Total = 1600p = £16.00
        BigDecimal cost = tariff.calculateUnitCost(100.0);
        
        assertEquals(new BigDecimal("16.00"), cost);
    }

    @Test
    @DisplayName("Should calculate tiered pricing correctly")
    void shouldCalculateTieredPricing() {
        // Tier 1: 100 units @ 10p
        // Tier 2: Excess @ 20p
        ElectricityTariff tariff = ElectricityTariff.createTieredTariff("Tiered", BigDecimal.TEN,
                100.0, new BigDecimal("10.00"), new BigDecimal("20.00"));
        
        // Case 1: Within Tier 1 (50 units)
        // 50 * 10p = 500p = £5.00
        assertEquals(new BigDecimal("5.00"), tariff.calculateUnitCost(50.0));
        
        // Case 2: Exceeding Tier 1 (150 units)
        // 100 * 10p = 1000p
        // 50 * 20p = 1000p
        // Total = 2000p = £20.00
        assertEquals(new BigDecimal("20.00"), tariff.calculateUnitCost(150.0));
    }

    @Test
    @DisplayName("Should provide correct pricing description")
    void shouldProvidePricingDescription() {
        ElectricityTariff flat = new ElectricityTariff("Flat", BigDecimal.ZERO, new BigDecimal("15.50"));
        assertTrue(flat.getPricingDescription().contains("Flat rate: 15.50p"));
        
        ElectricityTariff dn = new ElectricityTariff("DN", BigDecimal.ZERO, new BigDecimal("20.00"), new BigDecimal("10.00"));
        assertTrue(dn.getPricingDescription().contains("Day: 20.00p"));
        assertTrue(dn.getPricingDescription().contains("Night: 10.00p"));
    }
}
