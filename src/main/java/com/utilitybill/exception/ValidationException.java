package com.utilitybill.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when input validation fails.
 * This exception can hold multiple validation errors for batch validation scenarios.
 *
 * <p>Error Codes:</p>
 * <ul>
 *   <li>VAL001 - Required field missing</li>
 *   <li>VAL002 - Invalid format</li>
 *   <li>VAL003 - Value out of range</li>
 *   <li>VAL004 - Invalid email format</li>
 *   <li>VAL005 - Invalid phone format</li>
 *   <li>VAL006 - Password too weak</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class ValidationException extends UtilityBillException {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Map of field names to their validation error messages */
    private final Map<String, String> validationErrors;

    /**
     * Constructs a new ValidationException with a single validation error.
     *
     * @param fieldName the name of the invalid field
     * @param message   the validation error message
     */
    public ValidationException(String fieldName, String message) {
        super(String.format("Validation failed for '%s': %s", fieldName, message), "VAL002");
        this.validationErrors = new HashMap<>();
        this.validationErrors.put(fieldName, message);
    }

    /**
     * Constructs a new ValidationException with a specific error code.
     *
     * @param fieldName the name of the invalid field
     * @param message   the validation error message
     * @param errorCode the specific error code
     */
    public ValidationException(String fieldName, String message, String errorCode) {
        super(String.format("Validation failed for '%s': %s", fieldName, message), errorCode);
        this.validationErrors = new HashMap<>();
        this.validationErrors.put(fieldName, message);
    }

    /**
     * Constructs a new ValidationException with multiple validation errors.
     *
     * @param validationErrors map of field names to error messages
     */
    public ValidationException(Map<String, String> validationErrors) {
        super("Multiple validation errors occurred", "VAL002");
        this.validationErrors = new HashMap<>(validationErrors);
    }

    /**
     * Factory method for required field validation.
     *
     * @param fieldName the name of the missing required field
     * @return a new ValidationException
     */
    public static ValidationException requiredField(String fieldName) {
        return new ValidationException(fieldName, "This field is required", "VAL001");
    }

    /**
     * Factory method for invalid email format.
     *
     * @param email the invalid email address
     * @return a new ValidationException
     */
    public static ValidationException invalidEmail(String email) {
        return new ValidationException("email",
                String.format("'%s' is not a valid email address", email), "VAL004");
    }

    /**
     * Factory method for invalid phone format.
     *
     * @param phone the invalid phone number
     * @return a new ValidationException
     */
    public static ValidationException invalidPhone(String phone) {
        return new ValidationException("phone",
                String.format("'%s' is not a valid phone number", phone), "VAL005");
    }

    /**
     * Factory method for weak password.
     *
     * @param reason the reason the password is weak
     * @return a new ValidationException
     */
    public static ValidationException weakPassword(String reason) {
        return new ValidationException("password", reason, "VAL006");
    }

    /**
     * Factory method for value out of range.
     *
     * @param fieldName the field name
     * @param value     the invalid value
     * @param min       the minimum allowed value
     * @param max       the maximum allowed value
     * @return a new ValidationException
     */
    public static ValidationException outOfRange(String fieldName, double value, double min, double max) {
        return new ValidationException(fieldName,
                String.format("Value %.2f is out of range [%.2f - %.2f]", value, min, max), "VAL003");
    }

    /**
     * Gets all validation errors.
     *
     * @return unmodifiable map of validation errors
     */
    public Map<String, String> getValidationErrors() {
        return Collections.unmodifiableMap(validationErrors);
    }

    /**
     * Checks if a specific field has a validation error.
     *
     * @param fieldName the field name to check
     * @return true if the field has a validation error
     */
    public boolean hasErrorForField(String fieldName) {
        return validationErrors.containsKey(fieldName);
    }

    /**
     * Gets the error message for a specific field.
     *
     * @param fieldName the field name
     * @return the error message, or null if no error for this field
     */
    public String getErrorForField(String fieldName) {
        return validationErrors.get(fieldName);
    }

    /**
     * Gets the number of validation errors.
     *
     * @return the error count
     */
    public int getErrorCount() {
        return validationErrors.size();
    }

    /**
     * Adds a validation error to this exception.
     *
     * @param fieldName the field name
     * @param message   the error message
     */
    public void addError(String fieldName, String message) {
        validationErrors.put(fieldName, message);
    }

    @Override
    public String getMessage() {
        if (validationErrors.size() == 1) {
            Map.Entry<String, String> entry = validationErrors.entrySet().iterator().next();
            return String.format("Validation failed for '%s': %s", entry.getKey(), entry.getValue());
        }
        StringBuilder sb = new StringBuilder("Multiple validation errors:\n");
        validationErrors.forEach((field, error) ->
                sb.append(String.format("  - %s: %s%n", field, error)));
        return sb.toString();
    }
}

