package com.example.android.medjour.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.View;

import com.example.android.medjour.model.DateConverter;
import com.example.android.medjour.model.data.JournalDb;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JournalUtils {
    static long MAX_PREP_TIME = TimeUnit.MINUTES.toMillis(5); //5 minutes
    static long MAX_REVIEW_TIME = TimeUnit.MINUTES.toMillis(10); //10 minutes

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
}