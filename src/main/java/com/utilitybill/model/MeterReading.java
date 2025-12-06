package com.utilitybill.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a meter reading in the Utility Bill Management System.
 * A meter reading captures the meter value at a specific point in time.
 *
 * <p>Readings can be:</p>
 * <ul>
 *   <li>ACTUAL - Customer or engineer submitted reading</li>
 *   <li>ESTIMATED - System-generated estimate based on historical usage</li>
 *   <li>SMART - Automatically submitted from smart meter</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class MeterReading implements Serializable {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Unique reading identifier */
    private String readingId;

    /** Associated meter ID */
    private String meterId;

    /** Associated customer ID */
    private String customerId;

    /** The meter reading value */
    private double readingValue;

    /** Previous reading value (for consumption calculation) */
    private double previousReadingValue;

    /** Date the reading was taken */
    private LocalDate readingDate;

    /** Timestamp when the reading was recorded in the system */
    private LocalDateTime recordedAt;

    /** Type of reading (actual, estimated, smart) */
    private ReadingType readingType;

    /** Who submitted the reading */
    private String submittedBy;

    /** Whether this reading has been used in billing */
    private boolean billed;

    /** Notes or comments about the reading */
    private String notes;

    /**
     * Enum representing the type of meter reading.
     */
    public enum ReadingType {
        /** Customer or engineer submitted actual reading */
        ACTUAL("Actual Reading"),
        /** System-generated estimate */
        ESTIMATED("Estimated"),
        /** Automatically submitted from smart meter */
        SMART("Smart Meter"),
        /** Opening reading when account starts */
        OPENING("Opening Reading"),
        /** Final reading when account closes */
        FINAL("Final Reading");

        private final String displayName;

        ReadingType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Default constructor required for JSON deserialization.
     */
    public MeterReading() {
        this.readingId = UUID.randomUUID().toString();
        this.recordedAt = LocalDateTime.now();
        this.readingDate = LocalDate.now();
        this.readingType = ReadingType.ACTUAL;
        this.billed = false;
    }

    /**
     * Constructs a new MeterReading with essential details.
     *
     * @param meterId      the meter ID
     * @param customerId   the customer ID
     * @param readingValue the reading value
     * @param readingType  the type of reading
     */
    public MeterReading(String meterId, String customerId, double readingValue, ReadingType readingType) {
        this();
        this.meterId = meterId;
        this.customerId = customerId;
        this.readingValue = readingValue;
        this.readingType = readingType;
    }

    /**
     * Constructs a new MeterReading with previous reading for consumption calculation.
     *
     * @param meterId              the meter ID
     * @param customerId           the customer ID
     * @param readingValue         the reading value
     * @param previousReadingValue the previous reading value
     * @param readingType          the type of reading
     */
    public MeterReading(String meterId, String customerId, double readingValue,
                        double previousReadingValue, ReadingType readingType) {
        this(meterId, customerId, readingValue, readingType);
        this.previousReadingValue = previousReadingValue;
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the reading ID.
     *
     * @return the reading ID
     */
    public String getReadingId() {
        return readingId;
    }

    /**
     * Sets the reading ID.
     *
     * @param readingId the reading ID to set
     */
    public void setReadingId(String readingId) {
        this.readingId = readingId;
    }

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
     * Gets the customer ID.
     *
     * @return the customer ID
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer ID.
     *
     * @param customerId the customer ID to set
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * Gets the reading value.
     *
     * @return the reading value
     */
    public double getReadingValue() {
        return readingValue;
    }

    /**
     * Sets the reading value.
     *
     * @param readingValue the reading value to set
     */
    public void setReadingValue(double readingValue) {
        this.readingValue = readingValue;
    }

    /**
     * Gets the previous reading value.
     *
     * @return the previous reading value
     */
    public double getPreviousReadingValue() {
        return previousReadingValue;
    }

    /**
     * Sets the previous reading value.
     *
     * @param previousReadingValue the previous reading value to set
     */
    public void setPreviousReadingValue(double previousReadingValue) {
        this.previousReadingValue = previousReadingValue;
    }

    /**
     * Gets the reading date.
     *
     * @return the reading date
     */
    public LocalDate getReadingDate() {
        return readingDate;
    }

    /**
     * Sets the reading date.
     *
     * @param readingDate the reading date to set
     */
    public void setReadingDate(LocalDate readingDate) {
        this.readingDate = readingDate;
    }

    /**
     * Gets the recorded timestamp.
     *
     * @return the recorded timestamp
     */
    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    /**
     * Sets the recorded timestamp.
     *
     * @param recordedAt the recorded timestamp to set
     */
    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    /**
     * Gets the reading type.
     *
     * @return the reading type
     */
    public ReadingType getReadingType() {
        return readingType;
    }

    /**
     * Sets the reading type.
     *
     * @param readingType the reading type to set
     */
    public void setReadingType(ReadingType readingType) {
        this.readingType = readingType;
    }

    /**
     * Gets who submitted the reading.
     *
     * @return the submitter
     */
    public String getSubmittedBy() {
        return submittedBy;
    }

    /**
     * Sets who submitted the reading.
     *
     * @param submittedBy the submitter to set
     */
    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    /**
     * Checks if the reading has been billed.
     *
     * @return true if billed
     */
    public boolean isBilled() {
        return billed;
    }

    /**
     * Sets the billed status.
     *
     * @param billed the billed status to set
     */
    public void setBilled(boolean billed) {
        this.billed = billed;
    }

    /**
     * Gets the notes.
     *
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the notes.
     *
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    // ==================== Business Methods ====================

    /**
     * Calculates the consumption (units used) since the previous reading.
     *
     * @return the units consumed
     */
    public double getConsumption() {
        return readingValue - previousReadingValue;
    }

    /**
     * Checks if this is an estimated reading.
     *
     * @return true if estimated
     */
    public boolean isEstimated() {
        return readingType == ReadingType.ESTIMATED;
    }

    /**
     * Checks if this is a smart meter reading.
     *
     * @return true if from smart meter
     */
    public boolean isSmartMeterReading() {
        return readingType == ReadingType.SMART;
    }

    /**
     * Validates the reading against the previous reading.
     *
     * @return true if the reading is valid (>= previous reading)
     */
    public boolean isValidReading() {
        return readingValue >= previousReadingValue;
    }

    /**
     * Marks the reading as used in billing.
     */
    public void markAsBilled() {
        this.billed = true;
    }

    // ==================== Object Methods ====================

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

