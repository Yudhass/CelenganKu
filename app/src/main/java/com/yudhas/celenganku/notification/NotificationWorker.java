package com.yudhas.celenganku.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.yudhas.celenganku.database.AppDatabase;
import com.yudhas.celenganku.database.entity.Tabungan;
import com.yudhas.celenganku.util.DateHelper;

public class NotificationWorker extends Worker {

    public static final String KEY_TABUNGAN_ID    = "tabungan_id";
    public static final String KEY_NOTIF_TYPE     = "notif_type";
    public static final String KEY_INTERVAL_MENIT = "interval_menit"; // for self-reschedule

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long tabunganId  = getInputData().getLong(KEY_TABUNGAN_ID, -1);
        String notifType = getInputData().getString(KEY_NOTIF_TYPE);
        int intervalMenit = getInputData().getInt(KEY_INTERVAL_MENIT, 15);

        if (tabunganId == -1) return Result.failure();

        try {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            Tabungan tabungan = db.tabunganDao().getById(tabunganId);

            if (tabungan == null) return Result.success();

            if (tabungan.isCompleted()) {
                NotificationHelper.sendAchievementNotification(
                        getApplicationContext(), tabunganId, tabungan.getNama());
            } else {
                long daysRemaining = DateHelper.getDaysRemaining(tabungan.getDeadline());
                if (daysRemaining >= 0 && daysRemaining <= 5) {
                    NotificationHelper.sendDeadlineNotification(
                            getApplicationContext(),
                            tabunganId,
                            tabungan.getNama(),
                            tabungan.getSisaNominal(),
                            daysRemaining
                    );
                } else {
                    NotificationHelper.sendReminderNotification(
                            getApplicationContext(),
                            tabunganId,
                            tabungan.getNama(),
                            tabungan.getSisaNominal(),
                            notifType != null ? notifType : "harian"
                    );
                }
            }

            // "permenit" self-reschedule: re-queue next OneTimeWorkRequest
            if ("permenit".equals(notifType)) {
                // Read fresh interval from DB in case user changed it
                int freshInterval = tabungan.getNotifIntervalMenit() > 0
                        ? tabungan.getNotifIntervalMenit() : intervalMenit;
                NotificationScheduler.schedulePerMenitNext(
                        getApplicationContext(), tabunganId, freshInterval);
            }

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
