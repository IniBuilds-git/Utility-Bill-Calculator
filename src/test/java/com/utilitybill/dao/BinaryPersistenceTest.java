package com.utilitybill.dao;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.*;
import com.utilitybill.util.AppLogger;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryPersistenceTest {

    private static final String CLASS_NAME = BinaryPersistenceTest.class.getName();

    @Test
    public void testTariffBinaryPersistence() throws DataPersistenceException {
        TariffDAO tariffDAO = TariffDAO.getInstance();

        // Test reading existing data
        List<Tariff> tariffs = tariffDAO.findAll();
        AppLogger.info(CLASS_NAME, "Loaded " + tariffs.size() + " tariffs from binary file");

        // Test saving new tariff
        GasTariff newTariff = new GasTariff(
                "Test Binary Tariff",
                new BigDecimal("28.00"),
                new BigDecimal("4.20"));
        newTariff.setDescription("Test tariff for binary persistence");

        int initialCount = tariffs.size();
        tariffDAO.save(newTariff);

        // Refresh and verify
        tariffDAO.refresh();
        List<Tariff> updatedTariffs = tariffDAO.findAll();
        assertEquals(initialCount + 1, updatedTariffs.size(), "Should have one more tariff");

        // Cleanup
        tariffDAO.delete(newTariff.getTariffId());
        AppLogger.info(CLASS_NAME, "✓ Binary tariff persistence test passed!");
    }

    @Test
    public void testCustomerBinaryPersistence() throws DataPersistenceException {
        CustomerDAO customerDAO = CustomerDAO.getInstance();

        // Test reading
        List<Customer> customers = customerDAO.findAll();
        AppLogger.info(CLASS_NAME, "Loaded " + customers.size() + " customers from binary file");

        // Test saving
        Address testAddress = new Address("999", "Test St", "TestCity", "TestCounty", "TE99ST", "UK");
        Customer newCustomer = new Customer("Binary", "Test", "binary@test.com", "+441234567890", testAddress);

        Meter meter = Meter.createElectricityMeter("BIN-TEST-001");
        newCustomer.addMeter(meter);

        int initialCount = customers.size();
        customerDAO.save(newCustomer);

        // Refresh and verify
        customerDAO.refresh();
        List<Customer> updatedCustomers = customerDAO.findAll();
        assertEquals(initialCount + 1, updatedCustomers.size(), "Should have one more customer");

        // Cleanup
        customerDAO.delete(newCustomer.getCustomerId());
        AppLogger.info(CLASS_NAME, "✓ Binary customer persistence test passed!");
    }
}
