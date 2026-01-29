package com.utilitybill.exception;

/**
 * Exception thrown when a notification (email, SMS, etc.) fails to send.
 */
public class NotificationException extends UtilityBillException {

    private static final long serialVersionUID = 1L;

    public NotificationException(String message) {
        super(message, "NOTIF_ERROR");
    }

    public NotificationException(String message, Throwable cause) {
        super(message, "NOTIF_ERROR", cause);
    }
    
    public NotificationException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
