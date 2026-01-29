package com.utilitybill.service;

import com.utilitybill.dao.*;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BillingService Tests")
class BillingServiceTest {

    private BillingService billingService;
    private CustomerDAO customerDAO;
    private TariffDAO tariffDAO;
    private MeterReadingDAO meterReadingDAO;
    private InvoiceDAO invoiceDAO;

    @BeforeEach
    void setUp() {
        // Clear data
        deleteFile("data/customers.dat");
        deleteFile("data/tariffs.dat");
        deleteFile("data/meter_readings.dat");
        deleteFile("data/invoices.dat");

        // Reset DAOs
        customerDAO = CustomerDAO.getInstance();
        tariffDAO = TariffDAO.getInstance();
        meterReadingDAO = MeterReadingDAO.getInstance();
        invoiceDAO = InvoiceDAO.getInstance();
        
        billingService = BillingService.getInstance();
    }

    private void deleteFile(String path) {
        new File(path).delete();
    }

    @Test
    @DisplayName("Should record valid meter reading")
    void shouldRecordValidMeterReading() throws Exception {
        // Setup
        Customer customer = createTestCustomer();
        Meter meter = customer.getMeters().get(0);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        // Explicitly use 0.0 for previous reading as this is first reading
        billingService.recordMeterReading(
                customer.getCustomerId(),
                meter.getMeterId(),
                100.0, // Current
                50.0,  // Previous
                startDate,
                endDate
        );

        assertEquals(100.0, meter.getCurrentReading());
        assertEquals(1, meterReadingDAO.findAll().size());
    }
    
    @Test
    @DisplayName("Should fail recording with invalid dates")
    void shouldFailWithInvalidDates() throws Exception {
        Customer customer = createTestCustomer();
        Meter meter = customer.getMeters().get(0);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.plusDays(1); 

        assertThrows(ValidationException.class, () ->
            billingService.recordMeterReading(
                customer.getCustomerId(),
                meter.getMeterId(),
                100.0, 50.0, startDate, endDate
            )
        );
    }

    @Test
    @DisplayName("Should generate invoice")
    void shouldGenerateInvoice() throws Exception {
        Customer customer = createTestCustomer();
        Meter meter = customer.getMeters().get(0);
        
        // Record reading first
        billingService.recordMeterReading(
                customer.getCustomerId(),
                meter.getMeterId(),
                100.0, 0.0, 
                LocalDate.now().minusDays(30), LocalDate.now()
        );

        Invoice invoice = billingService.generateInvoice(
                customer.getCustomerId(),
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        assertNotNull(invoice);
        assertEquals(customer.getCustomerId(), invoice.getCustomerId());
        assertTrue(invoice.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(Invoice.InvoiceStatus.PENDING, invoice.getStatus());
    }

    // Removed shouldCalculateBillDryRun as BillingService does not support dry-runs without generation

    private Customer createTestCustomer() throws Exception {
        ElectricityTariff tariff = new ElectricityTariff("Standard", new BigDecimal("20.00"), new BigDecimal("15.00"));
        tariffDAO.save(tariff);

        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setTariffId(tariff.getTariffId());
        
        ElectricityMeter meter = new ElectricityMeter("METER-TEST");
        customer.addMeter(meter);
        
        customerDAO.save(customer);
        return customer;
    }
}
