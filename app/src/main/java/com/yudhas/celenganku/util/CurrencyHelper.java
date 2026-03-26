package com.yudhas.celenganku.util;

import java.util.Locale;

public class CurrencyHelper {

    private static final Locale LOCALE_ID = Locale.forLanguageTag("id-ID");

    public static String formatRupiah(long amount) {
        // Format manual: Rp 1.000.000 (no ,00 suffix)
        if (amount == 0) return "Rp 0";
        String str = String.valueOf(Math.abs(amount));
        StringBuilder sb = new StringBuilder();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) sb.append('.');
            sb.append(str.charAt(i));
        }
        return (amount < 0 ? "-Rp " : "Rp ") + sb.toString();
    }

    public static String formatRupiahShort(long amount) {
        if (amount >= 1_000_000_000) {
            return String.format(LOCALE_ID, "Rp %.1fM", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000) {
            return String.format(LOCALE_ID, "Rp %.1fjt", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return String.format(LOCALE_ID, "Rp %.0frb", amount / 1_000.0);
        } else {
            return "Rp " + amount;
        }
    }

    public static long parseCurrency(String text) {
        if (text == null || text.isEmpty()) return 0;
        try {
            String cleaned = text.replaceAll("[^0-9]", "");
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}




