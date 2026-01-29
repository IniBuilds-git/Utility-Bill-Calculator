package com.utilitybill.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Meter implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final AtomicLong ID_COUNTER = new AtomicLong(1000);

    private String meterId;
    private MeterType meterType;
    private String serialNumber;
    private LocalDate installationDate;
    private LocalDate lastInspectionDate;
    private double currentReading;
    private boolean active;
    private double maxReading;

    public abstract double updateReading(double newReading);

    public Meter() {
        this.active = true;
        this.maxReading = 99999.99;
        this.currentReading = 0.0;
    }

    public Meter(MeterType meterType, String serialNumber) {
        this();
        this.meterId = generateMeterId(meterType);
        this.meterType = meterType;
        this.serialNumber = serialNumber;
        this.installationDate = LocalDate.now();
    }

    public static Meter createElectricityMeter(String serialNumber) {
        return new ElectricityMeter(serialNumber);
    }

    public static Meter createGasMeter(String serialNumber) {
        return new GasMeter(serialNumber);
    }

    private static String generateMeterId(MeterType type) {
        return String.format("%s-%06d", type.getPrefix(), ID_COUNTER.getAndIncrement());
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public MeterType getMeterType() {
        return meterType;
    }

    public void setMeterType(MeterType meterType) {
        this.meterType = meterType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    public LocalDate getLastInspectionDate() {
        return lastInspectionDate;
    }

    public void setLastInspectionDate(LocalDate lastInspectionDate) {
        this.lastInspectionDate = lastInspectionDate;
    }

    public double getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(double currentReading) {
        this.currentReading = currentReading;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getMaxReading() {
        return maxReading;
    }

    public void setMaxReading(double maxReading) {
        this.maxReading = maxReading;
    }

    public boolean isInspectionDue() {
        if (lastInspectionDate == null) {
            return true;
        }
        return lastInspectionDate.plusYears(1).isBefore(LocalDate.now());
    }

    public void recordInspection() {
        this.lastInspectionDate = LocalDate.now();
    }

    public int getMeterAge() {
        if (installationDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.YEARS.between(installationDate, LocalDate.now());
    }



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

