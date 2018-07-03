package com.example.android.medjour.settingsv7.settingsv7;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.example.android.medjour.R;
import com.example.android.medjour.settingsv7.settingsv7.custom.SoundPreference;
import com.example.android.medjour.settingsv7.settingsv7.custom.SoundPreferenceDialog;
import com.example.android.medjour.settingsv7.settingsv7.custom.TimePreference;
import com.example.android.medjour.settingsv7.settingsv7.custom.TimePreferenceDialog;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.menu_settings);

        SharedPreferences sharedPref = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            if (!(p instanceof SwitchPreference)) {
                String value = sharedPref.getString(p.getKey(), "");
                setPreferenceSummary(p, value);

//                final ListPreference callbackType = (ListPreference) findPreference(getString(R.string.pref_callback_key));
//                final SoundPreference callbackTone = (SoundPreference) findPreference(getString(R.string.pref_key_tone));
//                callbackType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                    @Override
//                    public boolean onPreferenceChange(Preference preference, Object newValue) {
//                        SoundPreferenceDialog soundPreferenceDialog = new SoundPreferenceDialog();
//                        int callBack = SettingsUtils.getMeditationCallback(getContext());
//                        boolean boolEnable = false;
//                        switch (callBack) {
//                            case SettingsUtils.RINGTONE_CB:
//                                soundPreferenceDialog.setCallBackSelection(soundPreferenceDialog.SHOW_RINGTONES);
//                                boolEnable = true;
//                                break;
//                            case SettingsUtils.APP_SOUND_CB:
//                                soundPreferenceDialog.setCallBackSelection(soundPreferenceDialog.SHOW_APP_SOUNDS);
//                                boolEnable = true;
//                                break;
//                            case SettingsUtils.VIDEO_CB:
//                                boolEnable = false;
//                                break;
//                        }
//                        callbackTone.setVisible(boolEnable);
//                        return true;
//                    }
//                });
            }
        }
    }

    //TODO: not showing up fro List & Edit preferences!
    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // register the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_key_med_time))) {
            //TODO: retrieve set meditation time length
        } else if (key.equals(getString(R.string.pref_callback_key))) {
            //TODO: retrieve chosen callback type
        } else if (key.equals(getString(R.string.pref_key_app_sounds))) {
            //TODO: retrieve app sound
        } else if (key.equals(getString(R.string.pref_med_reminder_key))) {
            //TODO: set reminder intent
        } else if (key.equals(getString(R.string.pref_time_key))) {
            //TODO: pass selected time to reminder
        }

        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof SwitchPreference)) {
                //TODO: crashes when turning switch off
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference || preference instanceof SoundPreference) {
            if (preference instanceof TimePreference) {
                dialogFragment = new TimePreferenceDialog();
            } else {
                dialogFragment = new SoundPreferenceDialog();
            }
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
