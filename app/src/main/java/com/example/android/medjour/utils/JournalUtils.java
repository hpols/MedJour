package com.example.android.medjour.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;

import com.example.android.medjour.model.DateConverter;
import com.example.android.medjour.model.EntryExecutor;
import com.example.android.medjour.model.data.JournalDb;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JournalUtils {
    public static long MAX_PREP_TIME = TimeUnit.MINUTES.toMillis(5); //5 minutes
    public static long MAX_REVIEW_TIME = TimeUnit.MINUTES.toMillis(10); //10 minutes

    private static SharedPreferences sharedPref;

    private static String TOTAL_TIME = "total_time";
    public static final int NO_TOT_TIME = 0;
    private static String LAST_DATE = "last_date";
    public static String PREP_FLAG = "preparation_call";
    public static String REVIEW_FLAG = "review_call";
    public static String WIDGET_CALL = "widget_actvity";

    public static final int NOT_SILENCE = 100;
    public static final int NOT_NORMAL = 200;
    private static final String KEY_NOT_MODE = "notification mode";

    public static boolean hasMeditatedToday(JournalDb dB) {
        Date lastDate = dB.journalDao().getLastEntryDate();

        Date today = DateConverter.toDate(System.currentTimeMillis());

        return lastDate == today;
    }

    //slowly change background colour according to the maximum time allowed
    public static void changeBackground(View root, int startColor, int endColor, String prepFlag) {
        long duration;
        if (prepFlag.equals(PREP_FLAG)) {
            duration = MAX_PREP_TIME;
        } else {
            duration = MAX_REVIEW_TIME;
        }

        ObjectAnimator colorFade = ObjectAnimator.ofObject(root, "backgroundColor",
                new ArgbEvaluator(), startColor, endColor);
        colorFade.setDuration(duration);
        colorFade.start();
    }

    //convert the retrieved/stored milliseconds into readable time
    public static String toMinutes(long timeInMillis) {

        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(timeInMillis);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(timeInMillis);

        //round seconds
        if (seconds > 30) {
            minutes += 1;
        }
        int hours = (int) TimeUnit.MILLISECONDS.toHours(timeInMillis);
        if (hours == 0) {
            return minutes + " min";
        } else {
            return hours + ":" + minutes;
        }
    }

    public static long getTotalTime(final JournalDb dB) {
        final long[] prepTime = new long[1];
        final long[] medTime = new long[1];
        final long[] revTime = new long[1];
        EntryExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                prepTime[0] = dB.journalDao().getTotalPrepTime();
                medTime[0] = dB.journalDao().getTotalMedTime();
                revTime[0] = dB.journalDao().getTotalRevTime();
            }
        });
        return prepTime[0] + medTime[0] + revTime[0];
    }

    public static void saveTotalTime(Context ctxt, long totalTime) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(TOTAL_TIME, totalTime);
        editor.apply();

        //reset date if cumulativeTime = 0 => no entries in the db.
        if (totalTime == 0) {
            saveLastDate(ctxt, "");
        }
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

    //TODO: work with long so we can retrieve dd/mm/yyyy pertaining to locale format.

    /**
     * retrieve the date of the last date from the db
     *
     * @param ctxt       the context to call the sharedPreference from
     * @param widgetCall identifies the calling activity
     * @return the last date
     */
    public static String retrieveLastDate(Context ctxt, String widgetCall) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);
        return sharedPref.getString(LAST_DATE, "");
    }

    /**
     * Changes the ringer mode on the device to either silent or back to normal.
     * See Shushme app from AND-udacity course
     *
     * @param ctxt             is the context for the audio service
     * @param notificationMode is the audio-notification setting (silent or normal) to be set
     */
    public static void setRingerMode(Context ctxt, int notificationMode) {


        //handle audio
        NotificationManager notMan = (NotificationManager)
                ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
        // Check for DND permissions for API 24+
        if (notMan != null && (Build.VERSION.SDK_INT < 24 ||
                (Build.VERSION.SDK_INT >= 24 &&
                        notMan.isNotificationPolicyAccessGranted()))) {
            AudioManager audioManager = (AudioManager) ctxt.getSystemService(Context.AUDIO_SERVICE);
            assert audioManager != null;
            int audioMode;
            if (notificationMode == NOT_SILENCE) {
                audioMode = AudioManager.RINGER_MODE_SILENT;
            } else {
                audioMode = AudioManager.RINGER_MODE_NORMAL;
            }
            audioManager.setRingerMode(audioMode);
        }

    }
}