package com.utilitybill.model;

public enum MeterType {

    ELECTRICITY("Electricity", "kWh", "ELEC"),
    GAS("Gas", "kWh", "GAS"),
    DUAL_FUEL("Dual Fuel", "kWh", "DUAL");

    private final String displayName;
    private final String unit;
    private final String prefix;

    MeterType(String displayName, String unit, String prefix) {
        this.displayName = displayName;
        this.unit = unit;
        this.prefix = prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }

    public String getPrefix() {
        return prefix;
    }

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

