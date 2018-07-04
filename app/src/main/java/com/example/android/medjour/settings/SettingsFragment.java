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

    private static final String MED_PREFS = "medPrefs"; // shared preferences identifier
    SharedPreferences sharedPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.menu_settings);

        Preference medTime = findPreference(getString(R.string.pref_key_med_time));
        setValueToSummary(medTime);
        Preference callback = findPreference(getString(R.string.pref_callback_key));
        setValueToSummary(callback);
        Preference sound = findPreference(getString(R.string.pref_key_app_sounds));
        setValueToSummary(sound);
        Preference medReminder = findPreference(getString(R.string.pref_med_reminder_key));
        setValueToSummary(medReminder);
        Preference reminderTime = findPreference(getString(R.string.pref_time_key));
        setValueToSummary(reminderTime);
    }

    private void setValueToSummary(Preference pref) {
        pref.setOnPreferenceChangeListener(this);
        if (pref instanceof ListPreference) {
            sharedPref  = PreferenceManager.getDefaultSharedPreferences(pref.getContext());
            String preferenceString = sharedPref.getString(pref.getKey(), "");
            onPreferenceChange(pref, preferenceString);
        } else {
            String preferenceText = resetPref(pref.getKey());
            onPreferenceChange(pref, preferenceText);
        }
    }

    // This method to store the custom preferences changes
    public void savePref(String key, String value) {
        sharedPref = this.getActivity().getSharedPreferences(MED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString(key, value);
        prefEditor.apply();
    }

    // This method to restore the custom preferences data
    public String resetPref(String key) {
        sharedPref = this.getActivity().getSharedPreferences(MED_PREFS, Context.MODE_PRIVATE);
        if (sharedPref.contains(key))
            return sharedPref.getString(key, "");
        else return "";
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (pref instanceof ListPreference) {
            String stringValue = newValue.toString();
            ListPreference listPreference = (ListPreference) pref;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                CharSequence[] labels = listPreference.getEntries();
                pref.setSummary(labels[prefIndex]);
            }
        } else if (pref instanceof TimePreference) {
            TimePreference timePreference = (TimePreference) pref;
            String timeDisplay = timePreference.getTime();
            savePref(pref.getKey(), timeDisplay);
            pref.setSummary(timeDisplay);
        } else if (pref instanceof SoundPreference) {
            SoundPreference soundPreference = (SoundPreference) pref;
            String soundDisplay = soundPreference.getValue();
            savePref(pref.getKey(), soundDisplay);
            pref.setSummary(soundDisplay);
        }
        return true;
    }
}