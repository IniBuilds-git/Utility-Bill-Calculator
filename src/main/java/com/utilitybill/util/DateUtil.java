package com.utilitybill.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public final class DateUtil {

    public static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter LONG_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");
    public static final DateTimeFormatter MONTH_YEAR_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

    private DateUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String formatForDisplay(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DISPLAY_DATE_FORMAT);
    }

    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DISPLAY_DATETIME_FORMAT);
    }

    public static String formatLong(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(LONG_DATE_FORMAT);
    }

    public static String formatMonthYear(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(MONTH_YEAR_FORMAT);
    }

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

    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    public static LocalDate getFirstDayOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    public static LocalDate getLastDayOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(now.lengthOfMonth());
    }

    public static LocalDate getFirstDayOfPreviousMonth() {
        return LocalDate.now().minusMonths(1).withDayOfMonth(1);
    }

    public static LocalDate getLastDayOfPreviousMonth() {
        return LocalDate.now().withDayOfMonth(1).minusDays(1);
    }

    public static String getBillingPeriodDescription(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "";
        }
        return String.format("%s to %s (%d days)",
                formatForDisplay(startDate),
                formatForDisplay(endDate),
                daysBetween(startDate, endDate) + 1);
    }

    public static boolean isInPast(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }

    public static boolean isInFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(LocalDate.now());
    }

    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now());
    }

    public static LocalDate getDefaultDueDate() {
        return LocalDate.now().plusDays(14);
    }

    public static LocalDate getDueDate(LocalDate issueDate, int days) {
        if (issueDate == null) {
            return null;
        }
        return issueDate.plusDays(days);
    }

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

