package com.example.android.medjour.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import com.example.android.medjour.model.DateConverter;
import com.example.android.medjour.model.data.JournalDb;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JournalUtils {
    static long MAX_PREP_TIME = TimeUnit.MINUTES.toMillis(5); //5 minutes
    static long MAX_REVIEW_TIME = TimeUnit.MINUTES.toMillis(10); //10 minutes

    private static SharedPreferences sharedPref;

    private static String TOTAL_TIME = "total_time";
    public static final int NO_TOT_TIME = 0;
    private static String LAST_DATE ="last_date";


    public static boolean hasMeditatedToday(JournalDb dB) {
        Date lastDate = dB.journalDao().getLastEntryDate();

        Date today = DateConverter.toDate(System.currentTimeMillis());

        return lastDate == today;
    }

    //slowly change background colour according to the maximum time allowed
    public static void changeBackground(View root, int startColor, int endColor) {

        ObjectAnimator colorFade = ObjectAnimator.ofObject(root, "backgroundColor",
                new ArgbEvaluator(), startColor, endColor);
        colorFade.setDuration(MAX_PREP_TIME);
        colorFade.start();
    }

    //convert the retrieved/stored milliseconds into readable time
    public static String toMinutes(long timeInMillis) {

        int seconds = (int) (timeInMillis / 1000) % 60;
        int minutes = (int) ((timeInMillis / (1000 * 60)) % 60);

        //round seconds
        if (seconds > 30) {
            minutes += 1;
        }
        int hours = (int) ((timeInMillis / (1000 * 60 * 60)) % 24);
        if (hours == 0) {
            return minutes + "min";
        } else {
            return hours + ":" + minutes;
        }
    }

    public static long getCumulativeTime(JournalDb dB) {
        long prepTime = dB.journalDao().getTotalPrepTime();
        long medTime = dB.journalDao().getTotalMedTime();
        long revTime = dB.journalDao().getTotalRevTime();

        return prepTime + medTime + revTime;
    }

    public static Date getLastEntry(JournalDb dB) {
        return dB.journalDao().getLastEntryDate();
    }

    public static void saveCumulativeTime(Context ctxt, long cumulativeTime) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(TOTAL_TIME, cumulativeTime);
        editor.apply();
    }

    public static long retrieveCumulativeTime(Context ctxt) {
     sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        return sharedPref.getLong(TOTAL_TIME, NO_TOT_TIME);
    }

    public static void saveLastDate(Context ctxt, String date) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LAST_DATE, date);
        editor.apply();
    }

    public static String retireveLastDate (Context ctxt) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);
        return sharedPref.getString(LAST_DATE, "");
    }

}