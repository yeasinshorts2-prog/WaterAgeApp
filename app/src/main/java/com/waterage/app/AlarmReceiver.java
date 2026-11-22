package com.waterage.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "water_alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmId = intent.getIntExtra("alarmId", 0);
        int hour = intent.getIntExtra("hour", 8);
        int minute = intent.getIntExtra("minute", 0);
        String message = intent.getStringExtra("message");
        String ringtoneUri = intent.getStringExtra("ringtoneUri");
        boolean vibrate = intent.getBooleanExtra("vibrate", true);
        ArrayList<Integer> days = intent.getIntegerArrayListExtra("days");

        // পরের দিনের জন্য একই অ্যালার্ম আবার সেট করে রাখা
        rescheduleNextDay(context, alarmId, hour, minute, message, ringtoneUri, vibrate, days);

        // আজকের দিন সিলেক্ট করা আছে কি না চেক করা (ফাঁকা থাকলে প্রতিদিন বাজবে)
        Calendar now = Calendar.getInstance();
        int today = now.get(Calendar.DAY_OF_WEEK);
        if (days != null && !days.isEmpty() && !days.contains(today)) {
            return;
        }

        // স্ক্রিন জাগানোর জন্য WakeLock নেওয়া
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE,
                    "WaterAgeApp:AlarmWakeLock"
            );
            wl.acquire(10000);
        }

        // ফুল-স্ক্রিন অ্যালার্ম অ্যাক্টিভিটি ওপেন করার ইনটেন্ট
        Intent ringIntent = new Intent(context, AlarmRingActivity.class);
        ringIntent.putExtra("message", message);
        ringIntent.putExtra("ringtoneUri", ringtoneUri);
        ringIntent.putExtra("vibrate", vibrate);
        ringIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Android 10+ এর জন্য ফুল-স্ক্রিন নোটিফিকেশন দেখানো
        showFullScreenNotification(context, alarmId, message, ringIntent);

        // সরাসরি ফুল-স্ক্রিন ওপেন করার চেষ্টা করা
        try {
            context.startActivity(ringIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFullScreenNotification(Context context, int alarmId, String message, Intent ringIntent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Water Alarm",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("পানি খাওয়ার অ্যালার্ম নোটিফিকেশন");
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(channel);
        }

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                alarmId,
                ringIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String displayMsg = (message != null && !message.isEmpty())
                ? message
                : "পানি খাওয়ার সময় হয়ে গেছে, এখন এক গ্লাস পানি খান";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_water_logo)
                .setContentTitle("💧 পানি খাওয়ার অ্যালার্ম")
                .setContentText(displayMsg)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true);

        nm.notify(alarmId, builder.build());
    }

    private void rescheduleNextDay(Context context, int alarmId, int hour, int minute,
                                   String message, String ringtoneUri, boolean vibrate,
                                   ArrayList<Integer> days) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar nextCal = Calendar.getInstance();
        nextCal.add(Calendar.DAY_OF_YEAR, 1);
        nextCal.set(Calendar.HOUR_OF_DAY, hour);
        nextCal.set(Calendar.MINUTE, minute);
        nextCal.set(Calendar.SECOND, 0);
        nextCal.set(Calendar.MILLISECOND, 0);

        Intent nextIntent = new Intent(context, AlarmReceiver.class);
        nextIntent.putExtra("alarmId", alarmId);
        nextIntent.putExtra("hour", hour);
        nextIntent.putExtra("minute", minute);
        nextIntent.putExtra("message", message);
        nextIntent.putExtra("ringtoneUri", ringtoneUri);
        nextIntent.putExtra("vibrate", vibrate);
        if (days != null) {
            nextIntent.putIntegerArrayListExtra("days", days);
        }

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                alarmId,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextCal.getTimeInMillis(), pi);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextCal.getTimeInMillis(), pi);
            }
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextCal.getTimeInMillis(), pi);
        }
    }
}