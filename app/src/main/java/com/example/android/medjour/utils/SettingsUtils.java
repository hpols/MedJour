package com.example.android.medjour.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.example.android.medjour.R;

import timber.log.Timber;

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
    public static final int APP_SOUND_CB = 1;
    public static final int VIDEO_CB = 2;

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

    public static void getSound(Context ctxt) {

        String keyForSounds = ctxt.getString(R.string.pref_key_tone);
        String defaultSound = ctxt.getString(R.string.pref_default_val_sound);

        String chosenSound = PreferenceManager.getDefaultSharedPreferences(ctxt)
                .getString(keyForSounds, defaultSound);
        RingtoneManager manager = new RingtoneManager(ctxt);
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);

        Ringtone r;


        Uri soundToPlay;
        if (isAppOwn(ctxt, chosenSound)) {
            // Syntax : android.resource://[package]/[resource_id]
            String packageName = "android.resource://com.example.android.medjour/";
            if (chosenSound.equals(ctxt.getString(R.string.a_tone_value))) {
                soundToPlay = Uri.parse(packageName + R.raw.a_tone);
            } else if (chosenSound.equals(ctxt.getString(R.string.computer_magic_value))) {
                soundToPlay = Uri.parse(packageName + R.raw.computer_magic);
            } else if (chosenSound.equals(ctxt.getString(R.string.metal_gong_value))) {
                soundToPlay = Uri.parse(packageName + R.raw.metal_gong);
            } else {
                soundToPlay = Uri.parse(packageName + R.raw.temple_bell);
            }
        } else {
            soundToPlay = Uri.parse(chosenSound);
        }

        RingtoneManager.setActualDefaultRingtoneUri(ctxt, RingtoneManager.TYPE_NOTIFICATION,
                soundToPlay);
        RingtoneManager.getRingtone(ctxt, soundToPlay).play();

        Timber.v(String.valueOf(RingtoneManager.getActualDefaultRingtoneUri(ctxt, RingtoneManager.TYPE_NOTIFICATION)));
        //TODO: playing back wrong tone (always the same even when changed in settings)
    }

    private static boolean isAppOwn(Context ctxt, String chosenSound) {
        return chosenSound.equals(ctxt.getString(R.string.a_tone_value))
                || chosenSound.equals(ctxt.getString(R.string.computer_magic_value))
                || chosenSound.equals(ctxt.getString(R.string.metal_gong_value))
                || chosenSound.equals(ctxt.getString(R.string.temple_bell_value));

    }
}
