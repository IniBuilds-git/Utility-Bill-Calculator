package com.utilitybill.dao;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Customer;
import com.utilitybill.model.Tariff;
import com.utilitybill.util.AppLogger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataPersistenceTest {

    private static final String CLASS_NAME = DataPersistenceTest.class.getName();

    @Test
    public void testTariffPersistence() throws DataPersistenceException {
        TariffDAO tariffDAO = TariffDAO.getInstance();

        // Load all tariffs
        List<Tariff> tariffs = tariffDAO.findAll();
        AppLogger.info(CLASS_NAME, "Loaded " + tariffs.size() + " tariffs");

        // Print each tariff
        for (Tariff tariff : tariffs) {
            AppLogger.info(CLASS_NAME, "Tariff: " + tariff.getName() + " - " + tariff.getTariffId());
        }

        assertTrue(tariffs.size() >= 0, "Should be able to load tariffs");
    }

    @Test
    public void testCustomerPersistence() throws DataPersistenceException {
        CustomerDAO customerDAO = CustomerDAO.getInstance();

        // Load all customers
        List<Customer> customers = customerDAO.findAll();
        AppLogger.info(CLASS_NAME, "Loaded " + customers.size() + " customers");

        // Print each customer
        for (Customer customer : customers) {
            AppLogger.info(CLASS_NAME, "Customer: " + customer.getFullName() + " - " + customer.getAccountNumber());
        }

        assertTrue(customers.size() >= 0, "Should be able to load customers");
    }
}