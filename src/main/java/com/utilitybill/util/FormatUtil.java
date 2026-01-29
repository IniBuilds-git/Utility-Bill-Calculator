package com.utilitybill.util;

import java.math.BigDecimal;

public class FormatUtil {
    
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "£0.00";
        return String.format("£%.2f", amount);
    }
    
    public static String formatCurrency(double amount) {
        return String.format("£%.2f", amount);
    }
    
    public static String formatReading(double reading) {
        return String.format("%.2f", reading);
    }
    
    private FormatUtil() {}
}
