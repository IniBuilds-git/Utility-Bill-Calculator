package com.utilitybill.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public abstract class Tariff implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final BigDecimal STANDARD_VAT_RATE = new BigDecimal("0.05");

    protected String tariffId;
    protected String name;
    protected String description;
    protected BigDecimal standingCharge;
    protected BigDecimal vatRate;
    protected boolean active;
    protected LocalDate startDate;
    protected LocalDate endDate;
    protected MeterType meterType;

    protected Tariff() {
        this.tariffId = UUID.randomUUID().toString();
        this.vatRate = STANDARD_VAT_RATE;
        this.active = true;
        this.startDate = LocalDate.now();
    }

    protected Tariff(String name, BigDecimal standingCharge, MeterType meterType) {
        this();
        this.name = name;
        this.standingCharge = standingCharge;
        this.meterType = meterType;
    }

    public abstract BigDecimal calculateUnitCost(double units);

    public abstract BigDecimal getUnitRate();

    public abstract String getPricingDescription();

    public final BigDecimal calculateBill(double units, int billingDays) {
        BigDecimal unitCost = calculateUnitCost(units);
        BigDecimal totalStandingCharge = standingCharge.multiply(BigDecimal.valueOf(billingDays));
        BigDecimal subtotal = unitCost.add(totalStandingCharge);
        BigDecimal vat = subtotal.multiply(vatRate);
        return subtotal.add(vat).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal calculateVAT(BigDecimal subtotal) {
        return subtotal.multiply(vatRate).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public String getTariffId() {
        return tariffId;
    }

    public void setTariffId(String tariffId) {
        this.tariffId = tariffId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getStandingCharge() {
        return standingCharge;
    }

    public void setStandingCharge(BigDecimal standingCharge) {
        this.standingCharge = standingCharge;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public MeterType getMeterType() {
        return meterType;
    }

    public void setMeterType(MeterType meterType) {
        this.meterType = meterType;
    }

    public boolean isValidOn(LocalDate date) {
        if (!active) return false;
        if (date.isBefore(startDate)) return false;
        if (endDate != null && date.isAfter(endDate)) return false;
        return true;
    }

    public boolean isCurrentlyValid() {
        return isValidOn(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tariff tariff = (Tariff) o;
        return Objects.equals(tariffId, tariff.tariffId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tariffId);
    }

    @Override
    public String toString() {
        return String.format("Tariff{id='%s', name='%s', type=%s, standing=£%.2f/day}",
                tariffId, name, meterType, standingCharge);
    }
}
