package com.utilitybill.util;

import com.utilitybill.exception.ValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern UK_PHONE_PATTERN = Pattern.compile(
            "^(?:(?:\\+44)|(?:0))(?:\\s?\\d){9,10}$"
    );

    private static final Pattern UK_POSTCODE_PATTERN = Pattern.compile(
            "^[A-Z]{1,2}[0-9][A-Z0-9]?\\s?[0-9][A-Z]{2}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(
            "^ACC-\\d{6}$"
    );

    private static final int MIN_PASSWORD_LENGTH = 8;

    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw ValidationException.requiredField(fieldName);
        }
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static void validateEmail(String email) throws ValidationException {
        if (!isValidEmail(email)) {
            throw ValidationException.invalidEmail(email);
        }
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleaned = phone.replaceAll("[\\s\\-()]", "");
        return UK_PHONE_PATTERN.matcher(cleaned).matches();
    }

    public static void validatePhone(String phone) throws ValidationException {
        if (!isValidPhone(phone)) {
            throw ValidationException.invalidPhone(phone);
        }
    }

    public static boolean isValidPostcode(String postcode) {
        if (postcode == null || postcode.trim().isEmpty()) {
            return false;
        }
        return UK_POSTCODE_PATTERN.matcher(postcode.trim()).matches();
    }

    public static void validatePostcode(String postcode) throws ValidationException {
        if (!isValidPostcode(postcode)) {
            throw new ValidationException("postcode", "Invalid UK postcode format");
        }
    }

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

    public static void validatePositive(double value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw ValidationException.outOfRange(fieldName, value, 0.01, Double.MAX_VALUE);
        }
    }

    public static void validateNonNegative(double value, String fieldName) throws ValidationException {
        if (value < 0) {
            throw ValidationException.outOfRange(fieldName, value, 0, Double.MAX_VALUE);
        }
    }

    public static void validateRange(double value, double min, double max, String fieldName)
            throws ValidationException {
        if (value < min || value > max) {
            throw ValidationException.outOfRange(fieldName, value, min, max);
        }
    }

    public static void validateMeterReading(double currentReading, double previousReading, String fieldName)
            throws ValidationException {
        if (currentReading < previousReading) {
            throw new ValidationException(fieldName,
                    String.format("Reading %.2f cannot be less than previous reading %.2f",
                            currentReading, previousReading));
        }
    }

    public static boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        return ACCOUNT_NUMBER_PATTERN.matcher(accountNumber.trim()).matches();
    }

    @FunctionalInterface
    public interface ValidationAction {
        void validate() throws ValidationException;
    }

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

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    public static String formatPhone(String phone) {
        if (phone == null) {
            return null;
        }
        String cleaned = phone.replaceAll("[^\\d+]", "");
        if (cleaned.startsWith("44")) {
            cleaned = "+" + cleaned;
        } else if (cleaned.startsWith("0")) {
            cleaned = "+44" + cleaned.substring(1);
        }
        return cleaned;
    }

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

