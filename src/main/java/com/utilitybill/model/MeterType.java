package com.utilitybill.model;

/**
 * Enum representing the different types of utility meters.
 * This enum supports the Factory Pattern for creating meter-specific objects.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public enum MeterType {

    /** Electricity meter */
    ELECTRICITY("Electricity", "kWh", "ELEC"),

    /** Gas meter */
    GAS("Gas", "kWh", "GAS"),

    /** Dual fuel meter (electricity and gas) */
    DUAL_FUEL("Dual Fuel", "kWh", "DUAL");

    /** Display name for the meter type */
    private final String displayName;

    /** Unit of measurement */
    private final String unit;

    /** Prefix for meter ID generation */
    private final String prefix;

    /**
     * Constructs a MeterType enum value.
     *
     * @param displayName the human-readable name
     * @param unit        the unit of measurement
     * @param prefix      the meter ID prefix
     */
    MeterType(String displayName, String unit, String prefix) {
        this.displayName = displayName;
        this.unit = unit;
        this.prefix = prefix;
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the unit of measurement.
     *
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Gets the meter ID prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the enum value from display name.
     *
     * @param displayName the display name to search
     * @return the matching MeterType, or null if not found
     */
    public static MeterType fromDisplayName(String displayName) {
        for (MeterType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

