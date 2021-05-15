package com.lukaskorinek.pocketracetimer.static_classes;

import java.util.Calendar;
import java.util.Formatter;

public class MillisToStringDate {

    public static String getStringDateFromMillis(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("%02d.%02d.%d %02d:%02d", mDay, mMonth, mYear, mHour, mMinute);
        return formatter.toString();
    }
}
