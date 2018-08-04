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

import java.util.concurrent.TimeUnit;

public class JournalUtils {
    public static final long MAX_PREP_TIME = TimeUnit.MINUTES.toMillis(5); //5 minutes
    public static final long MAX_REVIEW_TIME = TimeUnit.MINUTES.toMillis(10); //10 minutes

    private static SharedPreferences sharedPref;

    private static final String TOTAL_TIME = "total_time";
    public static final int NO_TOT_TIME = 0; //no time has been logged via the entries so far
    private static final String LAST_DATE = "last_date";
    public static final String PREP_FLAG = "preparation_call";
    public static final String REVIEW_FLAG = "review_call";

    public static final int NOT_SILENCE = 100;
    public static final int NOT_NORMAL = 200;
    public static final String DELETE = "deleting entry";
    public static final String CREATE = "creating entry";

    //boolean keys and codes
    private static final String REPEAT_ACCESS = "repeated app access";
    private static final String STUDENT = "student";
    private static final String VIDEO_UNLOCKED = "video unlocked";
    private static final String FULLY_UPGRADED = "fully upgraded";
    private static final String ADS_REMOVED = "ads removed";

    public static final int BOO_REPEAT = 10;
    public static final int BOO_STUDENT = 20;
    public static final int BOO_FULLY_UPGRADED = 30;
    public static final int BOO_ADS_REMOVED = 40;
    public static final int BOO_VIDEOS_UNLOCKED = 50;

    /**
     * get the current time.
     *
     * @return the time in milliseconds as a long
     */
    public static long getNow() {
        return System.currentTimeMillis();
    }

    // –––––– BOOLEANS –––––– //

    /**
     * ensure the user only logs one meditation per day (as per the C.MI. regulations)
     *
     * @param ctxt is the Context used to retrieve the date from sharedPreferences by retrieveLastDate
     * @return the boolean confirming the last entry was today or not
     */
    public static boolean hasMeditatedToday(Context ctxt) {
        String lastDate = retrieveLastDate(ctxt);

        String today = String.valueOf(DateConverter.toDate(System.currentTimeMillis()));

        boolean meditatedToday = false;
        if (lastDate.equals(today)) {
            meditatedToday = true;
        } else if (!lastDate.equals(today) || lastDate.isEmpty()) {
            meditatedToday = false;
        }

        return meditatedToday;
    }

    /**
     * retrieve the needed boolean form sharedPreferences
     *
     * @param ctxt         is the Context for the PreferenceManager to work in
     * @param BOOLEAN_CODE is a unique code identifying the boolean to be retrieved
     * @return the boolean value
     */
    public static boolean getSharedPrefBoo(Context ctxt, int BOOLEAN_CODE) {
        String BOOLEAN_KEY = null;
        boolean defBoo = false;

        switch (BOOLEAN_CODE) {
            case BOO_REPEAT:
                BOOLEAN_KEY = REPEAT_ACCESS;
                defBoo = ctxt.getResources().getBoolean(R.bool.repeat_acess_default);
                break;
            case BOO_STUDENT:
                BOOLEAN_KEY = STUDENT;
                defBoo = ctxt.getResources().getBoolean(R.bool.student_default);
                break;
            case BOO_FULLY_UPGRADED:
                BOOLEAN_KEY = FULLY_UPGRADED;
                defBoo = ctxt.getResources().getBoolean(R.bool.upgraded_default);
                break;
            case BOO_ADS_REMOVED:
                BOOLEAN_KEY = ADS_REMOVED;
                defBoo = ctxt.getResources().getBoolean(R.bool.ads_removed_default);
                break;
            case BOO_VIDEOS_UNLOCKED:
                BOOLEAN_KEY = VIDEO_UNLOCKED;
                defBoo = ctxt.getResources().getBoolean(R.bool.video_unlocked_default);
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);
        return sharedPref.getBoolean(BOOLEAN_KEY, defBoo);
    }

    /**
     * update the stored boolean in sharedPreferences
     *
     * @param ctxt          is the Context for the PreferenceManager to work in
     * @param booleanUpdate is the updated boolean information
     * @param BOOLEAN_CODE  is a unique code identifying the boolean to be retrieved
     */
    public static void setSharedPrefBoo(Context ctxt, boolean booleanUpdate, int BOOLEAN_CODE) {
        String BOOLEAN_KEY = null;

        switch (BOOLEAN_CODE) {
            case BOO_REPEAT:
                BOOLEAN_KEY = REPEAT_ACCESS;
                break;
            case BOO_STUDENT:
                BOOLEAN_KEY = STUDENT;
                break;
            case BOO_FULLY_UPGRADED:
                BOOLEAN_KEY = FULLY_UPGRADED;
                break;
            case BOO_ADS_REMOVED:
                BOOLEAN_KEY = ADS_REMOVED;
                break;
            case BOO_VIDEOS_UNLOCKED:
                BOOLEAN_KEY = VIDEO_UNLOCKED;
        }
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(BOOLEAN_KEY, booleanUpdate);
        editor.apply();
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
     * @param timeInMillis a long in milliseconds
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

    //TODO: work with long so we can retrieve dd/mm/yyyy pertaining to locale format.

    /**
     * retrieve the date of the last date from the db
     *
     * @param ctxt       the context to call the sharedPreference from
     * @return the last date
     */
    public static String retrieveLastDate(Context ctxt) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);
        return sharedPref.getString(LAST_DATE, "");
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

    /**
     * Changes the ringer mode on the device to either silent or back to normal.
     * See ShushMe app from AND-Udacity course
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