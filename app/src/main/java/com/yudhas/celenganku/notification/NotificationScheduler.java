package com.yudhas.celenganku.notification;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.yudhas.celenganku.database.entity.NotifikasiTabungan;
import com.yudhas.celenganku.util.DateHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Schedule (or replace) ALL notification entries for a given tabungan.
     * Old schedules for this tabungan are cancelled first.
     */
    public static void scheduleAll(Context context, long tabunganId,
                                   List<NotifikasiTabungan> list) {
        cancelAll(context, tabunganId);
        for (NotifikasiTabungan notif : list) {
            scheduleOne(context, tabunganId, notif);
        }
    }

    /** Schedule a single notification entry. */
    public static void scheduleOne(Context context, long tabunganId,
                                   NotifikasiTabungan notif) {
        String notifType = notif.getNotifType();
        if (notifType == null || notifType.equals("none")) return;

        if ("permenit".equals(notifType)) {
            schedulePerMenitNext(context, tabunganId, notif.getId(),
                    notif.getNotifIntervalMenit());
            return;
        }

        int[] time = parseTime(notif.getNotifTime());
        long initialDelay;
        long repeatInterval;
        TimeUnit repeatUnit;

        switch (notifType) {
            case "perjam": {
                int jam = notif.getNotifIntervalJam();
                if (jam < 1) jam = 1;
                if (jam > 24) jam = 24;
                initialDelay = jam * 60L * 60L * 1000L;
                repeatInterval = jam;
                repeatUnit = TimeUnit.HOURS;
                break;
            }
            case "mingguan": {
                String hari = notif.getNotifHariMinggu();
                initialDelay = DateHelper.calcInitialDelayForWeekly(hari, time[0], time[1]);
                repeatInterval = 7;
                repeatUnit = TimeUnit.DAYS;
                break;
            }
            case "bulanan": {
                int tgl = notif.getNotifTanggalBulanan();
                if (tgl < 1) tgl = 1;
                if (tgl > 31) tgl = 31;
                initialDelay = DateHelper.calcInitialDelayForMonthly(tgl, time[0], time[1]);
                repeatInterval = 30;
                repeatUnit = TimeUnit.DAYS;
                break;
            }
            default: { // harian
                initialDelay = DateHelper.calcInitialDelay(time[0], time[1]);
                repeatInterval = 1;
                repeatUnit = TimeUnit.DAYS;
                break;
            }
        }

        Data inputData = new Data.Builder()
                .putLong(NotificationWorker.KEY_TABUNGAN_ID, tabunganId)
                .putLong(NotificationWorker.KEY_NOTIF_ID, notif.getId())
                .putString(NotificationWorker.KEY_NOTIF_TYPE, notifType)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NotificationWorker.class, repeatInterval, repeatUnit)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(getTabunganTag(tabunganId))
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                getWorkUniqueName(tabunganId, notif.getId()),
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }

    /**
     * Schedule next OneTimeWorkRequest for "permenit" — bypasses WorkManager 15-min minimum.
     * Called both from scheduleOne() and from the worker after each run.
     */
    public static void schedulePerMenitNext(Context context, long tabunganId,
                                            long notifId, int intervalMenit) {
        if (intervalMenit < 1) intervalMenit = 1;
        if (intervalMenit > 59) intervalMenit = 59;

        Data inputData = new Data.Builder()
                .putLong(NotificationWorker.KEY_TABUNGAN_ID, tabunganId)
                .putLong(NotificationWorker.KEY_NOTIF_ID, notifId)
                .putString(NotificationWorker.KEY_NOTIF_TYPE, "permenit")
                .putInt(NotificationWorker.KEY_INTERVAL_MENIT, intervalMenit)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(intervalMenit, TimeUnit.MINUTES)
                .setInputData(inputData)
                .addTag(getTabunganTag(tabunganId))
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                getWorkUniqueName(tabunganId, notifId),
                ExistingWorkPolicy.REPLACE,
                workRequest
        );
    }

    /** Cancel ALL notification schedules for a tabungan (by WorkManager tag). */
    public static void cancelAll(Context context, long tabunganId) {
        WorkManager.getInstance(context).cancelAllWorkByTag(getTabunganTag(tabunganId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Tag used to cancel all work for a tabungan at once. */
    private static String getTabunganTag(long tabunganId) {
        return "tag_tabungan_" + tabunganId;
    }

    /** Unique work name per (tabungan, notif) pair. */
    private static String getWorkUniqueName(long tabunganId, long notifId) {
        return "notif_" + tabunganId + "_" + notifId;
    }

    private static int[] parseTime(String timeStr) {
        int hour = 19, minute = 0;
        if (timeStr != null && timeStr.contains(":")) {
            String[] parts = timeStr.split(":");
            try {
                hour   = Integer.parseInt(parts[0].trim());
                minute = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {}
        }
        return new int[]{hour, minute};
    }
}
