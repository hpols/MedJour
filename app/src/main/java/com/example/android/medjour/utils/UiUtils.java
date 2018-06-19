package com.example.android.medjour.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.View;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class UiUtils {

    public static void changeBackground(View root, int startColor, int endColor) {
        int MAX_MINS = 5; //minutes
        long MAX_TIME = TimeUnit.MINUTES.toMillis(MAX_MINS);

        ObjectAnimator colorFade = ObjectAnimator.ofObject(root, "backgroundColor",
                new ArgbEvaluator(), startColor, endColor);
        colorFade.setDuration(MAX_TIME);
        colorFade.start();
    }

    public static void getTimeStamp(long startTime) {
        startTime = Calendar.getInstance().getTimeInMillis();
    }
}
