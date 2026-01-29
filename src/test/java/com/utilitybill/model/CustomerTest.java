package com.utilitybill.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Customer model class.
 * Tests customer creation, meter management, and account operations.
 */
@DisplayName("Customer Model Tests")
class CustomerTest {

    private Customer customer;
    private Address address;

    @BeforeEach
    void setUp() {
        address = new Address("1", "Test Street", "Test City", "Test County", "TS1 1AA");
        customer = new Customer("John", "Doe", "john.doe@example.com", "07123456789", address);
    }

    @Nested
    @DisplayName("Customer Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create customer with valid details")
        void shouldCreateCustomer() {
            assertNotNull(customer.getCustomerId());
            assertNotNull(customer.getAccountNumber());
            assertEquals("John", customer.getFirstName());
            assertEquals("Doe", customer.getLastName());
            assertEquals("john.doe@example.com", customer.getEmail());
            assertEquals("07123456789", customer.getPhone());
        }

        @Test
        @DisplayName("Should generate unique account numbers")
        void shouldGenerateUniqueAccountNumbers() {
            Customer customer2 = new Customer("Jane", "Smith", "jane@example.com", "07987654321", address);
            assertNotEquals(customer.getAccountNumber(), customer2.getAccountNumber());
        }

        @Test
        @DisplayName("Should return full name correctly")
        void shouldReturnFullName() {
            assertEquals("John Doe", customer.getFullName());
        }

        @Test
        @DisplayName("Should initialize with active status")
        void shouldBeActiveByDefault() {
            assertTrue(customer.isActive());
        }


        @Test
        @DisplayName("Should initialize with zero account balance")
        void shouldHaveZeroBalanceByDefault() {
            assertEquals(BigDecimal.ZERO, customer.getAccountBalance());
        }
    }

    @Nested
    @DisplayName("Meter Management Tests")
    class MeterTests {

        @Test
        @DisplayName("Should add meter successfully")
        void shouldAddMeter() {
            Meter meter = Meter.createElectricityMeter("SN123456");
            customer.addMeter(meter);

            assertEquals(1, customer.getMeters().size());
            assertTrue(customer.hasElectricityMeter());
        }

        @Test
        @DisplayName("Should not add duplicate meter")
        void shouldNotAddDuplicateMeter() {
            Meter meter = Meter.createElectricityMeter("SN123456");
            customer.addMeter(meter);
            customer.addMeter(meter); // Duplicate

            assertEquals(1, customer.getMeters().size());
        }

        @Test
        @DisplayName("Should remove meter successfully")
        void shouldRemoveMeter() {
            Meter meter = Meter.createElectricityMeter("SN123456");
            customer.addMeter(meter);
            assertTrue(customer.removeMeter(meter));
            assertEquals(0, customer.getMeters().size());
        }

        @Test
        @DisplayName("Should find meter by ID")
        void shouldFindMeterById() {
            Meter meter = Meter.createGasMeter("SN123456");
            customer.addMeter(meter);

            Meter found = customer.getMeterById(meter.getMeterId());
            assertNotNull(found);
            assertEquals(meter.getMeterId(), found.getMeterId());
        }

        @Test
        @DisplayName("Should filter meters by type")
        void shouldFilterMetersByType() {
            customer.addMeter(Meter.createElectricityMeter("SN001"));
            customer.addMeter(Meter.createGasMeter("SN002"));
            customer.addMeter(Meter.createElectricityMeter("SN003"));

            assertEquals(2, customer.getMetersByType(MeterType.ELECTRICITY).size());
            assertEquals(1, customer.getMetersByType(MeterType.GAS).size());
        }

        @Test
        @DisplayName("Should check for electricity meter correctly")
        void shouldCheckForElectricityMeter() {
            assertFalse(customer.hasElectricityMeter());
            customer.addMeter(Meter.createElectricityMeter("SN001"));
            assertTrue(customer.hasElectricityMeter());
        }

        @Test
        @DisplayName("Should check for gas meter correctly")
        void shouldCheckForGasMeter() {
            assertFalse(customer.hasGasMeter());
            customer.addMeter(Meter.createGasMeter("SN001"));
            assertTrue(customer.hasGasMeter());
        }
    }

    @Nested
    @DisplayName("Account Balance Tests")
    class AccountBalanceTests {

        @Test
        @DisplayName("Should credit account successfully")
        void shouldCreditAccount() {
            customer.creditAccount(new BigDecimal("100.00"));
            assertEquals(new BigDecimal("100.00"), customer.getAccountBalance());
        }

        @Test
        @DisplayName("Should debit account successfully")
        void shouldDebitAccount() {
            customer.debitAccount(new BigDecimal("50.00"));
            assertEquals(new BigDecimal("-50.00"), customer.getAccountBalance());
        }

        @Test
        @DisplayName("Should detect debt correctly")
        void shouldDetectDebt() {
            assertFalse(customer.hasDebt());
            customer.debitAccount(new BigDecimal("100.00"));
            assertTrue(customer.hasDebt());
        }

        @Test
        @DisplayName("Should return correct debt amount")
        void shouldReturnDebtAmount() {
            assertEquals(BigDecimal.ZERO, customer.getDebtAmount());
            customer.debitAccount(new BigDecimal("75.50"));
            assertEquals(new BigDecimal("75.50"), customer.getDebtAmount());
        }

        @Test
        @DisplayName("Should not credit negative amount")
        void shouldNotCreditNegativeAmount() {
            customer.creditAccount(new BigDecimal("-50.00"));
            assertEquals(BigDecimal.ZERO, customer.getAccountBalance());
        }

        @Test
        @DisplayName("Should not debit negative amount")
        void shouldNotDebitNegativeAmount() {
            customer.debitAccount(new BigDecimal("-50.00"));
            assertEquals(BigDecimal.ZERO, customer.getAccountBalance());
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Customers with same ID should be equal")
        void customersWithSameIdShouldBeEqual() {
            Customer customer2 = new Customer();
            customer2.setCustomerId(customer.getCustomerId());

            assertEquals(customer, customer2);
            assertEquals(customer.hashCode(), customer2.hashCode());
        }

        @Test
        @DisplayName("Customers with different IDs should not be equal")
        void customersWithDifferentIdsShouldNotBeEqual() {
            Customer customer2 = new Customer("John", "Doe", "john.doe@example.com", "07123456789", address);
            assertNotEquals(customer, customer2);
        }
    }
}
