package com.utilitybill.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FormatUtil Tests")
class FormatUtilTest {

    @Test
    @DisplayName("Should format BigDecimal currency correctly")
    void shouldFormatBigDecimalCurrency() {
        assertEquals("£10.50", FormatUtil.formatCurrency(new BigDecimal("10.50")));
        assertEquals("£0.00", FormatUtil.formatCurrency(BigDecimal.ZERO));
        assertEquals("£1234.56", FormatUtil.formatCurrency(new BigDecimal("1234.56")));
    }

    @Test
    @DisplayName("Should handle null BigDecimal in formatCurrency")
    void shouldHandleNullBigDecimal() {
        assertEquals("£0.00", FormatUtil.formatCurrency((BigDecimal) null));
    }

    @Test
    @DisplayName("Should format double currency correctly")
    void shouldFormatDoubleCurrency() {
        assertEquals("£10.50", FormatUtil.formatCurrency(10.5));
        assertEquals("£0.00", FormatUtil.formatCurrency(0.0));
        assertEquals("£10.00", FormatUtil.formatCurrency(10));
    }

    @Test
    @DisplayName("Should format meter reading correctly")
    void shouldFormatMeterReading() {
        assertEquals("100.00", FormatUtil.formatReading(100));
        assertEquals("100.50", FormatUtil.formatReading(100.5));
        assertEquals("0.00", FormatUtil.formatReading(0));
    }
}
