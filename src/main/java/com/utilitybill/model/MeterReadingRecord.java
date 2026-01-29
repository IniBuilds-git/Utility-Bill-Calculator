package com.utilitybill.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class MeterReadingRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String readingId;
    private String accountNumber;
    private MeterType meterType;
    private LocalDate openingDate;
    private LocalDate closingDate;
    private double openingRead;
    private double closingRead;
    private double units;
    private double m3;
    private double kWh;

    // Gas conversion fields
    private boolean gasImperial;
    private double correctionFactor;
    private double calorificValue;

    public MeterReadingRecord() {
        this.readingId = UUID.randomUUID().toString();
    }

    public void recalc() {
        this.units = closingRead - openingRead;
        if (meterType == MeterType.GAS) {
            double effectiveM3 = units;
            if (gasImperial) {
                effectiveM3 = units * 2.83;
            }
            this.m3 = effectiveM3;
            this.kWh = (m3 * correctionFactor * calorificValue) / 3.6;
        } else {
            this.m3 = 0;
            this.kWh = units;
        }
    }

    // Getters and Setters
    public String getReadingId() { return readingId; }
    public void setReadingId(String readingId) { this.readingId = readingId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public MeterType getMeterType() { return meterType; }
    public void setMeterType(MeterType meterType) { this.meterType = meterType; }

    public LocalDate getOpeningDate() { return openingDate; }
    public void setOpeningDate(LocalDate openingDate) { this.openingDate = openingDate; }

    public LocalDate getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDate closingDate) { this.closingDate = closingDate; }

    public double getOpeningRead() { return openingRead; }
    public void setOpeningRead(double openingRead) { this.openingRead = openingRead; }

    public double getClosingRead() { return closingRead; }
    public void setClosingRead(double closingRead) { this.closingRead = closingRead; }

    public double getUnits() { return units; }
    public void setUnits(double units) { this.units = units; }

    public double getM3() { return m3; }
    public void setM3(double m3) { this.m3 = m3; }

    public double getKWh() { return kWh; }
    public void setKWh(double kWh) { this.kWh = kWh; }

    public boolean isGasImperial() { return gasImperial; }
    public void setGasImperial(boolean gasImperial) { this.gasImperial = gasImperial; }

    public double getCorrectionFactor() { return correctionFactor; }
    public void setCorrectionFactor(double correctionFactor) { this.correctionFactor = correctionFactor; }

    public double getCalorificValue() { return calorificValue; }
    public void setCalorificValue(double calorificValue) { this.calorificValue = calorificValue; }
}
