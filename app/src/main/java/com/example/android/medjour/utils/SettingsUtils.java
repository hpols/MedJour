package com.example.android.medjour.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.medjour.R;

public class SettingsUtils {

    private static SharedPreferences sp;

    //Meditation lengths
    private static final int MIN_5 = 5;
    private static final int MIN_10 = 10;
    private static final int MIN_15 = 15;
    private static final int MIN_20 = 20;
    private static final int MIN_30 = 30;
    private static final int MIN_45 = 45;

    //Callback types
    public static final int RINGTONE_CB = 1;
    public static final int APP_SOUND_CB = 2;
    public static final int VIDEO_CB = 3;

    public static int getMeditationLength(Context ctxt) {
        sp = PreferenceManager.getDefaultSharedPreferences(ctxt);

        String keyForTime = ctxt.getString(R.string.pref_key_med_time);
        String defaultTime = ctxt.getString(R.string.min_20);

        String chosenLength = sp.getString(keyForTime, defaultTime);
        if (chosenLength.equals(ctxt.getString(R.string.min_5))) {
            return MIN_5;
        } else if (chosenLength.equals(ctxt.getString(R.string.min_10))) {
            return MIN_10;
        } else if (chosenLength.equals(ctxt.getString(R.string.min_15))) {
            return MIN_15;
        } else if (chosenLength.equals(defaultTime)) {
            return MIN_20;
        } else if (chosenLength.equals(ctxt.getString(R.string.min_30))) {
            return MIN_30;
        } else {
            return MIN_45;
        }
    }

    public static int getMeditationCallback(Context ctxt) {
        sp = PreferenceManager.getDefaultSharedPreferences(ctxt);

        String keyforCallback = ctxt.getString(R.string.pref_key_callback);
        String defaultCallback = ctxt.getString(R.string.sound_callback);

        String chosenCallback = sp.getString(keyforCallback, defaultCallback);
        if (chosenCallback.equals(ctxt.getString(R.string.sound_callback))) {
            return APP_SOUND_CB;
        } else {
            return VIDEO_CB;
        }
    }
}
