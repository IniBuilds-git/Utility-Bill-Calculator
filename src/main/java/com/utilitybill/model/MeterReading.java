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
    private LocalDate readingDate;
    private LocalDateTime recordedAt;
    private ReadingType readingType;
    private String submittedBy;
    private boolean billed;
    private String notes;

    public enum ReadingType {
        ACTUAL("Actual Reading"),
        ESTIMATED("Estimated"),
        SMART("Smart Meter"),
        OPENING("Opening Reading"),
        FINAL("Final Reading");

        private final String displayName;

        ReadingType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public MeterReading() {
        this.readingId = UUID.randomUUID().toString();
        this.recordedAt = LocalDateTime.now();
        this.readingDate = LocalDate.now();
        this.readingType = ReadingType.ACTUAL;
        this.billed = false;
    }

    public MeterReading(String meterId, String customerId, double readingValue, ReadingType readingType) {
        this();
        this.meterId = meterId;
        this.customerId = customerId;
        this.readingValue = readingValue;
        this.readingType = readingType;
    }

    public MeterReading(String meterId, String customerId, double readingValue,
                        double previousReadingValue, ReadingType readingType) {
        this(meterId, customerId, readingValue, readingType);
        this.previousReadingValue = previousReadingValue;
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

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public ReadingType getReadingType() {
        return readingType;
    }

    public void setReadingType(ReadingType readingType) {
        this.readingType = readingType;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getConsumption() {
        return readingValue - previousReadingValue;
    }

    public boolean isEstimated() {
        return readingType == ReadingType.ESTIMATED;
    }

    public boolean isSmartMeterReading() {
        return readingType == ReadingType.SMART;
    }

    public boolean isValidReading() {
        return readingValue >= previousReadingValue;
    }

    public void markAsBilled() {
        this.billed = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeterReading that = (MeterReading) o;
        return Objects.equals(readingId, that.readingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readingId);
    }

    @Override
    public String toString() {
        return String.format("MeterReading{id='%s', meter='%s', value=%.2f, type=%s, date=%s}",
                readingId, meterId, readingValue, readingType, readingDate);
    }
}
