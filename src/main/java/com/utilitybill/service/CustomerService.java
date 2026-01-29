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
import com.utilitybill.util.AppLogger;
import com.utilitybill.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for customer management operations.
 * 
 * <p>
 * This service provides comprehensive customer management functionality
 * including
 * customer creation, updates, deactivation, meter management, and account
 * balance
 * operations (credit/debit).
 * </p>
 * 
 * <p>
 * The service uses the Singleton pattern and can be obtained via
 * {@link #getInstance()}.
 * </p>
 * 
 * <h2>Usage Example:</h2>
 * 
 * <pre>{@code
 * CustomerService customerService = CustomerService.getInstance();
 * 
 * // Create a new customer
 * Address address = new Address("1 High Street", "London", "SW1A 1AA");
 * Customer customer = customerService.createCustomer(
 *         "John", "Doe", "john@example.com", "07123456789",
 *         address, MeterType.ELECTRICITY, tariffId);
 * 
 * // Credit customer account
 * customerService.creditAccount(customer.getCustomerId(), new BigDecimal("100.00"));
 * }</pre>
 * 
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 * @see Customer
 * @see BillingService
 */
public class CustomerService {

    private static final String CLASS_NAME = CustomerService.class.getSimpleName();

    private static volatile CustomerService instance;
    private final CustomerDAO customerDAO;

    private CustomerService() {
        this.customerDAO = CustomerDAO.getInstance();
    }

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

    public Customer createCustomer(String firstName, String lastName, String email, String phone,
            Address address, MeterType meterType, String tariffId)
            throws ValidationException, DuplicateAccountException, DataPersistenceException {

        ValidationUtil.requireNonEmpty(firstName, "firstName");
        ValidationUtil.requireNonEmpty(lastName, "lastName");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePhone(phone);

        if (address == null || !address.isValid()) {
            throw new ValidationException("address", "Valid address is required");
        }

        if (customerDAO.emailExists(email)) {
            throw DuplicateAccountException.duplicateEmail(email);
        }

        Customer customer = new Customer(
                firstName.trim(),
                lastName.trim(),
                email.trim().toLowerCase(),
                ValidationUtil.formatPhone(phone),
                address);

        if (tariffId != null && !tariffId.isEmpty()) {
            customer.setTariffId(tariffId);
        }

        Meter meter = switch (meterType) {
            case ELECTRICITY -> Meter.createElectricityMeter(generateSerialNumber());
            case GAS -> Meter.createGasMeter(generateSerialNumber());
        };
        customer.addMeter(meter);

        customerDAO.save(customer);

        AppLogger.info(CLASS_NAME, "Customer created: " + customer.getAccountNumber() +
                " (" + customer.getFullName() + ")");

        return customer;
    }

    public Customer getCustomerById(String customerId) throws CustomerNotFoundException, DataPersistenceException {
        Optional<Customer> customerOpt = customerDAO.findById(customerId);
        if (customerOpt.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }
        return customerOpt.get();
    }

    public Customer getCustomerByAccountNumber(String accountNumber)
            throws CustomerNotFoundException, DataPersistenceException {
        Optional<Customer> customerOpt = customerDAO.findByAccountNumber(accountNumber);
        if (customerOpt.isEmpty()) {
            throw new CustomerNotFoundException(accountNumber, CustomerNotFoundException.SearchType.ACCOUNT_NUMBER);
        }
        return customerOpt.get();
    }

    public List<Customer> getAllCustomers() throws DataPersistenceException {
        return customerDAO.findAll();
    }

    public List<Customer> getActiveCustomers() throws DataPersistenceException {
        return customerDAO.findAllActive();
    }

    public List<Customer> searchCustomers(String name) throws DataPersistenceException {
        if (name == null || name.trim().isEmpty()) {
            return customerDAO.findAll();
        }
        return customerDAO.searchByName(name);
    }

    public void updateCustomer(Customer customer)
            throws ValidationException, CustomerNotFoundException, DataPersistenceException {

        ValidationUtil.requireNonEmpty(customer.getFirstName(), "firstName");
        ValidationUtil.requireNonEmpty(customer.getLastName(), "lastName");
        ValidationUtil.validateEmail(customer.getEmail());
        ValidationUtil.validatePhone(customer.getPhone());

        if (!customerDAO.exists(customer.getCustomerId())) {
            throw new CustomerNotFoundException(customer.getCustomerId());
        }

        Optional<Customer> existingEmail = customerDAO.findByEmail(customer.getEmail());
        if (existingEmail.isPresent() && !existingEmail.get().getCustomerId().equals(customer.getCustomerId())) {
            throw new ValidationException("email", "Email already in use by another customer");
        }

        customerDAO.update(customer);
        AppLogger.info(CLASS_NAME, "Customer updated: " + customer.getAccountNumber());
    }

    public void deactivateCustomer(String customerId) throws CustomerNotFoundException, DataPersistenceException {
        Customer customer = getCustomerById(customerId);
        customer.setActive(false);
        customerDAO.update(customer);
        AppLogger.info(CLASS_NAME, "Customer deactivated: " + customer.getAccountNumber());
    }

    public void reactivateCustomer(String customerId) throws CustomerNotFoundException, DataPersistenceException {
        Customer customer = getCustomerById(customerId);
        customer.setActive(true);
        customerDAO.update(customer);
    }

    public Meter addMeter(String customerId, MeterType meterType)
            throws CustomerNotFoundException, DataPersistenceException {
        Customer customer = getCustomerById(customerId);

        Meter meter = switch (meterType) {
            case ELECTRICITY -> Meter.createElectricityMeter(generateSerialNumber());
            case GAS -> Meter.createGasMeter(generateSerialNumber());
        };

        customer.addMeter(meter);
        customerDAO.update(customer);

        return meter;
    }

    public void updateTariff(String customerId, String tariffId)
            throws CustomerNotFoundException, DataPersistenceException {
        Customer customer = getCustomerById(customerId);
        customer.setTariffId(tariffId);
        customerDAO.update(customer);
    }

    public void creditAccount(String customerId, BigDecimal amount)
            throws CustomerNotFoundException, ValidationException, DataPersistenceException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("amount", "Amount must be positive");
        }

        Customer customer = getCustomerById(customerId);
        customer.creditAccount(amount);
        customerDAO.update(customer);
        AppLogger.info(CLASS_NAME, "Account credited: " + customer.getAccountNumber() +
                " amount=£" + amount);
    }

    public void debitAccount(String customerId, BigDecimal amount)
            throws CustomerNotFoundException, ValidationException, DataPersistenceException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("amount", "Amount must be positive");
        }

        Customer customer = getCustomerById(customerId);
        customer.debitAccount(amount);
        customerDAO.update(customer);
        AppLogger.info(CLASS_NAME, "Account debited: " + customer.getAccountNumber() +
                " amount=£" + amount);
    }

    public List<Customer> getCustomersWithDebt() throws DataPersistenceException {
        return customerDAO.findCustomersWithDebt();
    }

    public List<Customer> getCustomersByPostcode(String postcode) throws DataPersistenceException {
        return customerDAO.findByPostcode(postcode);
    }

    public long getCustomerCount() throws DataPersistenceException {
        return customerDAO.count();
    }

    private String generateSerialNumber() {
        return String.format("SN%d", System.currentTimeMillis());
    }
}
