package com.utilitybill.exception;

/**
 * Exception thrown when attempting to create a duplicate account.
 * This exception is raised when trying to register a customer with
 * an email or account number that already exists in the system.
 *
 * <p>Error Codes:</p>
 * <ul>
 *   <li>DUP001 - Duplicate email address</li>
 *   <li>DUP002 - Duplicate account number</li>
 *   <li>DUP003 - Duplicate meter ID</li>
 *   <li>DUP004 - Duplicate phone number</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class DuplicateAccountException extends UtilityBillException {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** The duplicate field type */
    private final DuplicateType duplicateType;

    /** The duplicate value */
    private final String duplicateValue;

    /**
     * Enum representing the type of duplicate detected.
     */
    public enum DuplicateType {
        /** Duplicate email address */
        EMAIL("DUP001", "email address"),
        /** Duplicate account number */
        ACCOUNT_NUMBER("DUP002", "account number"),
        /** Duplicate meter ID */
        METER_ID("DUP003", "meter ID"),
        /** Duplicate phone number */
        PHONE_NUMBER("DUP004", "phone number");

        private final String errorCode;
        private final String displayName;

        DuplicateType(String errorCode, String displayName) {
            this.errorCode = errorCode;
            this.displayName = displayName;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Constructs a new DuplicateAccountException with specified duplicate type.
     *
     * @param duplicateType  the type of duplicate detected
     * @param duplicateValue the value that is duplicated
     */
    public DuplicateAccountException(DuplicateType duplicateType, String duplicateValue) {
        super(String.format("An account with this %s already exists: %s",
                duplicateType.getDisplayName(), duplicateValue),
                duplicateType.getErrorCode());
        this.duplicateType = duplicateType;
        this.duplicateValue = duplicateValue;
    }

    /**
     * Factory method for duplicate email exception.
     *
     * @param email the duplicate email address
     * @return a new DuplicateAccountException
     */
    public static DuplicateAccountException duplicateEmail(String email) {
        return new DuplicateAccountException(DuplicateType.EMAIL, email);
    }

    /**
     * Factory method for duplicate account number exception.
     *
     * @param accountNumber the duplicate account number
     * @return a new DuplicateAccountException
     */
    public static DuplicateAccountException duplicateAccountNumber(String accountNumber) {
        return new DuplicateAccountException(DuplicateType.ACCOUNT_NUMBER, accountNumber);
    }

    /**
     * Factory method for duplicate meter ID exception.
     *
     * @param meterId the duplicate meter ID
     * @return a new DuplicateAccountException
     */
    public static DuplicateAccountException duplicateMeterId(String meterId) {
        return new DuplicateAccountException(DuplicateType.METER_ID, meterId);
    }

    /**
     * Gets the type of duplicate.
     *
     * @return the duplicate type
     */
    public DuplicateType getDuplicateType() {
        return duplicateType;
    }

    /**
     * Gets the duplicate value.
     *
     * @return the duplicate value
     */
    public String getDuplicateValue() {
        return duplicateValue;
    }
}

