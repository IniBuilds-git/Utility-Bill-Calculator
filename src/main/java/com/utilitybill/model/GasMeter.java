package com.utilitybill.model;

public class GasMeter extends Meter {
    
     // For gas meters
    private boolean imperialMeter; // Measures in cubic feet vs cubic meters

    public GasMeter() {
        super();
        setMeterType(MeterType.GAS);
    }

    public GasMeter(String serialNumber) {
        super(MeterType.GAS, serialNumber);
        setMeterType(MeterType.GAS);
    }

    @Override
    public double updateReading(double newReading) {
         if (newReading < 0) {
            throw new IllegalArgumentException("Meter reading cannot be negative");
        }

        double unitsConsumed;
        if (newReading < getCurrentReading()) {
            // Handle meter rollover
            unitsConsumed = (getMaxReading() - getCurrentReading()) + newReading;
        } else {
            unitsConsumed = newReading - getCurrentReading();
        }

        // Convert imperial (hundreds of cubic feet) to m3 if needed, or handle in tariff? 
        // Usually consumption is stored in consistent unit (e.g. m3 or kWh).
        // For simplicity here, we assume stored reading is just the raw number.
        
        setCurrentReading(newReading);
        
        // If imperial, real consumption might need conversion logic here or in billing service
        if (imperialMeter) {
             // 100 ft3 approx 2.83 m3
             return unitsConsumed * 2.83; 
        }

        return unitsConsumed;
    }

    public boolean isImperialMeter() {
        return imperialMeter;
    }

    public void setImperialMeter(boolean imperialMeter) {
        this.imperialMeter = imperialMeter;
    }
}
