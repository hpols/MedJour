package com.example.android.medjour.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.example.android.medjour.R;

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

        //TODO: playing back wrong tone (always the same even when changed in settings)
    }

    public long getStartedTime() {
        return sharedPref.getLong(STARTED_TIME, 0);
    }

    public void setStartedTime(long started) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(STARTED_TIME, started);
        editor.apply();
    }
}
