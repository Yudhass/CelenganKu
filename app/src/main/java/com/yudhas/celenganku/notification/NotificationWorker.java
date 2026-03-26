package com.yudhas.celenganku.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.yudhas.celenganku.database.AppDatabase;
import com.yudhas.celenganku.database.entity.NotifikasiTabungan;
import com.yudhas.celenganku.database.entity.Tabungan;
import com.yudhas.celenganku.util.DateHelper;

public class NotificationWorker extends Worker {

    public static final String KEY_TABUNGAN_ID    = "tabungan_id";
    public static final String KEY_NOTIF_ID       = "notif_id";       // ID in notifikasi_tabungan
    public static final String KEY_NOTIF_TYPE     = "notif_type";
    public static final String KEY_INTERVAL_MENIT = "interval_menit"; // for permenit self-reschedule

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long tabunganId   = getInputData().getLong(KEY_TABUNGAN_ID, -1);
        long notifId      = getInputData().getLong(KEY_NOTIF_ID, -1);
        String notifType  = getInputData().getString(KEY_NOTIF_TYPE);
        int intervalMenit = getInputData().getInt(KEY_INTERVAL_MENIT, 15);

        if (tabunganId == -1) return Result.failure();

        try {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            Tabungan tabungan = db.tabunganDao().getById(tabunganId);
            if (tabungan == null) return Result.success(); // tabungan deleted

            // If this is a notif-specific job, verify the notif record still exists
            if (notifId > 0) {
                NotifikasiTabungan notif = db.notifikasiTabunganDao().getById(notifId);
                if (notif == null) return Result.success(); // notif deleted — stop rescheduling
            }

            // Send appropriate notification
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
                    // Use notifId as unique Android notification ID so multiple schedules
                    // each show their own notification entry in the tray.
                    int androidNotifId = notifId > 0 ? (int) notifId : (int) tabunganId;
                    NotificationHelper.sendReminderNotification(
                            getApplicationContext(),
                            androidNotifId,
                            tabungan.getNama(),
                            tabungan.getSisaNominal(),
                            notifType != null ? notifType : "harian"
                    );
                }
            }

            // "permenit" self-reschedule
            if ("permenit".equals(notifType) && notifId > 0) {
                NotifikasiTabungan freshNotif = db.notifikasiTabunganDao().getById(notifId);
                int freshInterval = (freshNotif != null && freshNotif.getNotifIntervalMenit() > 0)
                        ? freshNotif.getNotifIntervalMenit()
                        : intervalMenit;
                NotificationScheduler.schedulePerMenitNext(
                        getApplicationContext(), tabunganId, notifId, freshInterval);
            }

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
