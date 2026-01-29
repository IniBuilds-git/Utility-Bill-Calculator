package com.utilitybill.service;

import com.utilitybill.dao.CustomerDAO;
import com.utilitybill.dao.TariffDAO;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    private CustomerService customerService;
    private CustomerDAO customerDAO;
    private TariffDAO tariffDAO;

    @BeforeEach
    void setUp() throws Exception {
        // Clear data files
        deleteFile("data/customers.dat");
        deleteFile("data/tariffs.dat");

        // Reset DAOs
        customerDAO = CustomerDAO.getInstance();
        tariffDAO = TariffDAO.getInstance();
        
        // Initialize Service
        customerService = CustomerService.getInstance();
        
        // Setup initial tariff
        ElectricityTariff tariff = new ElectricityTariff("Standard", new BigDecimal("20.00"), new BigDecimal("15.00"));
        tariffDAO.save(tariff);
    }

    private void deleteFile(String path) {
        new File(path).delete();
    }

    @Test
    @DisplayName("Should create valid customer")
    void shouldCreateValidCustomer() throws Exception {
        Address address = new Address("1", "Street", "City", "County", "AB1 2CD");
        // Get valid tariff
        String tariffId = tariffDAO.findAll().get(0).getTariffId();
        
        Customer created = customerService.createCustomer(
            "John", "Doe", "john.doe@example.com", "07123456789",
            address, MeterType.ELECTRICITY, tariffId
        );
        
        assertNotNull(created.getCustomerId());
        assertEquals("John", created.getFirstName());
        assertEquals(tariffId, created.getTariffId());
        
        // Verify retrieval
        Customer retrieved = customerService.getCustomerById(created.getCustomerId());
        assertNotNull(retrieved);
        assertEquals(created.getCustomerId(), retrieved.getCustomerId());
    }

    @Test
    @DisplayName("Should fail to create customer with invalid data")
    void shouldFailToCreateInvalidCustomer() {
        // Test with null/empty values that should fail validation
        assertThrows(ValidationException.class, () -> 
            customerService.createCustomer(
                "", "", "invalid-email", "invalid-phone",
                null, MeterType.ELECTRICITY, "invalid-id"
            ));
    }

    @Test
    @DisplayName("Should add meter to customer")
    void shouldAddMeterToCustomer() throws Exception {
        // Create customer directly via DAO for speed
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setEmail("test@example.com"); // Required for some lookups
        customerDAO.save(customer);
        
        Meter meter = customerService.addMeter(customer.getCustomerId(), MeterType.ELECTRICITY);
        
        Customer updated = customerDAO.findById(customer.getCustomerId()).orElseThrow();
        assertEquals(1, updated.getMeters().size());
        assertEquals(meter.getMeterId(), updated.getMeters().get(0).getMeterId());
    }

    @Test
    @DisplayName("Should update customer details")
    void shouldUpdateCustomerDetails() throws Exception {
         Customer customer = new Customer();
        customer.setFirstName("OldName");
        customer.setLastName("User");
        customer.setEmail("old@example.com");
        customer.setPhone("07000000000");
        customerDAO.save(customer);
        
        // Fetch to get full object state
        customer = customerDAO.findById(customer.getCustomerId()).get();
        customer.setFirstName("NewName");
        
        customerService.updateCustomer(customer);
        
        Customer updated = customerDAO.findById(customer.getCustomerId()).get();
        assertEquals("NewName", updated.getFirstName());
    }
}
