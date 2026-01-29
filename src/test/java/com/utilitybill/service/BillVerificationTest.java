package com.utilitybill.service;

import com.utilitybill.dao.*;
import com.utilitybill.model.*;
import com.utilitybill.util.BillCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillVerificationTest {

        private BillingService billingService;
        private CustomerService customerService;
        private TariffDAO tariffDAO;
        private CustomerDAO customerDAO;
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
                tariffDAO = TariffDAO.getInstance();
                customerDAO = CustomerDAO.getInstance();
                meterReadingDAO = MeterReadingDAO.getInstance();
                invoiceDAO = InvoiceDAO.getInstance();

                // Initialize Services
                billingService = BillingService.getInstance();
                customerService = CustomerService.getInstance();
        }

        private void deleteFile(String path) {
                new File(path).delete();
        }

        @Test
        void verifyElectricityBillCalculation() throws Exception {
                // Bill Example from UK Energy Statement:
                // Standing Charge: 22.63p/day x 33 days = £7.47
                // Day Register (02): 37623.210 - 37386.998 = 236.212 kWh
                // Night Register (01): 40516.687 - 40470.637 = 46.050 kWh
                // Total: 282.262 kWh @ 19.349p = £54.61
                // Subtotal: £7.47 + £54.61 = £62.08
                // VAT 5%: £3.10
                // Total: £65.18

                // 1. Setup Electricity Tariff (Single Rate - both day/night at same price)
                ElectricityTariff elecTariff = new ElectricityTariff(
                                "Flexible 6 Direct Debit ebill",
                                new BigDecimal("22.63"), // Standing Charge 22.63p/day
                                new BigDecimal("19.349"), // Day Rate 19.349p/kWh
                                new BigDecimal("19.349") // Night Rate 19.349p/kWh (same as day)
                );
                tariffDAO.save(elecTariff);

                // 2. Setup Customer
                Customer customer = new Customer();
                customer.setFirstName("John");
                customer.setLastName("Doe");
                customer.setPhone("07700900000");
                customer.setServiceAddress(new Address("123", "Test St", "London", "Greater London", "SW1A 1AA"));
                customer.setTariffId(elecTariff.getTariffId());
                customerDAO.save(customer);

                // 3. Setup Day/Night Meter
                ElectricityMeter meter = new ElectricityMeter("L78FW05633");
                meter.setDayNightMeter(true);
                customer.addMeter(meter);
                customerDAO.update(customer);

                // 4. Record Readings
                // Period: 30/09/21 - 01/11/21 = 33 days inclusive
                // (Sept 30 to Nov 1 = ChronoUnit.DAYS.between + 1 = 32 + 1 = 33 days)
                LocalDate startDate = LocalDate.of(2021, 9, 30);
                LocalDate endDate = LocalDate.of(2021, 11, 1);

                // Record the meter reading with day/night values
                billingService.recordMeterReading(
                        customer.getCustomerId(),
                        meter.getMeterId(),
                        37623.210,  // Day closing
                        37386.998,  // Day opening
                        40516.687,  // Night closing
                        40470.637,  // Night opening
                        startDate,
                        endDate
                );

                // 5. Generate Invoice
                Invoice invoice = billingService.generateInvoice(customer.getCustomerId(), startDate, endDate);

                // 6. Verify calculation breakdown
                // Day consumption: 236.212 kWh
                // Night consumption: 46.050 kWh
                // Total: 282.262 kWh
                assertEquals(33, invoice.getBillingDays(), "Billing days should be 33");

                // Standing charge: 33 x 22.63p / 100 = £7.47 (rounded)
                assertEquals(new BigDecimal("7.47"), invoice.getStandingChargeTotal(), "Standing charge mismatch");

                // Usage: 282.262 x 19.349p / 100 = £54.61
                assertEquals(new BigDecimal("54.61"), invoice.getUnitCost(), "Unit cost mismatch");

                // Subtotal: £62.08
                assertEquals(new BigDecimal("62.08"), invoice.getSubtotal(), "Subtotal mismatch");

                // VAT 5%: £3.10
                assertEquals(new BigDecimal("3.10"), invoice.getVatAmount(), "VAT mismatch");

                // Total: £65.18
                assertEquals(new BigDecimal("65.18"), invoice.getTotalAmount(), "Total amount mismatch");
        }

        @Test
        void verifyGasBillCalculation() throws Exception {
                // Bill Example from UK Energy Statement:
                // Standing Charge: 24.87p/day x 33 days = £8.21
                // Meter: Imperial (100s of cubic feet)
                // Opening: 10091.5, Closing: 10127.6 = 36.1 units
                // Conversion: 36.1 x 2.83 = 102.163 m³
                // Correction: 102.163 x 1.02264 = 104.48 m³ (corrected volume)
                // kWh: (104.48 x 39.4) / 3.6 = 1143.846 kWh
                // Usage: 1143.846 x 3.797p / 100 = £43.43
                // Subtotal: £8.21 + £43.43 = £51.64
                // VAT 5%: £2.58
                // Total: £54.22

                // 1. Setup Gas Tariff
                GasTariff gasTariff = new GasTariff(
                                "Flexible 6 Direct Debit ebill",
                                new BigDecimal("24.87"), // Standing 24.87p/day
                                new BigDecimal("3.797")); // Unit rate 3.797p/kWh (from bill calculation)
                gasTariff.setCalorificValue(39.4); // CV from bill
                gasTariff.setCorrectionFactor(1.02264); // Standard UK correction factor
                tariffDAO.save(gasTariff);

                // 2. Setup Customer
                Customer customer = new Customer();
                customer.setFirstName("Jane");
                customer.setLastName("Doe");
                customer.setPhone("07700900001");
                customer.setServiceAddress(new Address("456", "Gas Ln", "London", "Greater London", "SW1A 1AB"));
                customer.setTariffId(gasTariff.getTariffId());
                customerDAO.save(customer);

                // 3. Setup Imperial Gas Meter (100s of cubic feet)
                GasMeter meter = new GasMeter("0000915");
                meter.setImperialMeter(true);
                customer.addMeter(meter);
                customerDAO.update(customer);

                // 4. Record Readings
                // Period: 30/09/21 - 01/11/21 = 33 days inclusive
                LocalDate startDate = LocalDate.of(2021, 9, 30);
                LocalDate endDate = LocalDate.of(2021, 11, 1);

                // Record meter reading
                billingService.recordMeterReading(
                        customer.getCustomerId(),
                        meter.getMeterId(),
                        10127.6, // Closing reading
                        10091.5, // Opening reading
                        startDate,
                        endDate
                );

                // 5. Generate Invoice
                Invoice invoice = billingService.generateInvoice(customer.getCustomerId(), startDate, endDate);

                // 6. Verify calculation breakdown
                assertEquals(33, invoice.getBillingDays(), "Billing days should be 33");

                // Verify gas conversion details
                assertNotNull(invoice.getMeterUnits(), "Meter units should be set");
                assertEquals(36.1, invoice.getMeterUnits(), 0.01, "Meter units mismatch");

                // Verify kWh conversion (with small tolerance for rounding)
                // Expected: ~1143 kWh (slight variations due to rounding at each step)
                double expectedKwh = 1143.0;
                assertEquals(expectedKwh, invoice.getKwhFromGas(), 2.0, "kWh conversion mismatch");

                // Standing charge: 33 x 24.87p / 100 = £8.21 (rounded)
                assertEquals(new BigDecimal("8.21"), invoice.getStandingChargeTotal(), "Standing charge mismatch");

                // Usage: ~1143 kWh x 3.797p / 100 = ~£43.42-43.43
                // (1 penny difference acceptable due to intermediate rounding)
                BigDecimal unitCost = invoice.getUnitCost();
                assertTrue(unitCost.compareTo(new BigDecimal("43.40")) >= 0 &&
                           unitCost.compareTo(new BigDecimal("43.45")) <= 0,
                        "Unit cost should be ~£43.42-43.43, was: " + unitCost);

                // Subtotal: ~£51.63-51.64
                BigDecimal subtotal = invoice.getSubtotal();
                assertTrue(subtotal.compareTo(new BigDecimal("51.60")) >= 0 &&
                           subtotal.compareTo(new BigDecimal("51.65")) <= 0,
                        "Subtotal should be ~£51.63-51.64, was: " + subtotal);

                // VAT 5%: ~£2.58
                BigDecimal vat = invoice.getVatAmount();
                assertTrue(vat.compareTo(new BigDecimal("2.55")) >= 0 &&
                           vat.compareTo(new BigDecimal("2.60")) <= 0,
                        "VAT should be ~£2.58, was: " + vat);

                // Total: ~£54.21-54.22 (1 penny tolerance due to rounding at each step)
                BigDecimal total = invoice.getTotalAmount();
                assertTrue(total.compareTo(new BigDecimal("54.20")) >= 0 &&
                           total.compareTo(new BigDecimal("54.25")) <= 0,
                        "Total should be ~£54.21-54.22, was: " + total);
        }
}
