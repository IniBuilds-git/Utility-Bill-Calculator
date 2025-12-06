package com.utilitybill.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a customer in the Utility Bill Management System.
 * A customer has personal details, address, meter(s), and account information.
 *
 * <p>This class demonstrates:</p>
 * <ul>
 *   <li>Encapsulation - Private fields with controlled access</li>
 *   <li>Composition - Contains Address and Meter objects</li>
 *   <li>Aggregation - Associated with Tariff</li>
 *   <li>Immutable collections - Returns unmodifiable lists</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class Customer implements Serializable {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Counter for generating unique account numbers */
    private static final AtomicLong ACCOUNT_COUNTER = new AtomicLong(100000);

    /** Unique customer identifier */
    private String customerId;

    /** Account number (auto-generated, unique) */
    private String accountNumber;

    /** Customer's first name */
    private String firstName;

    /** Customer's last name */
    private String lastName;

    /** Customer's email address */
    private String email;

    /** Customer's phone number */
    private String phone;

    /** Customer's service address (Composition) */
    private Address serviceAddress;

    /** Customer's billing address (may differ from service address) */
    private Address billingAddress;

    /** List of meters associated with this customer (Composition) */
    private List<Meter> meters;

    /** Current tariff ID */
    private String tariffId;

    /** Account balance (positive = credit, negative = debt) */
    private BigDecimal accountBalance;

    /** Account creation date */
    private LocalDateTime createdAt;

    /** Last updated date */
    private LocalDateTime updatedAt;

    /** Whether the account is active */
    private boolean active;

    /** Customer type (residential or commercial) */
    private CustomerType customerType;

    /**
     * Enum representing the type of customer.
     */
    public enum CustomerType {
        /** Residential customer */
        RESIDENTIAL("Residential"),
        /** Commercial/business customer */
        COMMERCIAL("Commercial"),
        /** Industrial customer */
        INDUSTRIAL("Industrial");

        private final String displayName;

        CustomerType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Default constructor required for JSON deserialization.
     */
    public Customer() {
        this.customerId = java.util.UUID.randomUUID().toString();
        this.accountNumber = generateAccountNumber();
        this.meters = new ArrayList<>();
        this.accountBalance = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
        this.customerType = CustomerType.RESIDENTIAL;
    }

    /**
     * Constructs a new Customer with essential details.
     *
     * @param firstName      the customer's first name
     * @param lastName       the customer's last name
     * @param email          the customer's email address
     * @param phone          the customer's phone number
     * @param serviceAddress the service address
     */
    public Customer(String firstName, String lastName, String email,
                    String phone, Address serviceAddress) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.serviceAddress = serviceAddress;
        this.billingAddress = serviceAddress; // Default to same address
    }

    /**
     * Generates a unique account number.
     *
     * @return a unique account number in format ACC-XXXXXX
     */
    private static String generateAccountNumber() {
        return String.format("ACC-%06d", ACCOUNT_COUNTER.getAndIncrement());
    }

    // ==================== Getters and Setters ====================

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
     * Gets the account number.
     *
     * @return the account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the account number.
     *
     * @param accountNumber the account number to set
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Gets the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     *
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     *
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the full name.
     *
     * @return the full name (first + last)
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    /**
     * Gets the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     *
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the phone number.
     *
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number.
     *
     * @param phone the phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the service address.
     *
     * @return the service address
     */
    public Address getServiceAddress() {
        return serviceAddress;
    }

    /**
     * Sets the service address.
     *
     * @param serviceAddress the service address to set
     */
    public void setServiceAddress(Address serviceAddress) {
        this.serviceAddress = serviceAddress;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the billing address.
     *
     * @return the billing address
     */
    public Address getBillingAddress() {
        return billingAddress;
    }

    /**
     * Sets the billing address.
     *
     * @param billingAddress the billing address to set
     */
    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets an unmodifiable list of meters.
     *
     * @return the list of meters
     */
    public List<Meter> getMeters() {
        return Collections.unmodifiableList(meters);
    }

    /**
     * Sets the meters list (for JSON deserialization).
     *
     * @param meters the meters to set
     */
    public void setMeters(List<Meter> meters) {
        this.meters = meters != null ? new ArrayList<>(meters) : new ArrayList<>();
    }

    /**
     * Gets the tariff ID.
     *
     * @return the tariff ID
     */
    public String getTariffId() {
        return tariffId;
    }

    /**
     * Sets the tariff ID.
     *
     * @param tariffId the tariff ID to set
     */
    public void setTariffId(String tariffId) {
        this.tariffId = tariffId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the account balance.
     *
     * @return the account balance
     */
    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    /**
     * Sets the account balance.
     *
     * @param accountBalance the account balance to set
     */
    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the creation date.
     *
     * @return the creation date
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation date.
     *
     * @param createdAt the creation date to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update date.
     *
     * @return the last update date
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update date.
     *
     * @param updatedAt the update date to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Checks if the account is active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active status.
     *
     * @param active the active status to set
     */
    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the customer type.
     *
     * @return the customer type
     */
    public CustomerType getCustomerType() {
        return customerType;
    }

    /**
     * Sets the customer type.
     *
     * @param customerType the customer type to set
     */
    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Business Methods ====================

    /**
     * Adds a meter to this customer.
     *
     * @param meter the meter to add
     */
    public void addMeter(Meter meter) {
        if (meter != null && !meters.contains(meter)) {
            meters.add(meter);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Removes a meter from this customer.
     *
     * @param meter the meter to remove
     * @return true if removed successfully
     */
    public boolean removeMeter(Meter meter) {
        boolean removed = meters.remove(meter);
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }

    /**
     * Gets a meter by its ID.
     *
     * @param meterId the meter ID
     * @return the meter, or null if not found
     */
    public Meter getMeterById(String meterId) {
        return meters.stream()
                .filter(m -> m.getMeterId().equals(meterId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets meters of a specific type.
     *
     * @param type the meter type
     * @return list of meters of the specified type
     */
    public List<Meter> getMetersByType(MeterType type) {
        return meters.stream()
                .filter(m -> m.getMeterType() == type)
                .toList();
    }

    /**
     * Credits the account (adds money).
     *
     * @param amount the amount to credit
     */
    public void creditAccount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.accountBalance = this.accountBalance.add(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Debits the account (removes money).
     *
     * @param amount the amount to debit
     */
    public void debitAccount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.accountBalance = this.accountBalance.subtract(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if the account has a debt.
     *
     * @return true if the balance is negative
     */
    public boolean hasDebt() {
        return accountBalance.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Gets the outstanding debt amount.
     *
     * @return the debt amount (positive value), or zero if in credit
     */
    public BigDecimal getDebtAmount() {
        return hasDebt() ? accountBalance.negate() : BigDecimal.ZERO;
    }

    /**
     * Checks if the customer has an electricity meter.
     *
     * @return true if has electricity meter
     */
    public boolean hasElectricityMeter() {
        return meters.stream().anyMatch(m ->
                m.getMeterType() == MeterType.ELECTRICITY || m.getMeterType() == MeterType.DUAL_FUEL);
    }

    /**
     * Checks if the customer has a gas meter.
     *
     * @return true if has gas meter
     */
    public boolean hasGasMeter() {
        return meters.stream().anyMatch(m ->
                m.getMeterType() == MeterType.GAS || m.getMeterType() == MeterType.DUAL_FUEL);
    }

    // ==================== Object Methods ====================

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
        return String.format("Customer{id='%s', account='%s', name='%s', email='%s', type=%s}",
                customerId, accountNumber, getFullName(), email, customerType);
    }
}

