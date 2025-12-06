package com.utilitybill.exception;

/**
 * Exception thrown when data persistence operations fail.
 * This exception wraps I/O errors and other storage-related failures
 * that occur during file-based data operations.
 *
 * <p>Error Codes:</p>
 * <ul>
 *   <li>DATA001 - File read error</li>
 *   <li>DATA002 - File write error</li>
 *   <li>DATA003 - File not found</li>
 *   <li>DATA004 - Data corruption detected</li>
 *   <li>DATA005 - Serialization error</li>
 *   <li>DATA006 - Deserialization error</li>
 * </ul>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class DataPersistenceException extends UtilityBillException {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** The file path involved in the error */
    private final String filePath;

    /** The operation that failed */
    private final Operation operation;

    /**
     * Enum representing the type of operation that failed.
     */
    public enum Operation {
        /** Reading data from file */
        READ("DATA001"),
        /** Writing data to file */
        WRITE("DATA002"),
        /** File access attempt */
        FILE_ACCESS("DATA003"),
        /** Data validation */
        VALIDATION("DATA004"),
        /** Object serialization */
        SERIALIZE("DATA005"),
        /** Object deserialization */
        DESERIALIZE("DATA006");

        private final String errorCode;

        Operation(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    /**
     * Constructs a new DataPersistenceException.
     *
     * @param message   the detail message
     * @param filePath  the file path involved
     * @param operation the operation that failed
     */
    public DataPersistenceException(String message, String filePath, Operation operation) {
        super(message, operation.getErrorCode());
        this.filePath = filePath;
        this.operation = operation;
    }

    /**
     * Constructs a new DataPersistenceException with cause.
     *
     * @param message   the detail message
     * @param filePath  the file path involved
     * @param operation the operation that failed
     * @param cause     the underlying cause
     */
    public DataPersistenceException(String message, String filePath, Operation operation, Throwable cause) {
        super(message, operation.getErrorCode(), cause);
        this.filePath = filePath;
        this.operation = operation;
    }

    /**
     * Factory method for read errors.
     *
     * @param filePath the file that could not be read
     * @param cause    the underlying cause
     * @return a new DataPersistenceException
     */
    public static DataPersistenceException readError(String filePath, Throwable cause) {
        return new DataPersistenceException(
                "Failed to read data from file: " + filePath,
                filePath, Operation.READ, cause);
    }

    /**
     * Factory method for write errors.
     *
     * @param filePath the file that could not be written
     * @param cause    the underlying cause
     * @return a new DataPersistenceException
     */
    public static DataPersistenceException writeError(String filePath, Throwable cause) {
        return new DataPersistenceException(
                "Failed to write data to file: " + filePath,
                filePath, Operation.WRITE, cause);
    }

    /**
     * Factory method for file not found errors.
     *
     * @param filePath the file that was not found
     * @return a new DataPersistenceException
     */
    public static DataPersistenceException fileNotFound(String filePath) {
        return new DataPersistenceException(
                "Data file not found: " + filePath,
                filePath, Operation.FILE_ACCESS);
    }

    /**
     * Factory method for deserialization errors.
     *
     * @param filePath the file with corrupted data
     * @param cause    the underlying cause
     * @return a new DataPersistenceException
     */
    public static DataPersistenceException deserializationError(String filePath, Throwable cause) {
        return new DataPersistenceException(
                "Failed to deserialize data from file: " + filePath,
                filePath, Operation.DESERIALIZE, cause);
    }

    /**
     * Gets the file path involved in the error.
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Gets the operation that failed.
     *
     * @return the operation
     */
    public Operation getOperation() {
        return operation;
    }
}

