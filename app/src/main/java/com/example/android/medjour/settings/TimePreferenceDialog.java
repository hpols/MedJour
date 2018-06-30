package com.example.android.medjour.settings;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

/**
 * The Dialog to display {@link TimePickerPreference}.
 */
public class TimePreferenceDialog extends PreferenceDialogFragmentCompat {

    /**
     * The TimePicker widget
     */
    private TimePicker timePicker;

    /**
     * Create a new instance of the TimePreferenceDialog and store the preference's key
     *
     * @param key The key of the Preference in question
     * @return the new instance
     */
    public static TimePreferenceDialog newInstance(String key) {
        final TimePreferenceDialog fragment = new TimePreferenceDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    /**
     * Set up the dialogView
     *
     * @param view the that will display the timePicker
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        timePicker = new TimePicker(getContext());

        // retrieve time from the preference
        int minutesPastMidnight = 0;
        DialogPreference pref = getPreference();
        if (pref instanceof TimePickerPreference) {
            minutesPastMidnight = ((TimePickerPreference) pref).getTimeInMinutes();
        }

        // Set the time to the TimePicker
        if (minutesPastMidnight != 0) {

            timePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
            timePicker.setCurrentHour(minutesPastMidnight / 60);
            timePicker.setCurrentMinute(minutesPastMidnight % 60);
        }
    }

    /**
     * When the dialog is closed
     *
     * @param positiveResult indicates whether the action is completed or canceled
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Get the current values from the TimePicker
            int hourDisplay;
            int minuteDisplay;
            if (Build.VERSION.SDK_INT >= 23) {
                hourDisplay = timePicker.getHour();
                minuteDisplay = timePicker.getMinute();
            } else {
                hourDisplay = timePicker.getCurrentHour();
                minuteDisplay = timePicker.getCurrentMinute();
            }

            // Generate value to save
            int minutesPastMidnight = (hourDisplay * 60) + minuteDisplay;

            // Save the value
            DialogPreference dialogPref = getPreference();
            if (dialogPref instanceof TimePickerPreference) {
                TimePickerPreference timePickerPreference = ((TimePickerPreference) dialogPref);

                //ensure there was a changed made
                if (timePickerPreference.callChangeListener(minutesPastMidnight)) {
                    // Save the value
                    timePickerPreference.setTimeInMinutes(minutesPastMidnight);
                }
            }
        }
    }
}