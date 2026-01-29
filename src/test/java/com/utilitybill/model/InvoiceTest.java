package com.utilitybill.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Invoice model class.
 * Tests invoice creation, line items, and total calculations.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
@DisplayName("Invoice Model Tests")
class InvoiceTest {

    private Invoice invoice;
    private static final String CUSTOMER_ID = "cust-123";
    private static final String ACCOUNT_NUMBER = "ACC-001234";

    @BeforeEach
    void setUp() {
        invoice = new Invoice(CUSTOMER_ID, ACCOUNT_NUMBER,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    }

    @Nested
    @DisplayName("Invoice Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create invoice with valid details")
        void shouldCreateInvoice() {
            assertNotNull(invoice.getInvoiceId());
            assertNotNull(invoice.getInvoiceNumber());
            assertEquals(CUSTOMER_ID, invoice.getCustomerId());
            assertEquals(ACCOUNT_NUMBER, invoice.getAccountNumber());
        }

        @Test
        @DisplayName("Should generate unique invoice numbers")
        void shouldGenerateUniqueInvoiceNumbers() {
            Invoice invoice2 = new Invoice(CUSTOMER_ID, ACCOUNT_NUMBER,
                    LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 28));
            assertNotEquals(invoice.getInvoiceNumber(), invoice2.getInvoiceNumber());
        }

        @Test
        @DisplayName("Should set period dates correctly")
        void shouldSetPeriodDates() {
            assertEquals(LocalDate.of(2024, 1, 1), invoice.getPeriodStart());
            assertEquals(LocalDate.of(2024, 1, 31), invoice.getPeriodEnd());
        }

        @Test
        @DisplayName("Should initialize with PENDING status")
        void shouldInitializeWithPendingStatus() {
            assertEquals(Invoice.InvoiceStatus.PENDING, invoice.getStatus());
        }

        @Test
        @DisplayName("Should set issue date to today")
        void shouldSetIssueDateToToday() {
            assertEquals(LocalDate.now(), invoice.getIssueDate());
        }
    }

    @Nested
    @DisplayName("Line Item Tests")
    class LineItemTests {

        @Test
        @DisplayName("Should add line item successfully")
        void shouldAddLineItem() {
            Invoice.InvoiceLineItem item = new Invoice.InvoiceLineItem(
                    "Electricity usage", 100.0, "kWh",
                    new BigDecimal("0.2862"), new BigDecimal("28.62"));

            invoice.addLineItem(item);

            assertEquals(1, invoice.getLineItems().size());
            assertEquals("Electricity usage", invoice.getLineItems().get(0).getDescription());
        }

        @Test
        @DisplayName("Should calculate totals correctly")
        void shouldCalculateTotalsCorrectly() {
            // Set unitRate (in pence) and unitsConsumed for calculation
            // Invoice.calculateTotals calculates: unitCost = unitRate * unitsConsumed / 100
            invoice.setUnitRate(new BigDecimal("28.62")); // 28.62p per kWh
            invoice.setUnitsConsumed(100); // 100 kWh -> unitCost = 28.62 * 100 / 100 = £28.62
            // Standing charge
            invoice.setStandingChargeTotal(new BigDecimal("13.50"));
            // VAT rate
            invoice.setVatRate(new BigDecimal("0.05"));

            invoice.calculateTotals();

            // Unit cost: (28.62p * 100) / 100 = £28.62
            assertEquals(new BigDecimal("28.62"), invoice.getUnitCost());
            // Subtotal = 28.62 + 13.50 = 42.12
            assertEquals(new BigDecimal("42.12"), invoice.getSubtotal());
            // VAT = 42.12 * 0.05 = 2.11
            assertEquals(new BigDecimal("2.11"), invoice.getVatAmount());
            // Total = 42.12 + 2.11 = 44.23
            assertEquals(new BigDecimal("44.23"), invoice.getTotalAmount());
        }

        @Test
        @DisplayName("Line item toString should format correctly")
        void lineItemToStringShouldFormat() {
            Invoice.InvoiceLineItem item = new Invoice.InvoiceLineItem(
                    "Gas usage", 500.0, "kWh",
                    new BigDecimal("0.0742"), new BigDecimal("37.10"));

            String str = item.toString();
            assertTrue(str.contains("Gas usage"));
            assertTrue(str.contains("500"));
        }
    }

    @Nested
    @DisplayName("Invoice Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should change status to PAID")
        void shouldChangeStatusToPaid() {
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            assertEquals(Invoice.InvoiceStatus.PAID, invoice.getStatus());
        }

        @Test
        @DisplayName("Should change status to OVERDUE")
        void shouldChangeStatusToOverdue() {
            invoice.setStatus(Invoice.InvoiceStatus.OVERDUE);
            assertEquals(Invoice.InvoiceStatus.OVERDUE, invoice.getStatus());
        }

        @Test
        @DisplayName("Should change status to CANCELLED")
        void shouldChangeStatusToCancelled() {
            invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
            assertEquals(Invoice.InvoiceStatus.CANCELLED, invoice.getStatus());
        }
    }

    @Nested
    @DisplayName("Day/Night Reading Tests")
    class DayNightReadingTests {

        @Test
        @DisplayName("Should store day readings correctly")
        void shouldStoreDayReadings() {
            invoice.setDayOpeningReading(1000.0);
            invoice.setDayClosingReading(1100.0);
            invoice.setDayUnitsConsumed(100.0);

            assertEquals(1000.0, invoice.getDayOpeningReading());
            assertEquals(1100.0, invoice.getDayClosingReading());
            assertEquals(100.0, invoice.getDayUnitsConsumed());
        }

        @Test
        @DisplayName("Should store night readings correctly")
        void shouldStoreNightReadings() {
            invoice.setNightOpeningReading(500.0);
            invoice.setNightClosingReading(550.0);
            invoice.setNightUnitsConsumed(50.0);

            assertEquals(500.0, invoice.getNightOpeningReading());
            assertEquals(550.0, invoice.getNightClosingReading());
            assertEquals(50.0, invoice.getNightUnitsConsumed());
        }
    }

    @Nested
    @DisplayName("Gas Conversion Tests")
    class GasConversionTests {

        @Test
        @DisplayName("Should store gas conversion details")
        void shouldStoreGasConversionDetails() {
            invoice.setMeterUnits(100.0);
            invoice.setCubicMeters(283.0);
            invoice.setCorrectedVolume(289.4);
            invoice.setCalorificValue(39.4);
            invoice.setKwhFromGas(3172.0);
            invoice.setImperialMeter(true);

            assertEquals(100.0, invoice.getMeterUnits());
            assertEquals(283.0, invoice.getCubicMeters());
            assertEquals(289.4, invoice.getCorrectedVolume());
            assertEquals(39.4, invoice.getCalorificValue());
            assertEquals(3172.0, invoice.getKwhFromGas());
            assertTrue(invoice.isImperialMeter());
        }
    }

    @Nested
    @DisplayName("Account Balance Tests")
    class AccountBalanceTests {

        @Test
        @DisplayName("Should store previous and after balance")
        void shouldStoreBalances() {
            invoice.setPreviousBalance(new BigDecimal("50.00"));
            invoice.setAccountBalanceAfter(new BigDecimal("94.23"));

            assertEquals(new BigDecimal("50.00"), invoice.getPreviousBalance());
            assertEquals(new BigDecimal("94.23"), invoice.getAccountBalanceAfter());
        }
    }
}
