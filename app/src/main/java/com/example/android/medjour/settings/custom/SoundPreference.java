package com.example.android.medjour.settings.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.example.android.medjour.R;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A list of sounds based on: https://stackoverflow.com/a/31004356/7601437
 */
public class SoundPreference extends DialogPreference {

    private Context ctxt;
    private String value;
    private Ringtone ringtone;
    private CharSequence[] appOwnSounds;
    private CharSequence[] appOwnSoundsTitles;

    public SoundPreference(Context ctxt, AttributeSet attrs) {

        super(ctxt, attrs);

        this.ctxt = ctxt;

        final TypedArray a = ctxt.obtainStyledAttributes(attrs, R.styleable.SoundPreference,
                0, 0);

        appOwnSounds = a.getTextArray(R.styleable.SoundPreference_appOwnSounds);
        appOwnSoundsTitles = a.getTextArray(R.styleable.SoundPreference_appOwnSoundsTitles);

        a.recycle();
    }

    public SoundPreference(Context context) {
        this(context, null);
    }

    public String getValue() {
        return value;
    }

    private Map<String, Uri> getSounds(int type) {

        RingtoneManager ringMan = new RingtoneManager(ctxt);
        ringMan.setType(type);
        Cursor csr = ringMan.getCursor();

        Map<String, Uri> list = new TreeMap<String, Uri>();
        while (csr.moveToNext()) {
            String ringTitle = csr.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            Uri ringUri =  ringMan.getRingtoneUri(csr.getPosition());

            list.put(ringTitle, ringUri);
        }

        return list;
    }

    private Uri uriFromRaw(String name) {
        int audioId = ctxt.getResources().getIdentifier(name, "raw", ctxt.getPackageName());
        return Uri.parse("android.resource://" + ctxt.getPackageName() + "/" + audioId);
    }

    private String appOwnSoundsTitle(CharSequence name) {
        if (appOwnSounds != null && appOwnSoundsTitles != null) {
            int index = Arrays.asList(appOwnSounds).indexOf(name);
            return appOwnSoundsTitles[index].toString();
        }

        return null;
    }

    @Override
    public CharSequence getSummary() {

        String ringTitle = null;

        if (value != null) {

            if (ringTitle == null && appOwnSounds != null && appOwnSoundsTitles != null) {

                for (int i = 0; i < appOwnSounds.length; i++) {

                    Uri uriExtra = uriFromRaw(appOwnSounds[i].toString());

                    if (uriExtra.equals(Uri.parse(value))) {
                        ringTitle = appOwnSoundsTitles[i].toString();
                        break;
                    }
                }
            }

            if (ringTitle == null) {
                Ringtone ringtone = RingtoneManager.getRingtone(ctxt, Uri.parse(value));
                String title = ringtone.getTitle(ctxt);
                if (title != null && title.length() > 0)
                    ringTitle = title;
            }
        }

        CharSequence summary = super.getSummary();

        if (ringTitle != null) {
            if (summary != null)
                return String.format(summary.toString(), ringTitle);
            else
                return ringTitle;
        } else return summary;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {

        final Map<String, Uri> sounds = new LinkedHashMap<String, Uri>();

        if (appOwnSounds != null) {
            for (CharSequence appOwnSound : appOwnSounds) {
                Uri uri = uriFromRaw(appOwnSound.toString());
                String title = appOwnSoundsTitle(appOwnSound);

                sounds.put(title, uri);
            }
        }

        sounds.putAll(getSounds(RingtoneManager.TYPE_NOTIFICATION));


        final String[] titleArray = sounds.keySet().toArray(new String[0]);
        final Uri[] uriArray = sounds.values().toArray(new Uri[0]);

        int index = value != null ? Arrays.asList(uriArray).indexOf(Uri.parse(value)) : -1;

        builder.setSingleChoiceItems(titleArray, index, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                if (ringtone != null)
                    ringtone.stop();

                String title = titleArray[which];
                Uri uri = uriArray[which];

                if (uri != null) {
                    if (uri.toString().length() > 0) {
                        ringtone = RingtoneManager.getRingtone(ctxt, uri);
                        ringtone.play();
                    }
                    value = uri.toString();
                } else value = null;
            }
        });

        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {

        super.onDialogClosed(positiveResult);

        if (ringtone != null)
            ringtone.stop();

        if (positiveResult && callChangeListener(value)) {
            persistString(value);
            notifyChanged();
        }

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
            if (appOwnSounds != null && defaultValue != null && defaultValue.toString().length() > 0) {

                int index = Arrays.asList(appOwnSounds).indexOf((CharSequence) defaultValue);
                if (index >= 0)
                    value = uriFromRaw(defaultValue.toString()).toString();
                else value = (String) defaultValue;

            } else value = (String) defaultValue;

            persistString(value);
        }
    }
}