package com.utilitybill.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final AtomicLong ACCOUNT_COUNTER = new AtomicLong(100000);

    private String customerId;
    private String accountNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Address serviceAddress;
    private Address billingAddress;
    private List<Meter> meters;
    private String tariffId;
    private BigDecimal accountBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;


    public Customer() {
        this.customerId = java.util.UUID.randomUUID().toString();
        this.accountNumber = generateAccountNumber();
        this.meters = new ArrayList<>();
        this.accountBalance = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;

    }

    public Customer(String firstName, String lastName, String email,
                    String phone, Address serviceAddress) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.serviceAddress = serviceAddress;
        this.billingAddress = serviceAddress;
    }

    private static String generateAccountNumber() {
        return String.format("ACC-%06d", ACCOUNT_COUNTER.getAndIncrement());
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }

    public Address getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(Address serviceAddress) {
        this.serviceAddress = serviceAddress;
        this.updatedAt = LocalDateTime.now();
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
        this.updatedAt = LocalDateTime.now();
    }

    public List<Meter> getMeters() {
        return Collections.unmodifiableList(meters);
    }

    public void setMeters(List<Meter> meters) {
        this.meters = meters != null ? new ArrayList<>(meters) : new ArrayList<>();
    }

    public String getTariffId() {
        return tariffId;
    }

    public void setTariffId(String tariffId) {
        this.tariffId = tariffId;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }



    public void addMeter(Meter meter) {
        if (meter != null && !meters.contains(meter)) {
            meters.add(meter);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public boolean removeMeter(Meter meter) {
        boolean removed = meters.remove(meter);
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }

    public Meter getMeterById(String meterId) {
        return meters.stream()
                .filter(m -> m.getMeterId().equals(meterId))
                .findFirst()
                .orElse(null);
    }

    public List<Meter> getMetersByType(MeterType type) {
        return meters.stream()
                .filter(m -> m.getMeterType() == type)
                .toList();
    }

    public void creditAccount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.accountBalance = this.accountBalance.add(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void debitAccount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.accountBalance = this.accountBalance.subtract(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public boolean hasDebt() {
        return accountBalance.compareTo(BigDecimal.ZERO) < 0;
    }

    public BigDecimal getDebtAmount() {
        return hasDebt() ? accountBalance.negate() : BigDecimal.ZERO;
    }

    public boolean hasElectricityMeter() {
        return meters.stream().anyMatch(m ->
                m.getMeterType() == MeterType.ELECTRICITY);
    }

    public boolean hasGasMeter() {
        return meters.stream().anyMatch(m ->
                m.getMeterType() == MeterType.GAS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    @Override
    public String toString() {
        return String.format("Customer{id='%s', account='%s', name='%s', email='%s'}",
                customerId, accountNumber, getFullName(), email);
    }
}
