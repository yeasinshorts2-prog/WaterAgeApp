package com.waterage.app;

import java.util.List;

public class AlarmModel {
    public int id;
    public int hour;
    public int minute;
    public String message;
    public String ringtoneUri;
    public String ringtoneName;
    public boolean vibrate;
    public List<Integer> days; // Calendar.SUNDAY (1) to Calendar.SATURDAY (7)
    public boolean enabled;

    public AlarmModel(int id, int hour, int minute, String message, String ringtoneUri, String ringtoneName, boolean vibrate, List<Integer> days, boolean enabled) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.message = message;
        this.ringtoneUri = ringtoneUri;
        this.ringtoneName = ringtoneName;
        this.vibrate = vibrate;
        this.days = days;
        this.enabled = enabled;
    }
}