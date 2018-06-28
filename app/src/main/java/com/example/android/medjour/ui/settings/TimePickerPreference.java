package com.example.android.medjour.ui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.example.android.medjour.R;

/**
 * based on: https://github.com/jakobulbrich/preferences-demo
 * A TimePicker enabling the user to select a timeInMinutes of day
 */
public class TimePickerPreference extends DialogPreference {

    //in minutes since the start of the day (midnight)
    private int timeInMinutes;

    public TimePickerPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    /**
     * Retrieve the timeInMinutes from the SharedPreference
     *
     * @return the integer value for the stored timeInMinutes
     */
    public int getTimeInMinutes() {
        return timeInMinutes;
    }

    /**
     * Saves the timeInMinutes to the SharedPreference
     *
     * @param timeInMinutes The timeInMinutes to save
     */
    public void setTimeInMinutes(int timeInMinutes) {
        this.timeInMinutes = timeInMinutes;

        // Save to SharedPreference
        persistInt(timeInMinutes);
    }

    /**
     * get default value as int
     *
     * @param a     are the retrieved values
     * @param index is the value set to be displayed
     * @return index and default value
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    /**
     * Returns the layout resource that is used as the content View for the dialog
     */
    @Override
    public int getDialogLayoutResource() {
        return R.layout.dialog_timepicker;
    }

    /**Set initial value for the preference
     *
     * @param restorePersistedValue reflects whether the value is restored
     * @param defaultValue is the default value as an integer
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // If the value can be restored, do it. If not, use the default value.
        setTimeInMinutes(restorePersistedValue ?
                getPersistedInt(timeInMinutes) : (int) defaultValue);
    }
}