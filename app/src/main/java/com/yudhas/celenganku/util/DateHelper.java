package com.yudhas.celenganku.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateHelper {

    private static final Locale LOCALE_ID = Locale.forLanguageTag("id-ID");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", LOCALE_ID);
    private static final SimpleDateFormat DATE_DISPLAY = new SimpleDateFormat("d MMM yyyy", LOCALE_ID);
    private static final SimpleDateFormat DATETIME_DISPLAY = new SimpleDateFormat("d MMM yyyy, HH:mm", LOCALE_ID);

    public static String formatDate(long timestamp) { return DATE_DISPLAY.format(new Date(timestamp)); }
    public static String formatDateTime(long timestamp) { return DATETIME_DISPLAY.format(new Date(timestamp)); }
    public static String formatDateInput(long timestamp) { return DATE_FORMAT.format(new Date(timestamp)); }

    /**
     * Konversi sisa hari menjadi teks yang mudah dibaca:
     * ≥ 365 hari → "X tahun lagi"
     * ≥ 30 hari  → "X bulan lagi"
     * < 30 hari  → "X hari lagi"
     */
    public static String formatSisaWaktu(long daysLeft) {
        if (daysLeft >= 365) {
            long tahun = daysLeft / 365;
            return tahun + " tahun lagi";
        } else if (daysLeft >= 30) {
            long bulan = daysLeft / 30;
            return bulan + " bulan lagi";
        } else {
            return daysLeft + " hari lagi";
        }
    }

    public static long parseDate(String dateStr) {
        try {
            Date date = DATE_FORMAT.parse(dateStr);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (Exception e) { return System.currentTimeMillis(); }
    }

    public static long getDaysRemaining(String deadlineStr) {
        if (deadlineStr == null || deadlineStr.isEmpty()) return -1;
        try {
            Date deadline = DATE_FORMAT.parse(deadlineStr);
            if (deadline == null) return -1;
            long diff = deadline.getTime() - System.currentTimeMillis();
            return TimeUnit.MILLISECONDS.toDays(diff);
        } catch (Exception e) { return -1; }
    }

    /** Delay (ms) hingga HH:mm berikutnya (harian) */
    public static long calcInitialDelay(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        if (!target.after(now)) target.add(Calendar.DAY_OF_MONTH, 1);
        return Math.max(0, target.getTimeInMillis() - now.getTimeInMillis());
    }

    /** Delay (ms) hingga hari tertentu dalam seminggu + jam */
    public static long calcInitialDelayForWeekly(String hariStr, int hour, int minute) {
        int targetDay = getDayOfWeekForHari(hariStr);
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        int currentDay = now.get(Calendar.DAY_OF_WEEK);
        int daysUntil = (targetDay - currentDay + 7) % 7;
        if (daysUntil == 0 && !target.after(now)) daysUntil = 7;
        target.add(Calendar.DAY_OF_MONTH, daysUntil);
        return Math.max(0, target.getTimeInMillis() - now.getTimeInMillis());
    }

    /** Delay (ms) hingga tanggal tertentu dalam bulan + jam */
    public static long calcInitialDelayForMonthly(int dayOfMonth, int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        int maxDay = target.getActualMaximum(Calendar.DAY_OF_MONTH);
        target.set(Calendar.DAY_OF_MONTH, Math.min(dayOfMonth, maxDay));
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        if (!target.after(now)) {
            target.add(Calendar.MONTH, 1);
            maxDay = target.getActualMaximum(Calendar.DAY_OF_MONTH);
            target.set(Calendar.DAY_OF_MONTH, Math.min(dayOfMonth, maxDay));
        }
        return Math.max(0, target.getTimeInMillis() - now.getTimeInMillis());
    }

    private static int getDayOfWeekForHari(String hari) {
        if (hari == null) return Calendar.MONDAY;
        switch (hari) {
            case "Senin":   return Calendar.MONDAY;
            case "Selasa":  return Calendar.TUESDAY;
            case "Rabu":    return Calendar.WEDNESDAY;
            case "Kamis":   return Calendar.THURSDAY;
            case "Jumat":   return Calendar.FRIDAY;
            case "Sabtu":   return Calendar.SATURDAY;
            case "Minggu":  return Calendar.SUNDAY;
            default:        return Calendar.MONDAY;
        }
    }

    public static long getCurrentTimestamp() { return System.currentTimeMillis(); }
}
