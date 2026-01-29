package com.utilitybill.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class MeterReading implements Serializable {

    private static final long serialVersionUID = 1L;

    private String readingId;
    private String meterId;
    private String customerId;
    private double readingValue;
    private double previousReadingValue;

    // For electricity day/night meters (Economy 7, Economy 10)
    private Double dayReading;
    private Double nightReading;
    private Double previousDayReading;
    private Double previousNightReading;

    // For gas meter conversion
    private boolean imperialMeter;
    private Double cubicMeters;
    private Double calorificValue;

    private LocalDate readingDate; // Closing Date
    private LocalDate periodStartDate; // Opening Date
    private LocalDateTime recordedAt;
    private String submittedBy;
    private boolean billed;

    public MeterReading() {
        this.readingId = UUID.randomUUID().toString();
        this.recordedAt = LocalDateTime.now();
        this.readingDate = LocalDate.now();
        this.billed = false;
    }

    public MeterReading(String meterId, String customerId, double readingValue) {
        this();
        this.meterId = meterId;
        this.customerId = customerId;
        this.readingValue = readingValue;
    }

    public MeterReading(String meterId, String customerId, double readingValue,
            double previousReadingValue) {
        this(meterId, customerId, readingValue);
        this.previousReadingValue = previousReadingValue;
    }

    public MeterReading(String meterId, String customerId, double readingValue,
            double previousReadingValue, LocalDate periodStartDate, LocalDate readingDate) {
        this(meterId, customerId, readingValue, previousReadingValue);
        this.periodStartDate = periodStartDate;
        this.readingDate = readingDate;
    }

    public String getReadingId() {
        return readingId;
    }

    public void setReadingId(String readingId) {
        this.readingId = readingId;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getReadingValue() {
        return readingValue;
    }

    public void setReadingValue(double readingValue) {
        this.readingValue = readingValue;
    }

    public double getPreviousReadingValue() {
        return previousReadingValue;
    }

    public void setPreviousReadingValue(double previousReadingValue) {
        this.previousReadingValue = previousReadingValue;
    }

    public LocalDate getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(LocalDate readingDate) {
        this.readingDate = readingDate;
    }

    public LocalDate getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public boolean isBilled() {
        return billed;
    }

    public void setBilled(boolean billed) {
        this.billed = billed;
    }

    public double getConsumption() {
        return readingValue - previousReadingValue;
    }

    // Day/Night electricity readings
    public Double getDayReading() {
        return dayReading;
    }

    public void setDayReading(Double dayReading) {
        this.dayReading = dayReading;
    }

    public Double getNightReading() {
        return nightReading;
    }

    public void setNightReading(Double nightReading) {
        this.nightReading = nightReading;
    }

    public Double getPreviousDayReading() {
        return previousDayReading;
    }

    public void setPreviousDayReading(Double previousDayReading) {
        this.previousDayReading = previousDayReading;
    }

    public Double getPreviousNightReading() {
        return previousNightReading;
    }

    public void setPreviousNightReading(Double previousNightReading) {
        this.previousNightReading = previousNightReading;
    }

    public boolean hasDayNightReadings() {
        return dayReading != null && nightReading != null;
    }

    public double getDayConsumption() {
        if (dayReading == null || previousDayReading == null) {
            return 0;
        }
        return dayReading - previousDayReading;
    }

    public double getNightConsumption() {
        if (nightReading == null || previousNightReading == null) {
            return 0;
        }
        return nightReading - previousNightReading;
    }

    public double getTotalDayNightConsumption() {
        return getDayConsumption() + getNightConsumption();
    }

    // Gas meter fields
    public boolean isImperialMeter() {
        return imperialMeter;
    }

    public void setImperialMeter(boolean imperialMeter) {
        this.imperialMeter = imperialMeter;
    }

    public Double getCubicMeters() {
        return cubicMeters;
    }

    public void setCubicMeters(Double cubicMeters) {
        this.cubicMeters = cubicMeters;
    }

    public Double getCalorificValue() {
        return calorificValue;
    }

    public void setCalorificValue(Double calorificValue) {
        this.calorificValue = calorificValue;
    }

    public void markAsBilled() {
        this.billed = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MeterReading that = (MeterReading) o;
        return Objects.equals(readingId, that.readingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readingId);
    }

    @Override
    public String toString() {
        return String.format("MeterReading{id='%s', meter='%s', value=%.2f, date=%s}",
                readingId, meterId, readingValue, readingDate);
    }
}
