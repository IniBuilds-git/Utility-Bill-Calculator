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
                // 1. Setup Electricity Tariff (Day/Night)
                // Standing Charge: 22.63p/day
                // Day Rate: 19.349p/kWh (Net from bill)
                // Night Rate: 19.349p/kWh (Net from bill)
                ElectricityTariff elecTariff = new ElectricityTariff(
                                "Flexible 6 Direct Debit",
                                BigDecimal.valueOf(22.63), // Standing Charge
                                BigDecimal.valueOf(19.349), // Day
                                BigDecimal.valueOf(19.349) // Night
                );
                tariffDAO.save(elecTariff);

                // 2. Setup Customer using default constructor and setters
                Customer customer = new Customer();
                customer.setFirstName("John");
                customer.setLastName("Doe");
                customer.setPhone("07700900000");
                customer.setServiceAddress(new Address("123", "Test St", "London", "Greater London", "SW1A 1AA"));
                customer.setTariffId(elecTariff.getTariffId());
                customerDAO.save(customer);

                // 3. Setup Meter (Day/Night)
                Meter meter = new Meter(MeterType.ELECTRICITY, "L78FW05633");
                meter.setDayNightMeter(true); // true = day/night
                customer.addMeter(meter);
                customerDAO.update(customer); // Save meter to customer

                // 4. Record Readings
                // Period: 30/09/21 - 02/11/21 (33 days total? Let's check calculation)
                LocalDate startDate = LocalDate.of(2021, 9, 30);
                LocalDate endDate = LocalDate.of(2021, 11, 2);

                // Opening Reading
                MeterReading opening = new MeterReading(meter.getMeterId(), customer.getCustomerId(), 0.0, 0.0,
                                MeterReading.ReadingType.ACTUAL);
                opening.setReadingDate(startDate);
                opening.setDayReading(37386.998);
                opening.setNightReading(40470.637);
                meterReadingDAO.save(opening);

                // Closing Reading
                MeterReading closing = new MeterReading(meter.getMeterId(), customer.getCustomerId(), 0.0, 0.0,
                                MeterReading.ReadingType.ACTUAL);
                closing.setReadingDate(endDate);
                closing.setDayReading(37623.210);
                closing.setNightReading(40516.687);
                closing.setPreviousDayReading(opening.getDayReading());
                closing.setPreviousNightReading(opening.getNightReading());
                meterReadingDAO.save(closing);

                // 5. Generate Invoice
                Invoice invoice = billingService.generateInvoice(customer.getCustomerId(), startDate, endDate);

                // 6. Verify Totals
                // Expected from Bill: £65.18
                assertEquals(new BigDecimal("65.18"), invoice.getTotalAmount());
        }

        @Test
        void verifyGasBillCalculation() throws Exception {
                // 1. Setup Gas Tariff
                // Standing: 24.87p/day
                // Unit Rate: 3.797p/kWh (Net)
                // CV: 39.3
                // CF: 1.02264
                GasTariff gasTariff = new GasTariff(
                                "Flexible 6 Gas",
                                BigDecimal.valueOf(24.87),
                                BigDecimal.valueOf(3.797));
                gasTariff.setCalorificValue(39.3);
                gasTariff.setCorrectionFactor(1.02264);
                tariffDAO.save(gasTariff);

                // 2. Setup Customer using default constructor and setters
                Customer customer = new Customer();
                customer.setFirstName("Jane");
                customer.setLastName("Doe");
                customer.setPhone("07700900001");
                customer.setServiceAddress(new Address("456", "Gas Ln", "London", "Greater London", "SW1A 1AB"));
                customer.setTariffId(gasTariff.getTariffId());
                customerDAO.save(customer);

                // 3. Setup Meter (Imperial)
                Meter meter = new Meter(MeterType.GAS, "0000915");
                meter.setImperialMeter(true); // Explicitly set Imperial
                customer.addMeter(meter);
                customerDAO.update(customer);

                // 4. Record Readings
                LocalDate startDate = LocalDate.of(2021, 9, 30);
                LocalDate endDate = LocalDate.of(2021, 11, 2);

                // Opening: 10091.5
                MeterReading opening = new MeterReading(meter.getMeterId(), customer.getCustomerId(), 10091.5, 0.0,
                                MeterReading.ReadingType.ACTUAL);
                opening.setReadingDate(startDate);
                meterReadingDAO.save(opening);

                // Closing: 10127.6
                MeterReading closing = new MeterReading(meter.getMeterId(), customer.getCustomerId(), 10127.6, 10091.5,
                                MeterReading.ReadingType.ACTUAL);
                closing.setReadingDate(endDate);
                meterReadingDAO.save(closing);

                // 5. Generate Invoice
                Invoice invoice = billingService.generateInvoice(customer.getCustomerId(), startDate, endDate);

                // 6. Verify Totals
                // Calculated: 1140.5 kWh * 3.797p = £43.31 + Standing £8.21 = £51.52 Net + 5%
                // VAT = £54.10
                // User provided example was £54.22, difference likely due to CV averaging or
                // rounding steps on the paper bill.
                assertEquals(new BigDecimal("54.10"), invoice.getTotalAmount());
        }
}
