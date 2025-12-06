package com.utilitybill.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a utility meter in the Utility Bill Management System.
 * A meter is associated with a customer's property and records utility usage.
 *
 * <p>Design Pattern: Factory Pattern - The static factory methods create
 * different types of meters based on the meter type.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class Meter implements Serializable {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Counter for generating unique meter IDs */
    private static final AtomicLong ID_COUNTER = new AtomicLong(1000);

    /** Unique meter identifier */
    private String meterId;

    /** Type of meter (electricity, gas, or dual) */
    private MeterType meterType;

    /** Meter serial number */
    private String serialNumber;

    /** Installation date */
    private LocalDate installationDate;

    /** Last inspection date */
    private LocalDate lastInspectionDate;

    /** Current meter reading */
    private double currentReading;

    /** Whether the meter is active */
    private boolean active;

    /** Maximum reading before rollover (typically 99999.99 for older meters) */
    private double maxReading;

    /**
     * Default constructor required for JSON deserialization.
     */
    public Meter() {
        this.active = true;
        this.maxReading = 99999.99;
        this.currentReading = 0.0;
    }

    /**
     * Constructs a new Meter with basic details.
     *
     * @param meterType    the type of meter
     * @param serialNumber the serial number
     */
    public Meter(MeterType meterType, String serialNumber) {
        this();
        this.meterId = generateMeterId(meterType);
        this.meterType = meterType;
        this.serialNumber = serialNumber;
        this.installationDate = LocalDate.now();
    }

    /**
     * Factory method for creating an Electricity meter.
     *
     * @param serialNumber the meter serial number
     * @return a new Electricity meter
     */
    public static Meter createElectricityMeter(String serialNumber) {
        return new Meter(MeterType.ELECTRICITY, serialNumber);
    }

    /**
     * Factory method for creating a Gas meter.
     *
     * @param serialNumber the meter serial number
     * @return a new Gas meter
     */
    public static Meter createGasMeter(String serialNumber) {
        return new Meter(MeterType.GAS, serialNumber);
    }

    /**
     * Factory method for creating a Dual Fuel meter.
     *
     * @param serialNumber the meter serial number
     * @return a new Dual Fuel meter
     */
    public static Meter createDualFuelMeter(String serialNumber) {
        return new Meter(MeterType.DUAL_FUEL, serialNumber);
    }

    /**
     * Generates a unique meter ID based on meter type.
     *
     * @param type the meter type
     * @return a unique meter ID
     */
    private static String generateMeterId(MeterType type) {
        return String.format("%s-%06d", type.getPrefix(), ID_COUNTER.getAndIncrement());
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the meter ID.
     *
     * @return the meter ID
     */
    public String getMeterId() {
        return meterId;
    }

    /**
     * Sets the meter ID.
     *
     * @param meterId the meter ID to set
     */
    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    /**
     * Gets the meter type.
     *
     * @return the meter type
     */
    public MeterType getMeterType() {
        return meterType;
    }

    /**
     * Sets the meter type.
     *
     * @param meterType the meter type to set
     */
    public void setMeterType(MeterType meterType) {
        this.meterType = meterType;
    }

    /**
     * Gets the serial number.
     *
     * @return the serial number
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the serial number.
     *
     * @param serialNumber the serial number to set
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Gets the installation date.
     *
     * @return the installation date
     */
    public LocalDate getInstallationDate() {
        return installationDate;
    }

    /**
     * Sets the installation date.
     *
     * @param installationDate the installation date to set
     */
    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    /**
     * Gets the last inspection date.
     *
     * @return the last inspection date
     */
    public LocalDate getLastInspectionDate() {
        return lastInspectionDate;
    }

    /**
     * Sets the last inspection date.
     *
     * @param lastInspectionDate the last inspection date to set
     */
    public void setLastInspectionDate(LocalDate lastInspectionDate) {
        this.lastInspectionDate = lastInspectionDate;
    }

    /**
     * Gets the current reading.
     *
     * @return the current reading
     */
    public double getCurrentReading() {
        return currentReading;
    }

    /**
     * Sets the current reading.
     *
     * @param currentReading the current reading to set
     */
    public void setCurrentReading(double currentReading) {
        this.currentReading = currentReading;
    }

    /**
     * Checks if the meter is active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active status.
     *
     * @param active the active status to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the maximum reading value.
     *
     * @return the maximum reading
     */
    public double getMaxReading() {
        return maxReading;
    }

    /**
     * Sets the maximum reading value.
     *
     * @param maxReading the maximum reading to set
     */
    public void setMaxReading(double maxReading) {
        this.maxReading = maxReading;
    }

    // ==================== Business Methods ====================

    /**
     * Updates the meter reading.
     *
     * @param newReading the new reading value
     * @return the units consumed since last reading
     * @throws IllegalArgumentException if the new reading is invalid
     */
    public double updateReading(double newReading) {
        if (newReading < 0) {
            throw new IllegalArgumentException("Meter reading cannot be negative");
        }

        double unitsConsumed;
        if (newReading < currentReading) {
            // Handle meter rollover
            unitsConsumed = (maxReading - currentReading) + newReading;
        } else {
            unitsConsumed = newReading - currentReading;
        }

        this.currentReading = newReading;
        return unitsConsumed;
    }

    /**
     * Checks if the meter is due for inspection.
     * Meters should be inspected annually.
     *
     * @return true if inspection is due
     */
    public boolean isInspectionDue() {
        if (lastInspectionDate == null) {
            return true;
        }
        return lastInspectionDate.plusYears(1).isBefore(LocalDate.now());
    }

    /**
     * Records a meter inspection.
     */
    public void recordInspection() {
        this.lastInspectionDate = LocalDate.now();
    }

    /**
     * Gets the age of the meter in years.
     *
     * @return the meter age in years
     */
    public int getMeterAge() {
        if (installationDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.YEARS.between(installationDate, LocalDate.now());
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meter meter = (Meter) o;
        return Objects.equals(meterId, meter.meterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meterId);
    }

    @Override
    public String toString() {
        return String.format("Meter{id='%s', type=%s, serial='%s', reading=%.2f}",
                meterId, meterType, serialNumber, currentReading);
    }
}

