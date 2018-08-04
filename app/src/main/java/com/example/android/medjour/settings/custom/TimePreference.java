package com.example.android.medjour.settings.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import com.example.android.medjour.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A timepicker would be the obvious choice, yet seeing it would require upping the sdk to api21 to
 * be able to customize the color, so we would not have white numbers on white background, this is
 * the next best solution.
 * <p>
 * based on: https://stackoverflow.com/a/5533295/7601437
 * and: https://www.programcreek.com/java-api-examples/?code=nathan-osman/chronosnap/chronosnap-master/app/src/main/java/com/nathanosman/chronosnap/preference/TimeIntervalPreference.java#
 */
public class TimePreference extends DialogPreference implements NumberPicker.Formatter {

    @Override
    public String format(int value) {
        return String.format("%02d", value);
    }

    // References to the pickers
    private NumberPicker hourPicker;
    private NumberPicker minutePicker;
    private ToggleButton amPmToggle;

    private static final String SPACE = " ";
    private static final String COLON = ":";

    private boolean isAm;

    /**
     * Initialize the dialog with the custom layout
     */
    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_timepicker);
    }

    /**
     * Perform initialization of the view items
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        // Obtain the current value
        String currentValue = getPersistedString(
                getContext().getString(R.string.pref_default_reminder_time));

        //split off the am/pm string for the toggle
        String[] timeElements = currentValue.split(SPACE);

        // split into hour and minute for the pickers
        String[] values = timeElements[0].split(COLON);

       // Obtain references to the pickers and initialize them with the correct value
        hourPicker = view.findViewById(R.id.dialog_hour_np);
        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);
        hourPicker.setFormatter(this);
        hourPicker.setValue(Integer.valueOf(values[0]));

        minutePicker = view.findViewById(R.id.dialog_minute_np);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setFormatter(this);
        minutePicker.setValue(Integer.valueOf(values[1]));

        amPmToggle = view.findViewById(R.id.dialog_amPm_tb);
        if (timeElements[1].equals(getContext().getString(R.string.time_string_am))) {
            amPmToggle.setChecked(true);
        } else {
            amPmToggle.setChecked(false);
        }
        amPmToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAm = isChecked;
            }
        });
    }

    /**
     * Persist the value of the pickers
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            String meridianId;
            if (isAm) {
                meridianId = getContext().getString(R.string.time_string_am);
            } else {
                meridianId = getContext().getString(R.string.time_string_pm);
            }



            // Get the current value as a string
            String currentValue = setTwoDigit((hourPicker).getValue()) + COLON +
                    setTwoDigit(minutePicker.getValue()) + SPACE + meridianId;

            if (callChangeListener(currentValue)) {
                persistString(currentValue);
            }
        }
    }

    private String setTwoDigit(int value) {
        NumberFormat formatter = new DecimalFormat("00");

        return formatter.format(value);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    // ensure the defaultValue is shown until changed by the user
    // see: https://stackoverflow.com/a/11875422/7601437
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        persistString(restoreValue ?
                getPersistedString((String)defaultValue) : (String)defaultValue);
    }
}