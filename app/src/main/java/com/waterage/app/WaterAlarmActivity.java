package com.waterage.app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WaterAlarmActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WaterAlarmsPrefs";
    private static final String KEY_ALARMS = "alarms_list";

    private List<AlarmModel> alarmList = new ArrayList<>();
    private AlarmAdapter adapter;
    private Gson gson = new Gson();

    // রিংটোন সিলেক্ট করার জন্য ভেরিয়েবল
    private String tempRingtoneUri = "";
    private String tempRingtoneName = "ডিফল্ট অ্যালার্ম";
    private TextView currentDialogRingtoneText;

    private final ActivityResultLauncher<Intent> ringtoneLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri != null) {
                        tempRingtoneUri = uri.toString();
                        Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                        tempRingtoneName = (ringtone != null) ? ringtone.getTitle(this) : "কাস্টম রিংটোন";
                    } else {
                        tempRingtoneUri = "";
                        tempRingtoneName = "ডিফল্ট অ্যালার্ম";
                    }
                    if (currentDialogRingtoneText != null) {
                        currentDialogRingtoneText.setText("রিংটোন: " + tempRingtoneName);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_alarm);

        loadAlarms();

        ListView listView = findViewById(R.id.listViewAlarms);
        FloatingActionButton fabAddAlarm = findViewById(R.id.fabAddAlarm);

        adapter = new AlarmAdapter();
        listView.setAdapter(adapter);

        fabAddAlarm.setOnClickListener(v -> showAddAlarmDialog());
    }

    private void showAddAlarmDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_alarm, null);

        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        EditText etMessage = dialogView.findViewById(R.id.etAlarmMessage);
        Button btnPickRingtone = dialogView.findViewById(R.id.btnPickRingtone);
        TextView tvSelectedRingtone = dialogView.findViewById(R.id.tvSelectedRingtone);
        CheckBox cbVibrate = dialogView.findViewById(R.id.cbVibrate);

        CheckBox cbSat = dialogView.findViewById(R.id.cbSat);
        CheckBox cbSun = dialogView.findViewById(R.id.cbSun);
        CheckBox cbMon = dialogView.findViewById(R.id.cbMon);
        CheckBox cbTue = dialogView.findViewById(R.id.cbTue);
        CheckBox cbWed = dialogView.findViewById(R.id.cbWed);
        CheckBox cbThu = dialogView.findViewById(R.id.cbThu);
        CheckBox cbFri = dialogView.findViewById(R.id.cbFri);

        tempRingtoneUri = "";
        tempRingtoneName = "ডিফল্ট অ্যালার্ম";
        currentDialogRingtoneText = tvSelectedRingtone;

        btnPickRingtone.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "অ্যালার্মের রিংটোন সিলেক্ট করুন");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            ringtoneLauncher.launch(intent);
        });

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("সেভ করুন", (dialog, which) -> {
                    int hour, minute;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        hour = timePicker.getHour();
                        minute = timePicker.getMinute();
                    } else {
                        hour = timePicker.getCurrentHour();
                        minute = timePicker.getCurrentMinute();
                    }

                    String message = etMessage.getText().toString().trim();
                    if (message.isEmpty()) {
                        message = "পানি খাওয়ার সময় হয়ে গেছে, এখন এক গ্লাস পানি খান";
                    }

                    List<Integer> selectedDays = new ArrayList<>();
                    if (cbSat.isChecked()) selectedDays.add(Calendar.SATURDAY);
                    if (cbSun.isChecked()) selectedDays.add(Calendar.SUNDAY);
                    if (cbMon.isChecked()) selectedDays.add(Calendar.MONDAY);
                    if (cbTue.isChecked()) selectedDays.add(Calendar.TUESDAY);
                    if (cbWed.isChecked()) selectedDays.add(Calendar.WEDNESDAY);
                    if (cbThu.isChecked()) selectedDays.add(Calendar.THURSDAY);
                    if (cbFri.isChecked()) selectedDays.add(Calendar.FRIDAY);

                    int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
                    AlarmModel alarm = new AlarmModel(
                            id, hour, minute, message,
                            tempRingtoneUri, tempRingtoneName,
                            cbVibrate.isChecked(), selectedDays, true
                    );

                    alarmList.add(alarm);
                    saveAlarms();
                    scheduleAlarm(alarm);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "অ্যালার্ম সেট হয়েছে!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void scheduleAlarm(AlarmModel alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarm.hour);
        cal.set(Calendar.MINUTE, alarm.minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // সময় পার হয়ে গেলে পরের দিনের জন্য সেট করা
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("alarmId", alarm.id);
        intent.putExtra("hour", alarm.hour);
        intent.putExtra("minute", alarm.minute);
        intent.putExtra("message", alarm.message);
        intent.putExtra("ringtoneUri", alarm.ringtoneUri);
        intent.putExtra("vibrate", alarm.vibrate);
        intent.putIntegerArrayListExtra("days", new ArrayList<>(alarm.days));

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }
    }

    private void cancelAlarm(AlarmModel alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pi);
    }

    private void saveAlarms() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_ALARMS, gson.toJson(alarmList)).apply();
    }

    private void loadAlarms() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(KEY_ALARMS, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<AlarmModel>>() {}.getType();
            List<AlarmModel> loaded = gson.fromJson(json, type);
            if (loaded != null) {
                alarmList = loaded;
            }
        }
    }

    private String formatTime(int hour, int minute) {
        String amPm = (hour >= 12) ? "PM" : "AM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
    }

    private String formatDays(List<Integer> days) {
        if (days == null || days.isEmpty()) return "প্রতিদিন";
        StringBuilder sb = new StringBuilder();
        for (int d : days) {
            if (sb.length() > 0) sb.append(", ");
            switch (d) {
                case Calendar.SATURDAY: sb.append("শনি"); break;
                case Calendar.SUNDAY: sb.append("রবি"); break;
                case Calendar.MONDAY: sb.append("সোম"); break;
                case Calendar.TUESDAY: sb.append("মঙ্গল"); break;
                case Calendar.WEDNESDAY: sb.append("বুধ"); break;
                case Calendar.THURSDAY: sb.append("বৃহঃ"); break;
                case Calendar.FRIDAY: sb.append("শুক্র"); break;
            }
        }
        return sb.toString();
    }

    private class AlarmAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return alarmList.size();
        }

        @Override
        public Object getItem(int position) {
            return alarmList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return alarmList.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(WaterAlarmActivity.this).inflate(R.layout.item_alarm, parent, false);
            }

            AlarmModel alarm = alarmList.get(position);

            TextView tvTime = convertView.findViewById(R.id.tvItemTime);
            TextView tvMsg = convertView.findViewById(R.id.tvItemMessage);
            TextView tvDays = convertView.findViewById(R.id.tvItemDays);
            Switch switchAlarm = convertView.findViewById(R.id.switchAlarm);
            Button btnDelete = convertView.findViewById(R.id.btnDeleteAlarm);

            tvTime.setText(formatTime(alarm.hour, alarm.minute));
            tvMsg.setText(alarm.message);

            String vibText = alarm.vibrate ? "চালু" : "বন্ধ";
            String ringName = (alarm.ringtoneName != null) ? alarm.ringtoneName : "ডিফল্ট";
            tvDays.setText("দিন: " + formatDays(alarm.days) + " | রিংটোন: " + ringName + " | ভাইব্রেশন: " + vibText);

            switchAlarm.setOnCheckedChangeListener(null);
            switchAlarm.setChecked(alarm.enabled);
            switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
                alarm.enabled = isChecked;
                saveAlarms();
                if (isChecked) {
                    scheduleAlarm(alarm);
                } else {
                    cancelAlarm(alarm);
                }
            });

            btnDelete.setOnClickListener(v -> {
                cancelAlarm(alarm);
                alarmList.remove(position);
                saveAlarms();
                notifyDataSetChanged();
                Toast.makeText(WaterAlarmActivity.this, "অ্যালার্ম মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show();
            });

            return convertView;
        }
    }
}