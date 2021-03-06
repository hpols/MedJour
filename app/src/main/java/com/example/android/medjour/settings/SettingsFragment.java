package com.example.android.medjour.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.example.android.medjour.R;
import com.example.android.medjour.settings.custom.SoundPreference;
import com.example.android.medjour.utils.JournalUtils;

public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPref;

    //preferences that need further setup beyond what the xml provides
    private ListPreference callback;
    private SoundPreference sound;

    /**
     * create the Fragment, set the preference summaries and ensure the sound preference is only
     * active when the sound callback is selected
     *
     * @param savedInstanceState retaining information as needed
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.menu_settings);

        sharedPref = getPreferenceScreen().getSharedPreferences();

        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = prefScreen.getPreference(i);
            if (!(pref instanceof SwitchPreference)) {
                setPrefSummary(pref, sharedPref.getString(pref.getKey(), ""));
            }
        }

        callback = (ListPreference) findPreference(getString(R.string.pref_key_callback));

        if (!JournalUtils.getSharedPrefBoo(getActivity(), JournalUtils.BOO_VIDEOS_UNLOCKED)
                && !JournalUtils.getSharedPrefBoo(getActivity(), JournalUtils.BOO_STUDENT)) {
            callback.setEnabled(false);
            callback.setSummary(getString(R.string.settings_callback_needs_upgrade));
        } else {
            callback.setEnabled(true);
            setSoundPrefActivation(callback.getValue());
        }

        setupSoundPreference();
    }

    private void setupSoundPreference() {
        sound = (SoundPreference) findPreference(getString(R.string.pref_key_sounds));
//        if (sound.getValue() == null) {
//            sharedPref.getString(getString(R.string.pref_key_tone),
//                    getString(R.string.temple_bell_value));
//            sound.getSummary();
//        }
    }

    /**
     * set the preference summary
     *
     * @param pref  is the preference in question
     * @param value is the value to be displayed in the summary
     */
    private void setPrefSummary(Preference pref, Object value) {
        String stringValue = value.toString();

        if (pref instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPref = (ListPreference) pref;
            int prefIndex = listPref.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                pref.setSummary(listPref.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            pref.setSummary(stringValue);
        }
    }

    /**
     * Only enable the soundPreference if the user wants a callback sound. When video is selected
     * we do not need to select anything further.
     *
     * @param value of the callback preference which acts as a flag to know whether the sound
     *              preference option should be enabled
     */
    private void setSoundPrefActivation(String value) {
        if (sound == null) { //after student activation or upgrade this might not yet be set up
            setupSoundPreference();
        }
        if (value.equals(getString(R.string.video_value_callback))) {
            sound.setEnabled(false);
            sound.setSummary("");
        } else {
            sound.setEnabled(true);
        }
    }

    /**
     * Update summaries and activation of the sound preference
     *
     * @param sharedPref holds the information of the preference just changed
     * @param key        is the key of the preference just changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
        setSoundPrefActivation(callback.getValue());

        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof SwitchPreference || preference instanceof SoundPreference)) {
                setPrefSummary(preference, sharedPref.getString(key, ""));
            }
        }

        if (key.equals(getString(R.string.pref_key_sounds))) {
            sound.setSummary(sharedPref.getString(key,
                    getString(R.string.pref_key_sounds)));
        }

        //if Callback is changed (back to) sound, ensure the summary is displayed for the latter.
        if (key.equals(getString(R.string.pref_key_callback))) {
           String callBackSelected = sharedPref.getString(getString(R.string.pref_key_callback),
                   getString(R.string.sound_callback));
           if (callBackSelected.equals(getString(R.string.sound_callback))) {
               sound.setSummary(sharedPref.getString(key,
                       getString(R.string.pref_key_sounds)));
           }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}