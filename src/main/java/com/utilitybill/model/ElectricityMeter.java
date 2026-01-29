package com.utilitybill.model;

public class ElectricityMeter extends Meter {
    
    // Day/Night meter support
    private boolean dayNightMeter;
    private double currentDayReading;
    private double currentNightReading;

    public ElectricityMeter() {
        super();
        setMeterType(MeterType.ELECTRICITY);
    }

    public ElectricityMeter(String serialNumber) {
        super(MeterType.ELECTRICITY, serialNumber);
        setMeterType(MeterType.ELECTRICITY);
    }
    
    @Override
    public double updateReading(double newReading) {
        // Default simpler behavior if just single reading passed
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

        setCurrentReading(newReading);
        return unitsConsumed;
    }

    // Day/Night specific methods
    public boolean isDayNightMeter() {
        return dayNightMeter;
    }

    public void setDayNightMeter(boolean dayNightMeter) {
        this.dayNightMeter = dayNightMeter;
    }

    public double getCurrentDayReading() {
        return currentDayReading;
    }

    public void setCurrentDayReading(double currentDayReading) {
        this.currentDayReading = currentDayReading;
    }

    public double getCurrentNightReading() {
        return currentNightReading;
    }

    public void setCurrentNightReading(double currentNightReading) {
        this.currentNightReading = currentNightReading;
    }
}
