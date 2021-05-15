package com.lukaskorinek.pocketracetimer.static_classes;

public class DateOfBirthConverter {

    public static String getCzechDateString(String english_date) {
        //24.12.2021
        return (english_date.substring(8,10) + "." + english_date.substring(5,7) + "." + english_date.substring(0,4));
    }

    public static String getEnglishDateString(String czech_date) {
        //2021-12-24
        return (czech_date.substring(6,10) + "-" + czech_date.substring(3,5) + "-" + czech_date.substring(0,2));
    }

    public static String getEnglishDateString(String day, String month, String year) {
        if(day.length() == 1) {
            day = "0" + day;
        }
        if(month.length() == 1) {
            month = "0" + month;
        }
        return (year + "-" + month + "-" + day);
    }
}
