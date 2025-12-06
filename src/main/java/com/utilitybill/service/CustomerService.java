package com.utilitybill.service;

import com.utilitybill.dao.CustomerDAO;
import com.utilitybill.exception.CustomerNotFoundException;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.DuplicateAccountException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.Address;
import com.utilitybill.model.Customer;
import com.utilitybill.model.Meter;
import com.utilitybill.model.MeterType;
import com.utilitybill.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for customer management operations.
 * Implements the Singleton pattern and provides business logic for CRUD operations.
 *
 * <p>Design Pattern: Singleton - Only one instance manages customer operations.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class CustomerService {

    /** Singleton instance */
    private static volatile CustomerService instance;

    /** Data access object for customers */
    private final CustomerDAO customerDAO;

    /**
     * Private constructor for singleton pattern.
     */
    private CustomerService() {
        this.customerDAO = CustomerDAO.getInstance();
    }

    /**
     * Gets the singleton instance.
     *
     * @return the CustomerService instance
     */
    public static CustomerService getInstance() {
        if (instance == null) {
            synchronized (CustomerService.class) {
                if (instance == null) {
                    instance = new CustomerService();
                }
            }
        }
        return instance;
    }

    /**
     * Creates a new customer.
     *
     * @param firstName   the customer's first name
     * @param lastName    the customer's last name
     * @param email       the customer's email
     * @param phone       the customer's phone number
     * @param address     the service address
     * @param meterType   the type of meter to install
     * @param tariffId    the tariff ID to assign
     * @return the created customer
     * @throws ValidationException      if validation fails
     * @throws DuplicateAccountException if email already exists
     * @throws DataPersistenceException  if data access fails
     */
    public Customer createCustomer(String firstName, String lastName, String email, String phone,
                                    Address address, MeterType meterType, String tariffId)
            throws ValidationException, DuplicateAccountException, DataPersistenceException {

        // Validate inputs
        ValidationUtil.requireNonEmpty(firstName, "firstName");
        ValidationUtil.requireNonEmpty(lastName, "lastName");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePhone(phone);

        if (address == null || !address.isValid()) {
            throw new ValidationException("address", "Valid address is required");
        }

        // Check for duplicate email
        if (customerDAO.emailExists(email)) {
            throw DuplicateAccountException.duplicateEmail(email);
        }

        // Create customer
        Customer customer = new Customer(
                firstName.trim(),
                lastName.trim(),
                email.trim().toLowerCase(),
                ValidationUtil.formatPhone(phone),
                address
        );

        // Set tariff
        if (tariffId != null && !tariffId.isEmpty()) {
            customer.setTariffId(tariffId);
        }

        // Create and add meter
        Meter meter = switch (meterType) {
            case ELECTRICITY -> Meter.createElectricityMeter(generateSerialNumber());
            case GAS -> Meter.createGasMeter(generateSerialNumber());
            case DUAL_FUEL -> Meter.createDualFuelMeter(generateSerialNumber());
        };
        customer.addMeter(meter);

        // Save customer
        customerDAO.save(customer);

        return customer;
    }

    /**
     * Gets a customer by ID.
     *
     * @param customerId the customer ID
     * @return the customer
     * @throws CustomerNotFoundException if customer not found
     * @throws DataPersistenceException  if data access fails
     */
    public Customer getCustomerById(String customerId) throws CustomerNotFoundException, DataPersistenceException {
        Optional<Customer> customerOpt = customerDAO.findById(customerId);
        if (customerOpt.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }
        return customerOpt.get();
    }

    /**
     * Gets a customer by account number.
     *
     * @param accountNumber the account number
     * @return the customer
     * @throws CustomerNotFoundException if customer not found
     * @throws DataPersistenceException  if data access fails
     */
    public Customer getCustomerByAccountNumber(String accountNumber)
            throws CustomerNotFoundException, DataPersistenceException {
        Optional<Customer> customerOpt = customerDAO.findByAccountNumber(accountNumber);
        if (customerOpt.isEmpty()) {
            throw new CustomerNotFoundException(accountNumber, CustomerNotFoundException.SearchType.ACCOUNT_NUMBER);
        }
        return customerOpt.get();
    }

    /**
     * Gets all customers.
     *
     * @return list of all customers
     * @throws DataPersistenceException if data access fails
     */
    public List<Customer> getAllCustomers() throws DataPersistenceException {
        return customerDAO.findAll();
    }

    /**
     * Gets all active customers.
     *
     * @return list of active customers
     * @throws DataPersistenceException if data access fails
     */
    public List<Customer> getActiveCustomers() throws DataPersistenceException {
        return customerDAO.findAllActive();
    }

    /**
     * Searches customers by name.
     *
     * @param name the name to search (partial match)
     * @return list of matching customers
     * @throws DataPersistenceException if data access fails
     */
    public List<Customer> searchCustomers(String name) throws DataPersistenceException {
        if (name == null || name.trim().isEmpty()) {
            return customerDAO.findAll();
        }
        return customerDAO.searchByName(name);
    }

    /**
     * Updates a customer's details.
     *
     * @param customer the customer with updated details
     * @throws ValidationException       if validation fails
     * @throws CustomerNotFoundException if customer not found
     * @throws DataPersistenceException  if data access fails
     */
    public void updateCustomer(Customer customer)
            throws ValidationException, CustomerNotFoundException, DataPersistenceException {

        // Validate
        ValidationUtil.requireNonEmpty(customer.getFirstName(), "firstName");
        ValidationUtil.requireNonEmpty(customer.getLastName(), "lastName");
        ValidationUtil.validateEmail(customer.getEmail());
        ValidationUtil.validatePhone(customer.getPhone());

        // Check exists
        if (!customerDAO.exists(customer.getCustomerId())) {
            throw new CustomerNotFoundException(customer.getCustomerId());
        }

        // Check email uniqueness (excluding current customer)
        Optional<Customer> existingEmail = customerDAO.findByEmail(customer.getEmail());
        if (existingEmail.isPresent() && !existingEmail.get().getCustomerId().equals(customer.getCustomerId())) {
            throw new ValidationException("email", "Email already in use by another customer");
        }

        customerDAO.update(customer);
    }

    /**
     * Deactivates a customer account.
     *
     * @param customerId the customer ID
     * @throws CustomerNotFoundException if customer not found
     * @throws DataPersistenceException  if data access fails
     */
    public void deactivateCustomer(String customerId) throws CustomerNotFoundException, DataPersistenceException {
        Customer customer = getCustomerById(customerId);
        customer.setActive(false);
        customerDAO.update(customer);
    }

    /**
     * Reactivates a customer account.
     *
     * @param customerId the customer ID
     * @throws CustomerNotFoundException if customer not found
     * @throws DataPersistenceException  if data access fails
     */
    public void reactivateCustomer(String customerId) throws CustomerNotFoundException, DataPersistenceException {
        Customer customer = getCustomerById(customerId);
        customer.setActive(true);
        customerDAO.update(customer);
    }

    /**
     * Adds a meter to a customer.
     *
     * @param customerId the customer ID
     * @param meterType  the type of meter to add
     * @return the created meter
     * @throws CustomerNotFoundException if customer not found
     * @throws DataPersistenceException  if data access fails
     */
    public Meter addMeter(String customerId, MeterType meterType)
            throws CustomerNotFoundException, DataPersistenceException {
        Customer customer = getCustomerById(customerId);

        Meter meter = switch (meterType) {
            case ELECTRICITY -> Meter.createElectricityMeter(generateSerialNumber());
            case GAS -> Meter.createGasMeter(generateSerialNumber());
            case DUAL_FUEL -> Meter.createDualFuelMeter(generateSerialNumber());
        };

        customer.addMeter(meter);
        customerDAO.update(customer);

        return meter;
    }

    /**
     * Updates a customer's tariff.
     *
     * @param customerId the customer ID
     * @param tariffId   the new tariff ID
     * @throws CustomerNotFoundException if customer not found
     * @throws DataPersistenceException  if data access fails
     */
    public void updateTariff(String customerId, String tariffId)
            throws CustomerNotFoundException, DataPersistenceException {
        Customer customer = getCustomerById(customerId);
        customer.setTariffId(tariffId);
        customerDAO.update(customer);
    }

    /**
     * Credits a customer's account.
     *
     * @param customerId the customer ID
     * @param amount     the amount to credit
     * @throws CustomerNotFoundException if customer not found
     * @throws ValidationException       if amount is invalid
     * @throws DataPersistenceException  if data access fails
     */
    public void creditAccount(String customerId, BigDecimal amount)
            throws CustomerNotFoundException, ValidationException, DataPersistenceException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("amount", "Amount must be positive");
        }

        Customer customer = getCustomerById(customerId);
        customer.creditAccount(amount);
        customerDAO.update(customer);
    }

    /**
     * Debits a customer's account.
     *
     * @param customerId the customer ID
     * @param amount     the amount to debit
     * @throws CustomerNotFoundException if customer not found
     * @throws ValidationException       if amount is invalid
     * @throws DataPersistenceException  if data access fails
     */
    public void debitAccount(String customerId, BigDecimal amount)
            throws CustomerNotFoundException, ValidationException, DataPersistenceException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("amount", "Amount must be positive");
        }

        Customer customer = getCustomerById(customerId);
        customer.debitAccount(amount);
        customerDAO.update(customer);
    }

    /**
     * Gets customers with outstanding debt.
     *
     * @return list of customers in debt
     * @throws DataPersistenceException if data access fails
     */
    public List<Customer> getCustomersWithDebt() throws DataPersistenceException {
        return customerDAO.findCustomersWithDebt();
    }

    /**
     * Gets customers by postcode.
     *
     * @param postcode the postcode to search
     * @return list of customers at that postcode
     * @throws DataPersistenceException if data access fails
     */
    public List<Customer> getCustomersByPostcode(String postcode) throws DataPersistenceException {
        return customerDAO.findByPostcode(postcode);
    }

    /**
     * Gets total number of customers.
     *
     * @return the customer count
     * @throws DataPersistenceException if data access fails
     */
    public long getCustomerCount() throws DataPersistenceException {
        return customerDAO.count();
    }

    /**
     * Generates a meter serial number.
     */
    private String generateSerialNumber() {
        return String.format("SN%d", System.currentTimeMillis());
    }
}

