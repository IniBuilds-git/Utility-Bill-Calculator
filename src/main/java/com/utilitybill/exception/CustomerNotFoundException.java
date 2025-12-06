package com.utilitybill.exception;

/**
 * Exception thrown when a customer cannot be found in the system.
 * This exception is raised during customer lookup operations when the
 * specified customer ID or account number does not exist.
 *
 * <p>Error Codes:</p>
 * <ul>
 *   <li>CUST001 - Customer not found by ID</li>
 *   <li>CUST002 - Customer not found by account number</li>
 *   <li>CUST003 - Customer not found by email</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class CustomerNotFoundException extends UtilityBillException {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** The identifier used to search for the customer */
    private final String searchIdentifier;

    /** The type of identifier used (ID, ACCOUNT_NUMBER, EMAIL) */
    private final SearchType searchType;

    /**
     * Enum representing the type of search identifier used.
     */
    public enum SearchType {
        /** Search by unique customer ID */
        ID("CUST001"),
        /** Search by account number */
        ACCOUNT_NUMBER("CUST002"),
        /** Search by email address */
        EMAIL("CUST003");

        private final String errorCode;

        SearchType(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    /**
     * Constructs a new CustomerNotFoundException for a customer ID search.
     *
     * @param customerId the customer ID that was not found
     */
    public CustomerNotFoundException(String customerId) {
        super("Customer not found with ID: " + customerId, "CUST001");
        this.searchIdentifier = customerId;
        this.searchType = SearchType.ID;
    }

    /**
     * Constructs a new CustomerNotFoundException with specified search type.
     *
     * @param searchIdentifier the identifier used in the search
     * @param searchType       the type of search performed
     */
    public CustomerNotFoundException(String searchIdentifier, SearchType searchType) {
        super(String.format("Customer not found with %s: %s",
                searchType.name().toLowerCase().replace("_", " "), searchIdentifier),
                searchType.getErrorCode());
        this.searchIdentifier = searchIdentifier;
        this.searchType = searchType;
    }

    /**
     * Gets the search identifier that was used.
     *
     * @return the search identifier
     */
    public String getSearchIdentifier() {
        return searchIdentifier;
    }

    /**
     * Gets the type of search that was performed.
     *
     * @return the search type
     */
    public SearchType getSearchType() {
        return searchType;
    }
}

