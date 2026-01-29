package com.utilitybill.exception;

/**
 * Exception thrown when document generation (PDF, reports) fails.
 * 
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class DocumentGenerationException extends UtilityBillException {

    private static final long serialVersionUID = 1L;

    public DocumentGenerationException(String message) {
        super(message, "DOC_GEN_ERROR");
    }

    public DocumentGenerationException(String message, Throwable cause) {
        super(message, "DOC_GEN_ERROR", cause);
    }

    public DocumentGenerationException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
