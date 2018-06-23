package com.example.android.medjour.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.text.format.DateUtils;
import android.view.View;

import java.util.concurrent.TimeUnit;

public class UiUtils {
    static int MAX_PREP_MINS = 5; //minutes
    static long MAX_PREP_TIME = TimeUnit.MINUTES.toMillis(MAX_PREP_MINS);

    public static void changeBackground(View root, int startColor, int endColor) {

        ObjectAnimator colorFade = ObjectAnimator.ofObject(root, "backgroundColor",
                new ArgbEvaluator(), startColor, endColor);
        colorFade.setDuration(MAX_PREP_TIME);
        colorFade.start();
    }

    public static String toMinutes(long timeInMillis) {

        long time = TimeUnit.MILLISECONDS.toSeconds(timeInMillis);

        return DateUtils.formatElapsedTime(time) + " min";
    }
}
