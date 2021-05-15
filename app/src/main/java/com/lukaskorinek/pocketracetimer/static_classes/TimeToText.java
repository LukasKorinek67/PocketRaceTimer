package com.lukaskorinek.pocketracetimer.static_classes;

import java.util.Calendar;

public class TimeToText {

    public static String longTimeToString(long time_in_seconds) {
        int hours = (int)time_in_seconds/3600;
        int minutes = (int)((time_in_seconds/60) - (hours*60));
        int seconds = (int)(time_in_seconds - (hours*3600) - (minutes*60));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String numbersToTextTime(String text_hours, String text_minutes, String text_seconds) {
        int hours = Integer.parseInt(text_hours);
        int minutes = Integer.parseInt(text_minutes);
        int seconds = Integer.parseInt(text_seconds);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String getTimeFromMilliesDate(long date) {
        Calendar date_time = Calendar.getInstance();
        date_time.setTimeInMillis(date);
        int hours = date_time.get(Calendar.HOUR_OF_DAY);
        int minutes = date_time.get(Calendar.MINUTE);
        int seconds = date_time.get(Calendar.SECOND);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
