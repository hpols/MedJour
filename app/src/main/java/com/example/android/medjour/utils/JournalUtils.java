package com.example.android.medjour.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.text.format.DateUtils;
import android.view.View;

import com.example.android.medjour.model.data.JournalDb;

import java.util.concurrent.TimeUnit;

public class JournalUtils {
    static int MAX_PREP_MINS = 5; //minutes
    static long MAX_PREP_TIME = TimeUnit.MINUTES.toMillis(MAX_PREP_MINS);

    //slowly change background colour according to the maximum time allowed
    public static void changeBackground(View root, int startColor, int endColor) {

        ObjectAnimator colorFade = ObjectAnimator.ofObject(root, "backgroundColor",
                new ArgbEvaluator(), startColor, endColor);
        colorFade.setDuration(MAX_PREP_TIME);
        colorFade.start();
    }

    //convert the retrieved/stored milliseconds into readable time
    public static String toMinutes(long timeInMillis) {

        long time = TimeUnit.MILLISECONDS.toSeconds(timeInMillis);

        //TODO: add format and remove "min"
        return DateUtils.formatElapsedTime(time) + " min";
    }

    public static long getCumulativeTime(JournalDb dB) {
        long prepTime = dB.journalDao().getTotalPrepTime();
        long medTime = dB.journalDao().getTotalMedTime();
        long revTime = dB.journalDao().getTotalRevTime();

        return prepTime + medTime + revTime;
    }
}
