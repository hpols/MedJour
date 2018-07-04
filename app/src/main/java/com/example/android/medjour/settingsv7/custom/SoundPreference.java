package com.example.android.medjour.settingsv7.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.example.android.medjour.R;

import java.util.Arrays;

/**
 * A list of sounds based on: https://stackoverflow.com/a/31004356/7601437
 */
public class SoundPreference extends DialogPreference {

    public Context ctxt;
    public String value;
    public Ringtone ringtone;
    public int ringtoneType;
    public CharSequence[] extraRingtones;
    public CharSequence[] extraRingtoneTitles;

    //crashes when set to "private"
    public SoundPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        this.ctxt = ctxt;

        final TypedArray a = ctxt.obtainStyledAttributes(attrs,
                R.styleable.SoundPreference, 0, 0);

        ringtoneType = a.getInt(R.styleable.SoundPreference_ringtoneType,
                RingtoneManager.TYPE_RINGTONE);
        extraRingtones = a.getTextArray(R.styleable.SoundPreference_appOwnSounds);
        extraRingtoneTitles = a.getTextArray(R.styleable.SoundPreference_appOwnSoundsTitles);

        a.recycle();
    }
    //crashes when set to "private"
    public SoundPreference(Context context) {
        this(context, null);
    }

    public String getValue() {
        return value;
    }

    public Uri uriFromRaw(String name) {
        int resId = ctxt.getResources().getIdentifier(name, "raw", ctxt.getPackageName());
        return Uri.parse("android.resource://" + ctxt.getPackageName() + "/" + resId);
    }

    @Override
    public CharSequence getSummary() {

        String ringtoneTitle = null;

        if (value != null) {

            if (ringtoneTitle == null && extraRingtones != null && extraRingtoneTitles != null) {

                for (int i = 0; i < extraRingtones.length; i++) {

                    Uri uriExtra = uriFromRaw(extraRingtones[i].toString());

                    if (uriExtra.equals(Uri.parse(value))) {
                        ringtoneTitle = extraRingtoneTitles[i].toString();
                        break;
                    }
                }
            }

            if (ringtoneTitle == null) {
                Ringtone ringtone = RingtoneManager.getRingtone(ctxt, Uri.parse(value));
                String title = ringtone.getTitle(ctxt);
                if (title != null && title.length() > 0)
                    ringtoneTitle = title;
            }
        }

        CharSequence summary = super.getSummary();

        if (ringtoneTitle != null) {
            if (summary != null)
                return String.format(summary.toString(), ringtoneTitle);
            else
                return ringtoneTitle;
        } else return summary;
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue)
            value = getPersistedString("");
        else {
            if (extraRingtones != null && defaultValue != null && defaultValue.toString().length() > 0) {

                int index = Arrays.asList(extraRingtones).indexOf(defaultValue);
                if (index >= 0)
                    value = uriFromRaw(defaultValue.toString()).toString();
                else value = (String) defaultValue;

            } else value = (String) defaultValue;

            persistString(value);
        }
    }
}
