package com.utilitybill.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ElectricityMeter Tests")
class ElectricityMeterTest {

    @Test
    @DisplayName("Should initialize correctly")
    void shouldInitializeCorrectly() {
        ElectricityMeter meter = new ElectricityMeter("ELEC-001");
        assertEquals("ELEC-001", meter.getSerialNumber());
        assertEquals(MeterType.ELECTRICITY, meter.getMeterType());
        assertFalse(meter.isDayNightMeter());
    }

    @Test
    @DisplayName("Should update reading correctly")
    void shouldUpdateReadingCorrectly() {
        ElectricityMeter meter = new ElectricityMeter("ELEC-001");
        meter.setCurrentReading(100.0);
        
        double consumption = meter.updateReading(150.0);
        
        assertEquals(150.0, meter.getCurrentReading());
        assertEquals(50.0, consumption);
    }

    @Test
    @DisplayName("Should handle meter rollover")
    void shouldHandleMeterRollover() {
        ElectricityMeter meter = new ElectricityMeter("ELEC-001");
        // Default max reading is 999999
        double maxExample = 999999.0;
        meter.setMaxReading(maxExample);
        meter.setCurrentReading(maxExample);
        
        // Rollover to 50 (e.g., 999999 -> 0 -> 50)
        // Logic in Meter class: (Max - Current) + New
        // (999999 - 999999) + 50 = 50 units consuemd
        double consumption = meter.updateReading(50.0);
        
        assertEquals(50.0, meter.getCurrentReading());
        assertEquals(50.0, consumption);
    }

    @Test
    @DisplayName("Should throw exception for negative reading")
    void shouldThrowExceptionForNegativeReading() {
        ElectricityMeter meter = new ElectricityMeter("ELEC-001");
        assertThrows(IllegalArgumentException.class, () -> meter.updateReading(-10.0));
    }

    @Test
    @DisplayName("Should handle Day/Night properties")
    void shouldHandleDayNightProperties() {
        ElectricityMeter meter = new ElectricityMeter("ELEC-DN-001");
        meter.setDayNightMeter(true);
        meter.setCurrentDayReading(1000.0);
        meter.setCurrentNightReading(500.0);
        
        assertTrue(meter.isDayNightMeter());
        assertEquals(1000.0, meter.getCurrentDayReading());
        assertEquals(500.0, meter.getCurrentNightReading());
    }
}
