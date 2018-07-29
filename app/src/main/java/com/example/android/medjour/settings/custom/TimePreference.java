package com.example.android.medjour.settings.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * based on: https://stackoverflow.com/a/5533295/7601437
 * A TimePicker enabling the user to select a time of day
 */
public class TimePreference extends DialogPreference {

    private int lastHour = 0;
    private int lastMinute = 0;
    private boolean isAm;
    private TimePicker picker = null;


    private static int getHour(String time) {
        String[] pieces = time.split(":");

        return (Integer.parseInt(pieces[0]));
    }

    private static int getMinute(String time) {
        String[] pieces = time.split(":");

        return (Integer.parseInt(pieces[1]));
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());

        //check locale settings whether to use am/pm or 24 hour display
        picker.setIs24HourView(false);

        //changing color of title textview
//        int numberId = getDialog().getContext().getResources()
//                .getIdentifier("android:id/???", null, null);
//        TextView numbers = (TextView) getDialog().findViewById(numberId);
//        numbers.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));

        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();

            String time = getTime();

            if (callChangeListener(time)) {
                persistString(time);
            }
        }
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

    public String getTime() {
        String meridianId;
        if (isAm) {
            if (lastHour > 12) {
                meridianId = " pm";
            } else {
                meridianId = " am";
            }
        } else {
            meridianId = "";
        }

        NumberFormat formatter = new DecimalFormat("00");
        String minuteString = formatter.format(lastMinute);

        return lastHour + ":" + minuteString + meridianId;
    }
}