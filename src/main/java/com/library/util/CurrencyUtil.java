package com.library.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Tiện ích định dạng tiền tệ VNĐ
 */
public final class CurrencyUtil {

    private static final DecimalFormat VND_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        VND_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    private CurrencyUtil() {}

    /**
     * Định dạng số tiền VNĐ: 1.250.000 ₫
     */
    public static String format(double amount) {
        return VND_FORMAT.format(amount) + " ₫";
    }

    /**
     * Định dạng số tiền không có đơn vị
     */
    public static String formatNumber(double amount) {
        return VND_FORMAT.format(amount);
    }

    /**
     * Parse chuỗi tiền về double
     */
    public static double parse(String text) {
        try {
            String cleaned = text.replaceAll("[^0-9,]", "").replace(",", ".");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
