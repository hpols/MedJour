package com.example.android.medjour.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.example.android.medjour.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

public class SettingsUtils {

    private SharedPreferences sharedPref;
    private static final String STARTED_TIME = "started_time";

    //Meditation lengths
    private static final int MIN_5 = 5;
    private static final int MIN_10 = 10;
    private static final int MIN_15 = 15;
    private static final int MIN_20 = 20;
    private static final int MIN_30 = 30;
    private static final int MIN_45 = 45;

    //Callback types
    public static final int APP_SOUND_CB = 1;
    public static final int VIDEO_CB = 2;

    public SettingsUtils(Context ctxt) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);
    }

    public int getMeditationLength(Context ctxt) {

        String keyForTime = ctxt.getString(R.string.pref_key_med_time);
        String defaultTime = ctxt.getString(R.string.min_20);

        String chosenLength = sharedPref.getString(keyForTime, defaultTime);
        if (chosenLength.equals(ctxt.getString(R.string.min_5_value))) {
            return MIN_5;
        } else if (chosenLength.equals(ctxt.getString(R.string.min_10_value))) {
            return MIN_10;
        } else if (chosenLength.equals(ctxt.getString(R.string.min_15_value))) {
            return MIN_15;
        } else if (chosenLength.equals(ctxt.getString(R.string.min_20_value))) {
            return MIN_20;
        } else if (chosenLength.equals(ctxt.getString(R.string.min_30_value))) {
            return MIN_30;
        } else {
            return MIN_45;
        }
    }

    public int getMeditationCallback(Context ctxt) {
        String keyforCallback = ctxt.getString(R.string.pref_key_callback);
        String defaultCallback = ctxt.getString(R.string.sound_callback);

        String chosenCallback = sharedPref.getString(keyforCallback, defaultCallback);
        if (chosenCallback.equals(ctxt.getString(R.string.sound_callback))) {
            return APP_SOUND_CB;
        } else {
            return VIDEO_CB;
        }
    }

    public Uri playCallbackSound(Context ctxt) {

        String keyForSounds = ctxt.getString(R.string.pref_key_tone);
        String defaultSound = ctxt.getString(R.string.pref_default_val_sound);

        String chosenSound = PreferenceManager.getDefaultSharedPreferences(ctxt)
                .getString(keyForSounds, defaultSound);

        Uri soundToPlay;
        // Syntax : android.resource://[package]/[resource_id]
        String packageName = "android.resource://com.example.android.medjour/";
        if (chosenSound.equals(ctxt.getString(R.string.a_tone_value))) {
            soundToPlay = Uri.parse(packageName + R.raw.a_tone);
        } else if (chosenSound.equals(ctxt.getString(R.string.computer_magic_value))) {
            soundToPlay = Uri.parse(packageName + R.raw.computer_magic);
        } else if (chosenSound.equals(ctxt.getString(R.string.metal_gong_value))) {
            soundToPlay = Uri.parse(packageName + R.raw.metal_gong);
        } else if (chosenSound.equals(ctxt.getString(R.string.temple_bell_value))) {
            soundToPlay = Uri.parse(packageName + R.raw.temple_bell);
        } else {
            soundToPlay = Uri.parse(chosenSound);
        }

        return soundToPlay;
    }

    public String getVideofromPrefSetting(Context ctxt) {
        String video = null;
        switch (getMeditationLength(ctxt)) {
            case MIN_5:
                video = ctxt.getString(R.string.meditation_5min);
                break;
            case MIN_10:
                video = ctxt.getString(R.string.meditation_10min);
                break;
            case MIN_15:
                video = ctxt.getString(R.string.meditation_15min);
                break;
            case MIN_20:
                video = ctxt.getString(R.string.meditation_20min);
                break;
            case MIN_30:
                video = ctxt.getString(R.string.meditation_30min);
                break;
            case MIN_45:
                video = ctxt.getString(R.string.meditation_45min);
                break;
            default:
                Timber.e("there is no video provided for a meditation length of "
                        + getMeditationLength(ctxt));
                video = ctxt.getString(R.string.meditation_test);
        }
        return video;
    }

    public long getStartedTime() {
        return sharedPref.getLong(STARTED_TIME, 0);
    }

    public void setStartedTime(long started) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(STARTED_TIME, started);
        editor.apply();
    }

    public int getNotificationTime(Context ctxt) {
        String keyforReminderTime = ctxt.getString(R.string.pref_key_reminder_time);
        String defaultReminderTime = ctxt.getString(R.string.pref_default_reminder_time);

        String chosenTime = PreferenceManager.getDefaultSharedPreferences(ctxt)
                .getString(keyforReminderTime, defaultReminderTime);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        long timeInMils = 0;
        try {
            Date timeOfDay = sdf.parse(chosenTime);
            timeInMils = timeOfDay.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long now = System.currentTimeMillis();

        return Math.toIntExact(timeInMils - now / 1000);
    }

    public boolean reminderIsEnabled(Context ctxt) {
        String keyForReminder = ctxt.getString(R.string.pref_med_reminder_key);
        boolean defaultBoo = ctxt.getResources().getBoolean(R.bool.notification_activated_default);

        return sharedPref.getBoolean(keyForReminder, defaultBoo);
    }

    public boolean vibrateEnabled(Context ctxt) {
        String keyForVibrate = ctxt.getString(R.string.pref_key_vibrate);
        boolean defaultVibBoo = ctxt.getResources().getBoolean(R.bool.vibrate_default);

        return sharedPref.getBoolean(keyForVibrate, defaultVibBoo);
    }
}
