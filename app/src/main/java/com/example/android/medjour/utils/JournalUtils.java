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

import com.example.android.medjour.R;
import com.example.android.medjour.model.DateConverter;
import com.example.android.medjour.model.data.JournalDb;

import java.util.concurrent.TimeUnit;

public class JournalUtils {
    public static long MAX_PREP_TIME = TimeUnit.MINUTES.toMillis(5); //5 minutes
    public static long MAX_REVIEW_TIME = TimeUnit.MINUTES.toMillis(10); //10 minutes

    private static SharedPreferences sharedPref;

    private static String TOTAL_TIME = "total_time";
    public static final int NO_TOT_TIME = 0; //no time has been logged via the entries so far
    private static String LAST_DATE = "last_date";
    public static String PREP_FLAG = "preparation_call";
    public static String REVIEW_FLAG = "review_call";
    public static String WIDGET_CALL = "widget_actvity";

    public static final int NOT_SILENCE = 100;
    public static final int NOT_NORMAL = 200;
    private static String REPEAT_ACCESS = "repeated app access";
    public static final String DELETE = "deleting entry";
    public static final String CREATE = "creating entry";

    public static boolean isIsFullyUpgraded() {
        return isFullyUpgraded;
    }

    public static void setIsFullyUpgraded(boolean isFullyUpgraded) {
        JournalUtils.isFullyUpgraded = isFullyUpgraded;
    }

    public static boolean isFullyUpgraded;

    public static boolean isIsStudent() {
        return isStudent;
    }

    public static void setIsStudent(boolean isStudent) {
        JournalUtils.isStudent = isStudent;
    }

    public static boolean isStudent;

    /**
     * get the current time.
     *
     * @return the time in miliseconds as a long
     */
    public static long getNow() {
        return System.currentTimeMillis();
    }

    /**
     * ensure the user only logs one meditation per day (as per the C.MI. regulations)
     *
     * @param dB the database to be queried for the last entry date
     * @return the boolean confirming the last entry was today or not
     */
    public static boolean hasMeditatedToday(JournalDb dB) {
        String lastDate = dB.journalDao().getLastEntryDate();

        String today = String.valueOf(DateConverter.toDate(System.currentTimeMillis()));


        return lastDate == today;
    }

    /**
     * slowly change background colour according to the maximum time allowed
     *
     * @param root       is the View being animated
     * @param startColor is the starting color fr the animation
     * @param endColor   is the resulting color for the animation
     * @param prepFlag   is a flag identifying the calling activity so as to determine the length of
     *                   the animations
     */
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

    /**
     * convert the retrieved/stored milliseconds into readable time
     *
     * @param timeInMillis a long in miliseconds
     * @return a String to display the time
     */
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

    /**
     * get the total time stored in the sharedPreferences
     *
     * @param ctxt the context for the sharedPreferences
     * @return the long of accumulated time
     */
    public static long retrieveTotalTimeFromPref(Context ctxt) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        return sharedPref.getLong(TOTAL_TIME, NO_TOT_TIME);
    }

    /**
     * keep tabs on the latest date of entry
     *
     * @param ctxt the context for the sharedPreference
     * @param date the date to be stored
     */
    public static void saveLastDate(Context ctxt, String date) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LAST_DATE, date);
        editor.apply();
    }

    /**
     * update the total time in sharedPreference for future use in Overview, Journal and Widget
     *
     * @param ctxt               the context for the sharedPreference
     * @param totalTimeFromEntry the time to be added or subtracted from the entry in question
     * @param crudAction         the database action that will take place. I.e. should we add or
     *                           subtract totalTimeFromEntry
     */
    public static void updateTotalTimeFromPref(Context ctxt, long totalTimeFromEntry,
                                               String crudAction) {

        long storedTotalTime = retrieveTotalTimeFromPref(ctxt);
        long updatedTotalTime = 0;
        switch (crudAction) {
            case CREATE:
                updatedTotalTime = storedTotalTime + totalTimeFromEntry;
                break;
            case DELETE:
                updatedTotalTime = storedTotalTime - totalTimeFromEntry;
        }


        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(TOTAL_TIME, updatedTotalTime);
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

    /**
     * keep track of whether this is the first time the user opens the app
     *
     * @param ctxt the context for the sharedPreference
     * @return a boolean indicating whether this is a repeat
     */
    public static boolean isRepeatedAccess(Context ctxt) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        return sharedPref.getBoolean(REPEAT_ACCESS,
                ctxt.getResources().getBoolean(R.bool.repeat_acess_default));
    }

    /**
     * update the boolean to track whether the user has opened the app before
     *
     * @param repeatedAccess a boolean indicating whether the user has opened the app before
     * @param ctxt           the context for hte sharedPreference
     */
    public static void setRepeatedAccess(boolean repeatedAccess, Context ctxt) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(REPEAT_ACCESS, repeatedAccess);
        editor.apply();
    }
}