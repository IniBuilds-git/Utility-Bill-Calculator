package com.utilitybill.exception;

/**
 * Exception thrown when an invalid meter reading is submitted.
 * A meter reading is invalid if it is less than the previous reading,
 * negative, or exceeds reasonable bounds.
 *
 * <p>Error Codes:</p>
 * <ul>
 *   <li>MTR001 - Reading less than previous reading</li>
 *   <li>MTR002 - Negative reading value</li>
 *   <li>MTR003 - Reading exceeds maximum allowed</li>
 *   <li>MTR004 - Invalid meter ID</li>
 *   <li>MTR005 - Duplicate reading for same period</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class InvalidMeterReadingException extends UtilityBillException {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** The invalid reading value that was submitted */
    private final double invalidReading;

    /** The previous valid reading (if applicable) */
    private final double previousReading;

    /** The meter ID associated with the reading */
    private final String meterId;

    /**
     * Constructs a new InvalidMeterReadingException for a reading less than previous.
     *
     * @param meterId         the meter ID
     * @param invalidReading  the invalid reading submitted
     * @param previousReading the previous valid reading
     */
    public InvalidMeterReadingException(String meterId, double invalidReading, double previousReading) {
        super(String.format("Invalid meter reading: %.2f is less than previous reading: %.2f for meter: %s",
                invalidReading, previousReading, meterId), "MTR001");
        this.meterId = meterId;
        this.invalidReading = invalidReading;
        this.previousReading = previousReading;
    }

    /**
     * Constructs a new InvalidMeterReadingException with a custom error code.
     *
     * @param meterId        the meter ID
     * @param invalidReading the invalid reading submitted
     * @param errorCode      the specific error code
     * @param message        custom error message
     */
    public InvalidMeterReadingException(String meterId, double invalidReading, String errorCode, String message) {
        super(message, errorCode);
        this.meterId = meterId;
        this.invalidReading = invalidReading;
        this.previousReading = 0;
    }

    /**
     * Factory method for negative reading exception.
     *
     * @param meterId        the meter ID
     * @param invalidReading the negative reading value
     * @return a new InvalidMeterReadingException
     */
    public static InvalidMeterReadingException negativeReading(String meterId, double invalidReading) {
        return new InvalidMeterReadingException(meterId, invalidReading, "MTR002",
                String.format("Meter reading cannot be negative: %.2f for meter: %s", invalidReading, meterId));
    }

    /**
     * Factory method for reading exceeding maximum.
     *
     * @param meterId        the meter ID
     * @param invalidReading the reading that exceeds maximum
     * @param maxAllowed     the maximum allowed reading
     * @return a new InvalidMeterReadingException
     */
    public static InvalidMeterReadingException exceedsMaximum(String meterId, double invalidReading, double maxAllowed) {
        return new InvalidMeterReadingException(meterId, invalidReading, "MTR003",
                String.format("Meter reading %.2f exceeds maximum allowed %.2f for meter: %s",
                        invalidReading, maxAllowed, meterId));
    }

    /**
     * Gets the invalid reading value.
     *
     * @return the invalid reading
     */
    public double getInvalidReading() {
        return invalidReading;
    }

    /**
     * Gets the previous valid reading.
     *
     * @return the previous reading
     */
    public double getPreviousReading() {
        return previousReading;
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
     * Calculates the difference between the readings.
     *
     * @return the difference (invalid - previous)
     */
    public double getReadingDifference() {
        return invalidReading - previousReading;
    }
}

