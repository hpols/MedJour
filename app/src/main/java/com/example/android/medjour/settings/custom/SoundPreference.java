package com.example.android.medjour.settings.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
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

    private final Context ctxt;
    private String summaryValue;
    private Ringtone ringtone;
    private final CharSequence[] appOwnSounds;
    private final CharSequence[] appOwnSoundsTitles; //app own sounds and their titles

    /**
     * constructor
     *
     * @param ctxt  is the context within which the preference works
     * @param attrs are the attributes for the preference
     */
    public SoundPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        this.ctxt = ctxt;

        final TypedArray a = ctxt.obtainStyledAttributes(attrs, R.styleable.SoundPreference,
                0, 0);

        appOwnSounds = a.getTextArray(R.styleable.SoundPreference_appOwnSounds);
        appOwnSoundsTitles = a.getTextArray(R.styleable.SoundPreference_appOwnSoundsTitles);

        a.recycle();
    }

    /**
     * simple constructor
     *
     * @param ctxt is the context within which the preference works
     */
    public SoundPreference(Context ctxt) {
        this(ctxt, null);
    }

    private String getSummaryValue(Context ctxt) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);

        return sharedPref.getString(ctxt.getString(R.string.pref_key_tone),
                ctxt.getString(R.string.temple_bell_value));
    }

    /**
     * retrieve the sound
     *
     * @return a list of sounds
     */
    private Map<String, Uri> getSounds() {

        RingtoneManager ringMan = new RingtoneManager(ctxt);
        ringMan.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor csr = ringMan.getCursor();

        Map<String, Uri> list = new TreeMap<>();
        while (csr.moveToNext()) {
            String ringTitle = csr.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            Uri ringUri = ringMan.getRingtoneUri(csr.getPosition());

            list.put(ringTitle, ringUri);
        }

        return list;
    }

    /**
     * generate the uri from the name
     *
     * @param name is the sound name
     * @return the generated uri
     */
    private Uri uriFromRaw(String name) {
        int audioId = ctxt.getResources().getIdentifier(name, "raw", ctxt.getPackageName());
        return Uri.parse("android.resource://" + ctxt.getPackageName() + "/" + audioId);
    }


    /**
     * get titles from the sounds provided by the app
     *
     * @param name is the name of the app own sound
     * @return the app own sound title or nothing
     */
    private String appOwnSoundsTitle(CharSequence name) {
        if (appOwnSounds != null && appOwnSoundsTitles != null) {
            int index = Arrays.asList(appOwnSounds).indexOf(name);
            return appOwnSoundsTitles[index].toString();
        }
        return null;
    }

    /**
     * get the summary to display in the preference screen
     *
     * @return the summary
     */
    @Override
    public CharSequence getSummary() {

        String ringTitle = null;
        summaryValue = getSummaryValue(getContext());

        if (summaryValue != null) {

            if (appOwnSounds != null && appOwnSoundsTitles != null) {

                //compare the retrieved summary value to the app own sounds for a match.
                Uri uriValue = uriFromRaw(summaryValue);
                for (int i = 0; i < appOwnSounds.length; i++) {
                    Uri uriAppOwn = uriFromRaw(appOwnSounds[i].toString());

                    if (uriAppOwn.equals(uriValue)) {
                        ringTitle = appOwnSoundsTitles[i].toString();
                        break;
                    }
                }
            }

            //if no app own sounds were a match, the user will have chosen a ringtone
            if (ringTitle == null) {
                Ringtone ringtone = RingtoneManager.getRingtone(ctxt, Uri.parse(summaryValue));
                String title = ringtone.getTitle(ctxt);
                if (title != null && title.length() > 0)
                    ringTitle = title;
            }
        }

        //catch the exception that nothing matches (ringtone might have been removed since setting)
        CharSequence summary = super.getSummary();

        if (ringTitle != null) {
            if (summary != null)
                return String.format(summary.toString(), ringTitle);
            else
                return ringTitle;
        } else return summary;
    }

    /**
     * set up the dialog
     *
     * @param builder is the alertDialog.builder to create the dialog
     */
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {

        final Map<String, Uri> sounds = new LinkedHashMap<>();

        if (appOwnSounds != null) {
            for (CharSequence appOwnSound : appOwnSounds) {
                Uri uri = uriFromRaw(appOwnSound.toString());
                String title = appOwnSoundsTitle(appOwnSound);

                sounds.put(title, uri);
            }
        }

        sounds.putAll(getSounds());

        final String[] titleArray = sounds.keySet().toArray(new String[0]);
        final Uri[] uriArray = sounds.values().toArray(new Uri[0]);

        int index = summaryValue != null ? Arrays.asList(uriArray).indexOf(Uri.parse(summaryValue))
                : -1;

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
                    summaryValue = title;
                } else summaryValue = null;
            }
        });

        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);

    }

    /**
     * get the selection when the dialog is closed
     *
     * @param positiveResult checks whether an input is to be retrieved
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {

        super.onDialogClosed(positiveResult);

        if (ringtone != null)
            ringtone.stop();

        if (positiveResult && callChangeListener(summaryValue)) {
            persistString(summaryValue);
            notifyChanged();
        }
    }

    /**
     * Retrieve the defaultValue
     *
     * @param a     as the typeArray
     * @param index is the index of the value
     * @return the default value
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    /**
     * Display the initial value
     *
     * @param restoreValue is a boolean indicating whether the value is restored
     * @param defaultValue is the default value set for the preference
     */
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue)
            summaryValue = getPersistedString(getSummaryValue(getContext()));
        else {
            if (appOwnSounds != null && defaultValue != null && defaultValue.toString().length() > 0) {

                int index = Arrays.asList(appOwnSounds).indexOf(defaultValue);
                if (index >= 0)
                    summaryValue = String.valueOf(getSummary());
                else summaryValue = (String) defaultValue;

            } else summaryValue = (String) defaultValue;

            persistString(summaryValue);
        }
    }
}