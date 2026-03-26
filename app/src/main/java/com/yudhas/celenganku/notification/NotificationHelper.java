package com.yudhas.celenganku.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.yudhas.celenganku.MainActivity;
import com.yudhas.celenganku.R;
import com.yudhas.celenganku.util.CurrencyHelper;

public class NotificationHelper {

    public static final String CHANNEL_ID = "celenganku_channel";
    public static final String CHANNEL_NAME = "CelenganKu Reminder";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Pengingat tabungan harian, mingguan, atau bulanan");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void sendReminderNotification(Context context, long tabunganId,
                                                String namaTabungan, long sisaNominal,
                                                String notifType) {
        String title;
        String message;

        switch (notifType) {
            case "mingguan":
                title = "Sudah nabung minggu ini? 💰";
                message = "Target '" + namaTabungan + "' masih kurang " + CurrencyHelper.formatRupiah(sisaNominal);
                break;
            case "bulanan":
                title = "Update tabungan bulan ini! 📊";
                message = "Target '" + namaTabungan + "' masih perlu " + CurrencyHelper.formatRupiah(sisaNominal) + " lagi";
                break;
            default: // harian
                title = "Yuk nabung hari ini! 💪";
                message = "Sisa " + CurrencyHelper.formatRupiah(sisaNominal) + " lagi untuk '" + namaTabungan + "'";
                break;
        }

        sendNotification(context, (int) tabunganId, title, message);
    }

    public static void sendDeadlineNotification(Context context, long tabunganId,
                                                String namaTabungan, long sisaNominal,
                                                long daysRemaining) {
        String title = "⚠️ Deadline Mendekat!";
        String message = "Target '" + namaTabungan + "' tinggal " + daysRemaining
                + " hari lagi. Sisa " + CurrencyHelper.formatRupiah(sisaNominal);
        sendNotification(context, (int) (tabunganId + 10000), title, message);
    }

    public static void sendAchievementNotification(Context context, long tabunganId,
                                                   String namaTabungan) {
        String title = "🎉 Target Tercapai!";
        String message = "Selamat! Tabungan '" + namaTabungan + "' berhasil tercapai!";
        sendNotification(context, (int) (tabunganId + 20000), title, message);
    }

    private static void sendNotification(Context context, int notifId, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notifId, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat notifManager = NotificationManagerCompat.from(context);
            notifManager.notify(notifId, builder.build());
        } catch (SecurityException e) {
            // Permission not granted on Android 13+
        }
    }
}

