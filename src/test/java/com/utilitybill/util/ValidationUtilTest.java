package com.utilitybill.util;

import com.utilitybill.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtil class.
 * Demonstrates white-box testing with JUnit 5.
 
 */
@DisplayName("ValidationUtil Tests")
class ValidationUtilTest {

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "test@example.com",
                "user.name@domain.co.uk",
                "user+tag@example.org",
                "firstname.lastname@company.com"
        })
        @DisplayName("Should accept valid email addresses")
        void shouldAcceptValidEmails(String email) {
            assertTrue(ValidationUtil.isValidEmail(email),
                    "Email should be valid: " + email);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "@nodomain.com",
                "no@",
                "spaces in@email.com",
                "missing@domain"
        })
        @DisplayName("Should reject invalid email addresses")
        void shouldRejectInvalidEmails(String email) {
            assertFalse(ValidationUtil.isValidEmail(email),
                    "Email should be invalid: " + email);
        }

        @Test
        @DisplayName("Should reject null email")
        void shouldRejectNullEmail() {
            assertFalse(ValidationUtil.isValidEmail(null));
        }

        @Test
        @DisplayName("Should reject empty email")
        void shouldRejectEmptyEmail() {
            assertFalse(ValidationUtil.isValidEmail(""));
            assertFalse(ValidationUtil.isValidEmail("   "));
        }

        @Test
        @DisplayName("Should throw ValidationException for invalid email")
        void shouldThrowExceptionForInvalidEmail() {
            assertThrows(ValidationException.class,
                    () -> ValidationUtil.validateEmail("invalid"));
        }
    }

    @Nested
    @DisplayName("Phone Validation Tests")
    class PhoneValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "07123456789",
                "07123 456 789",
                "+447123456789",
                "0207 123 4567"
        })
        @DisplayName("Should accept valid UK phone numbers")
        void shouldAcceptValidPhoneNumbers(String phone) {
            assertTrue(ValidationUtil.isValidPhone(phone),
                    "Phone should be valid: " + phone);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "123",
                "abcdefghij",
                "12345"
        })
        @DisplayName("Should reject invalid phone numbers")
        void shouldRejectInvalidPhoneNumbers(String phone) {
            assertFalse(ValidationUtil.isValidPhone(phone),
                    "Phone should be invalid: " + phone);
        }
    }

    @Nested
    @DisplayName("Password Validation Tests")
    class PasswordValidationTests {

        @Test
        @DisplayName("Should accept strong password")
        void shouldAcceptStrongPassword() {
            assertTrue(ValidationUtil.isStrongPassword("Password123"));
            assertTrue(ValidationUtil.isStrongPassword("MyP@ssw0rd"));
        }

        @Test
        @DisplayName("Should reject weak passwords")
        void shouldRejectWeakPasswords() {
            // Too short
            assertFalse(ValidationUtil.isStrongPassword("Pass1"));

            // No uppercase
            assertFalse(ValidationUtil.isStrongPassword("password123"));

            // No lowercase
            assertFalse(ValidationUtil.isStrongPassword("PASSWORD123"));

            // No digit
            assertFalse(ValidationUtil.isStrongPassword("PasswordABC"));
        }

        @Test
        @DisplayName("Should throw ValidationException for weak password")
        void shouldThrowExceptionForWeakPassword() {
            assertThrows(ValidationException.class,
                    () -> ValidationUtil.validatePassword("weak"));
        }

        @Test
        @DisplayName("Should provide helpful password feedback")
        void shouldProvidePasswordFeedback() {
            String feedback = ValidationUtil.getPasswordFeedback("short");
            assertTrue(feedback.contains("characters"));

            feedback = ValidationUtil.getPasswordFeedback("longenough");
            assertTrue(feedback.contains("uppercase") || feedback.contains("digit"));
        }
    }

    @Nested
    @DisplayName("Postcode Validation Tests")
    class PostcodeValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "SW1A 1AA",
                "EC1A 1BB",
                "W1A 0AX",
                "M1 1AE",
                "B33 8TH",
                "CR2 6XH",
                "DN55 1PT"
        })
        @DisplayName("Should accept valid UK postcodes")
        void shouldAcceptValidPostcodes(String postcode) {
            assertTrue(ValidationUtil.isValidPostcode(postcode),
                    "Postcode should be valid: " + postcode);
        }

        @Test
        @DisplayName("Should accept postcodes without space")
        void shouldAcceptPostcodesWithoutSpace() {
            assertTrue(ValidationUtil.isValidPostcode("SW1A1AA"));
        }

        @Test
        @DisplayName("Should reject invalid postcodes")
        void shouldRejectInvalidPostcodes() {
            assertFalse(ValidationUtil.isValidPostcode("12345"));
            assertFalse(ValidationUtil.isValidPostcode("INVALID"));
        }
    }

    @Nested
    @DisplayName("Required Field Validation Tests")
    class RequiredFieldTests {

        @Test
        @DisplayName("Should throw exception for null field")
        void shouldThrowExceptionForNullField() {
            assertThrows(ValidationException.class,
                    () -> ValidationUtil.requireNonEmpty(null, "testField"));
        }

        @Test
        @DisplayName("Should throw exception for empty field")
        void shouldThrowExceptionForEmptyField() {
            assertThrows(ValidationException.class,
                    () -> ValidationUtil.requireNonEmpty("", "testField"));
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only field")
        void shouldThrowExceptionForWhitespaceField() {
            assertThrows(ValidationException.class,
                    () -> ValidationUtil.requireNonEmpty("   ", "testField"));
        }

        @Test
        @DisplayName("Should not throw exception for valid field")
        void shouldNotThrowExceptionForValidField() {
            assertDoesNotThrow(
                    () -> ValidationUtil.requireNonEmpty("valid", "testField"));
        }
    }

    @Nested
    @DisplayName("Numeric Validation Tests")
    class NumericValidationTests {

        @Test
        @DisplayName("Should validate positive numbers")
        void shouldValidatePositiveNumbers() {
            assertDoesNotThrow(() -> ValidationUtil.validatePositive(1.0, "amount"));
            assertDoesNotThrow(() -> ValidationUtil.validatePositive(0.01, "amount"));

            assertThrows(ValidationException.class,
                    () -> ValidationUtil.validatePositive(0, "amount"));
            assertThrows(ValidationException.class,
                    () -> ValidationUtil.validatePositive(-1, "amount"));
        }

        @Test
        @DisplayName("Should validate non-negative numbers")
        void shouldValidateNonNegativeNumbers() {
            assertDoesNotThrow(() -> ValidationUtil.validateNonNegative(0, "amount"));
            assertDoesNotThrow(() -> ValidationUtil.validateNonNegative(1.0, "amount"));

            assertThrows(ValidationException.class,
                    () -> ValidationUtil.validateNonNegative(-1, "amount"));
        }

        @Test
        @DisplayName("Should validate range")
        void shouldValidateRange() {
            assertDoesNotThrow(() -> ValidationUtil.validateRange(5, 0, 10, "value"));
            assertDoesNotThrow(() -> ValidationUtil.validateRange(0, 0, 10, "value"));
            assertDoesNotThrow(() -> ValidationUtil.validateRange(10, 0, 10, "value"));

            assertThrows(ValidationException.class,
                    () -> ValidationUtil.validateRange(-1, 0, 10, "value"));
            assertThrows(ValidationException.class,
                    () -> ValidationUtil.validateRange(11, 0, 10, "value"));
        }
    }

    @Nested
    @DisplayName("Meter Reading Validation Tests")
    class MeterReadingTests {

        @Test
        @DisplayName("Should validate meter readings")
        void shouldValidateMeterReadings() {
            // Valid: current >= previous
            assertDoesNotThrow(
                    () -> ValidationUtil.validateMeterReading(100, 50, "reading"));
            assertDoesNotThrow(
                    () -> ValidationUtil.validateMeterReading(100, 100, "reading"));

            // Invalid: current < previous
            assertThrows(ValidationException.class,
                    () -> ValidationUtil.validateMeterReading(50, 100, "reading"));
        }
    }

    @Nested
    @DisplayName("Formatting Tests")
    class FormattingTests {

        @Test
        @DisplayName("Should sanitize input")
        void shouldSanitizeInput() {
            assertEquals("hello world", ValidationUtil.sanitize("  hello   world  "));
            assertEquals("test", ValidationUtil.sanitize("test"));
            assertNull(ValidationUtil.sanitize(null));
        }

        @Test
        @DisplayName("Should format postcode correctly")
        void shouldFormatPostcode() {
            assertEquals("SW1A 1AA", ValidationUtil.formatPostcode("sw1a1aa"));
            assertEquals("EC1A 1BB", ValidationUtil.formatPostcode("EC1A1BB"));
            assertEquals("M1 1AE", ValidationUtil.formatPostcode("m11ae"));
        }
    }
}

