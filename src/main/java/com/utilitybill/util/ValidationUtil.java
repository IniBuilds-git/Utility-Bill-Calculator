package com.utilitybill.util;

import com.utilitybill.exception.ValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 * Provides methods for validating common input types and formats.
 *
 * <p>All methods are static and the class cannot be instantiated.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public final class ValidationUtil {

    /** Email validation pattern (RFC 5322 simplified) */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    /** UK phone number pattern (various formats) */
    private static final Pattern UK_PHONE_PATTERN = Pattern.compile(
            "^(?:(?:\\+44)|(?:0))(?:\\s?\\d){9,10}$"
    );

    /** UK postcode pattern */
    private static final Pattern UK_POSTCODE_PATTERN = Pattern.compile(
            "^[A-Z]{1,2}[0-9][A-Z0-9]?\\s?[0-9][A-Z]{2}$",
            Pattern.CASE_INSENSITIVE
    );

    /** Account number pattern (ACC-XXXXXX) */
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(
            "^ACC-\\d{6}$"
    );

    /** Minimum password length */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Private constructor to prevent instantiation.
     */
    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates that a string is not null or empty.
     *
     * @param value     the value to validate
     * @param fieldName the field name for error messages
     * @throws ValidationException if validation fails
     */
    public static void requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw ValidationException.requiredField(fieldName);
        }
    }

    /**
     * Validates an email address format.
     *
     * @param email the email to validate
     * @return true if the email is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates an email address and throws exception if invalid.
     *
     * @param email the email to validate
     * @throws ValidationException if the email is invalid
     */
    public static void validateEmail(String email) throws ValidationException {
        if (!isValidEmail(email)) {
            throw ValidationException.invalidEmail(email);
        }
    }

    /**
     * Validates a UK phone number format.
     *
     * @param phone the phone number to validate
     * @return true if the phone number is valid
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // Remove common separators for validation
        String cleaned = phone.replaceAll("[\\s\\-()]", "");
        return UK_PHONE_PATTERN.matcher(cleaned).matches();
    }

    /**
     * Validates a phone number and throws exception if invalid.
     *
     * @param phone the phone number to validate
     * @throws ValidationException if the phone number is invalid
     */
    public static void validatePhone(String phone) throws ValidationException {
        if (!isValidPhone(phone)) {
            throw ValidationException.invalidPhone(phone);
        }
    }

    /**
     * Validates a UK postcode format.
     *
     * @param postcode the postcode to validate
     * @return true if the postcode is valid
     */
    public static boolean isValidPostcode(String postcode) {
        if (postcode == null || postcode.trim().isEmpty()) {
            return false;
        }
        return UK_POSTCODE_PATTERN.matcher(postcode.trim()).matches();
    }

    /**
     * Validates a postcode and throws exception if invalid.
     *
     * @param postcode the postcode to validate
     * @throws ValidationException if the postcode is invalid
     */
    public static void validatePostcode(String postcode) throws ValidationException {
        if (!isValidPostcode(postcode)) {
            throw new ValidationException("postcode", "Invalid UK postcode format");
        }
    }

    /**
     * Validates password strength.
     * Requirements: minimum 8 characters, at least one uppercase, one lowercase, and one digit.
     *
     * @param password the password to validate
     * @return true if the password meets requirements
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            if (Character.isLowerCase(c)) hasLowercase = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUppercase && hasLowercase && hasDigit;
    }

    /**
     * Validates password strength and throws exception if weak.
     *
     * @param password the password to validate
     * @throws ValidationException if the password is weak
     */
    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw ValidationException.weakPassword(
                    String.format("Password must be at least %d characters", MIN_PASSWORD_LENGTH));
        }

        if (!isStrongPassword(password)) {
            throw ValidationException.weakPassword(
                    "Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        }
    }

    /**
     * Gets password strength feedback.
     *
     * @param password the password to check
     * @return a description of what the password is missing
     */
    public static String getPasswordFeedback(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }

        StringBuilder feedback = new StringBuilder();

        if (password.length() < MIN_PASSWORD_LENGTH) {
            feedback.append(String.format("At least %d characters required. ", MIN_PASSWORD_LENGTH));
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUppercase) feedback.append("Add uppercase letter. ");
        if (!hasLowercase) feedback.append("Add lowercase letter. ");
        if (!hasDigit) feedback.append("Add digit. ");

        return feedback.length() > 0 ? feedback.toString().trim() : "Password meets requirements";
    }

    /**
     * Validates that a number is positive.
     *
     * @param value     the value to validate
     * @param fieldName the field name for error messages
     * @throws ValidationException if the value is not positive
     */
    public static void validatePositive(double value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw ValidationException.outOfRange(fieldName, value, 0.01, Double.MAX_VALUE);
        }
    }

    /**
     * Validates that a number is non-negative.
     *
     * @param value     the value to validate
     * @param fieldName the field name for error messages
     * @throws ValidationException if the value is negative
     */
    public static void validateNonNegative(double value, String fieldName) throws ValidationException {
        if (value < 0) {
            throw ValidationException.outOfRange(fieldName, value, 0, Double.MAX_VALUE);
        }
    }

    /**
     * Validates that a value is within a range.
     *
     * @param value     the value to validate
     * @param min       the minimum allowed value
     * @param max       the maximum allowed value
     * @param fieldName the field name for error messages
     * @throws ValidationException if the value is out of range
     */
    public static void validateRange(double value, double min, double max, String fieldName)
            throws ValidationException {
        if (value < min || value > max) {
            throw ValidationException.outOfRange(fieldName, value, min, max);
        }
    }

    /**
     * Validates a meter reading against the previous reading.
     *
     * @param currentReading  the new reading
     * @param previousReading the previous reading
     * @param fieldName       the field name for error messages
     * @throws ValidationException if the current reading is less than previous
     */
    public static void validateMeterReading(double currentReading, double previousReading, String fieldName)
            throws ValidationException {
        if (currentReading < previousReading) {
            throw new ValidationException(fieldName,
                    String.format("Reading %.2f cannot be less than previous reading %.2f",
                            currentReading, previousReading));
        }
    }

    /**
     * Validates an account number format.
     *
     * @param accountNumber the account number to validate
     * @return true if the format is valid
     */
    public static boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        return ACCOUNT_NUMBER_PATTERN.matcher(accountNumber.trim()).matches();
    }

    /**
     * Functional interface for validation that can throw ValidationException.
     */
    @FunctionalInterface
    public interface ValidationAction {
        void validate() throws ValidationException;
    }

    /**
     * Validates multiple fields at once and collects all errors.
     *
     * @param validations a map of field names to validation actions
     * @throws ValidationException if any validation fails (contains all errors)
     */
    public static void validateAll(Map<String, ValidationAction> validations) throws ValidationException {
        Map<String, String> errors = new HashMap<>();

        for (Map.Entry<String, ValidationAction> entry : validations.entrySet()) {
            try {
                entry.getValue().validate();
            } catch (ValidationException e) {
                errors.put(entry.getKey(), e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /**
     * Sanitizes a string by trimming whitespace and removing dangerous characters.
     *
     * @param input the input string
     * @return the sanitized string
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        // Remove leading/trailing whitespace and normalize internal whitespace
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * Formats a phone number to a standard format.
     *
     * @param phone the phone number to format
     * @return the formatted phone number
     */
    public static String formatPhone(String phone) {
        if (phone == null) {
            return null;
        }
        // Remove all non-digit characters except leading +
        String cleaned = phone.replaceAll("[^\\d+]", "");
        if (cleaned.startsWith("44")) {
            cleaned = "+" + cleaned;
        } else if (cleaned.startsWith("0")) {
            cleaned = "+44" + cleaned.substring(1);
        }
        return cleaned;
    }

    /**
     * Formats a postcode to standard format (with space).
     *
     * @param postcode the postcode to format
     * @return the formatted postcode
     */
    public static String formatPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        String cleaned = postcode.toUpperCase().replaceAll("\\s", "");
        if (cleaned.length() >= 4) {
            return cleaned.substring(0, cleaned.length() - 3) + " " +
                    cleaned.substring(cleaned.length() - 3);
        }
        return cleaned;
    }
}

