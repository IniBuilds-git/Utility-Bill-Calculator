package com.utilitybill.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations.
 * Provides methods for date formatting, parsing, and calculations.
 *
 * <p>All methods are static and the class cannot be instantiated.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public final class DateUtil {

    /** Standard date format for display (dd/MM/yyyy) */
    public static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Standard date format for display with time */
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** ISO date format for storage */
    public static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /** Long date format for invoices */
    public static final DateTimeFormatter LONG_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");

    /** Short month year format */
    public static final DateTimeFormatter MONTH_YEAR_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

    /**
     * Private constructor to prevent instantiation.
     */
    private DateUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Formats a LocalDate for display.
     *
     * @param date the date to format
     * @return the formatted date string, or empty string if null
     */
    public static String formatForDisplay(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DISPLAY_DATE_FORMAT);
    }

    /**
     * Formats a LocalDateTime for display.
     *
     * @param dateTime the date/time to format
     * @return the formatted date/time string, or empty string if null
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DISPLAY_DATETIME_FORMAT);
    }

    /**
     * Formats a date in long format (for invoices).
     *
     * @param date the date to format
     * @return the formatted date string
     */
    public static String formatLong(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(LONG_DATE_FORMAT);
    }

    /**
     * Formats a date as month and year.
     *
     * @param date the date to format
     * @return the formatted string (e.g., "January 2024")
     */
    public static String formatMonthYear(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(MONTH_YEAR_FORMAT);
    }

    /**
     * Parses a date string in display format.
     *
     * @param dateString the date string to parse
     * @return the parsed LocalDate, or null if parsing fails
     */
    public static LocalDate parseDisplayDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim(), DISPLAY_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a date string in ISO format.
     *
     * @param dateString the date string to parse
     * @return the parsed LocalDate, or null if parsing fails
     */
    public static LocalDate parseIsoDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim(), ISO_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Calculates the number of days between two dates.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return the number of days between the dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculates the number of months between two dates.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return the number of months between the dates
     */
    public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    /**
     * Gets the first day of the current month.
     *
     * @return the first day of the current month
     */
    public static LocalDate getFirstDayOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * Gets the last day of the current month.
     *
     * @return the last day of the current month
     */
    public static LocalDate getLastDayOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(now.lengthOfMonth());
    }

    /**
     * Gets the first day of the previous month.
     *
     * @return the first day of the previous month
     */
    public static LocalDate getFirstDayOfPreviousMonth() {
        return LocalDate.now().minusMonths(1).withDayOfMonth(1);
    }

    /**
     * Gets the last day of the previous month.
     *
     * @return the last day of the previous month
     */
    public static LocalDate getLastDayOfPreviousMonth() {
        return LocalDate.now().withDayOfMonth(1).minusDays(1);
    }

    /**
     * Gets the billing period description.
     *
     * @param startDate the billing period start date
     * @param endDate   the billing period end date
     * @return a human-readable billing period string
     */
    public static String getBillingPeriodDescription(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "";
        }
        return String.format("%s to %s (%d days)",
                formatForDisplay(startDate),
                formatForDisplay(endDate),
                daysBetween(startDate, endDate) + 1);
    }

    /**
     * Checks if a date is in the past.
     *
     * @param date the date to check
     * @return true if the date is before today
     */
    public static boolean isInPast(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }

    /**
     * Checks if a date is in the future.
     *
     * @param date the date to check
     * @return true if the date is after today
     */
    public static boolean isInFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(LocalDate.now());
    }

    /**
     * Checks if a date is today.
     *
     * @param date the date to check
     * @return true if the date is today
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now());
    }

    /**
     * Gets the default due date (14 days from today).
     *
     * @return the default due date
     */
    public static LocalDate getDefaultDueDate() {
        return LocalDate.now().plusDays(14);
    }

    /**
     * Gets the due date from a given issue date.
     *
     * @param issueDate the invoice issue date
     * @param days      the number of days until due
     * @return the due date
     */
    public static LocalDate getDueDate(LocalDate issueDate, int days) {
        if (issueDate == null) {
            return null;
        }
        return issueDate.plusDays(days);
    }

    /**
     * Gets a friendly relative date description.
     *
     * @param date the date to describe
     * @return a friendly description (e.g., "Today", "Yesterday", "3 days ago")
     */
    public static String getRelativeDateDescription(LocalDate date) {
        if (date == null) {
            return "";
        }

        long days = daysBetween(date, LocalDate.now());

        if (days == 0) {
            return "Today";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days == -1) {
            return "Tomorrow";
        } else if (days > 1 && days <= 7) {
            return days + " days ago";
        } else if (days < -1 && days >= -7) {
            return "In " + (-days) + " days";
        } else {
            return formatForDisplay(date);
        }
    }
}

