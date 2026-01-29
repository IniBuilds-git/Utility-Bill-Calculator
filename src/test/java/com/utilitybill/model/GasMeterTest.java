package com.utilitybill.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GasMeter Tests")
class GasMeterTest {

    @Test
    @DisplayName("Should initialize correctly")
    void shouldInitializeCorrectly() {
        GasMeter meter = new GasMeter("GAS-001");
        assertEquals("GAS-001", meter.getSerialNumber());
        assertEquals(MeterType.GAS, meter.getMeterType());
        assertFalse(meter.isImperialMeter());
    }

    @Test
    @DisplayName("Should update metric reading correctly")
    void shouldUpdateMetricReading() {
        GasMeter meter = new GasMeter("GAS-001");
        meter.setImperialMeter(false);
        meter.setCurrentReading(100.0);
        
        double consumption = meter.updateReading(150.0);
        
        assertEquals(150.0, meter.getCurrentReading());
        assertEquals(50.0, consumption);
    }

    @Test
    @DisplayName("Should update imperial reading with conversion")
    void shouldUpdateImperialReadingWithConversion() {
        GasMeter meter = new GasMeter("GAS-IMP-001");
        meter.setImperialMeter(true);
        meter.setCurrentReading(100.0);
        
        // 50 units diff * 2.83 conversion
        double consumption = meter.updateReading(150.0);
        
        assertEquals(150.0, meter.getCurrentReading());
        assertEquals(50.0 * 2.83, consumption, 0.001);
    }

    @Test
    @DisplayName("Should handle meter rollover for gas")
    void shouldHandleRollover() {
        GasMeter meter = new GasMeter("GAS-001");
        meter.setCurrentReading(meter.getMaxReading());
        
        double consumption = meter.updateReading(10.0);
        
        assertEquals(10.0, meter.getCurrentReading());
        assertEquals(10.0, consumption);
    }
}
