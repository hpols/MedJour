package com.example.android.medjour.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.example.android.medjour.R;
import com.example.android.medjour.settings.custom.SoundPreference;
import com.example.android.medjour.settings.custom.TimePreference;

public class SettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SHARED_PREFERENCES = "medPrefs"; // shared preferences identifier

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.menu_settings);

        Preference medtime = findPreference(getString(R.string.pref_key_med_time));
        setValueToSummary(medtime);
        Preference callback = findPreference(getString(R.string.pref_callback_key));
        setValueToSummary(callback);
        Preference sound = findPreference(getString(R.string.pref_key_app_sounds));
        setValueToSummary(sound);
        Preference medReminder = findPreference(getString(R.string.pref_med_reminder_key));
        setValueToSummary(medReminder);
        Preference reminderTime = findPreference(getString(R.string.pref_time_key));
        setValueToSummary(reminderTime);

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

    private void setValueToSummary(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        if (preference instanceof ListPreference) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        } else {
            String preferenceString = restorePreferences(preference.getKey());
            onPreferenceChange(preference, preferenceString);
        }
    }

    // This method to store the custom preferences changes
    public void savePreferences(String key, String value) {
        SharedPreferences myPreferences = this.getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString(key, value);
        myEditor.apply();
    }

    // This method to restore the custom preferences data
    public String restorePreferences(String key) {
        SharedPreferences myPreferences = this.getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (myPreferences.contains(key))
            return myPreferences.getString(key, "");
        else return "";
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            String stringValue = newValue.toString();
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                CharSequence[] labels = listPreference.getEntries();
                preference.setSummary(labels[prefIndex]);
            }
        } else if (preference instanceof TimePreference) {
            TimePreference timePreference = (TimePreference) preference;
            String timeDisplay = timePreference.getTime();
            savePreferences(preference.getKey(), timeDisplay);
            preference.setSummary(timeDisplay);
        } else if (preference instanceof SoundPreference) {
            SoundPreference soundPreference = (SoundPreference) preference;
            String soundDisplay = soundPreference.getValue();
            savePreferences(preference.getKey(), soundDisplay);
            preference.setSummary(soundDisplay);
        }
        return true;
    }

}
