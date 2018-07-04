package com.example.android.medjour.settingsv7.custom;

import android.content.DialogInterface;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import com.example.android.medjour.R;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class SoundPreferenceDialog extends PreferenceDialogFragmentCompat
        implements DialogPreference.TargetFragment {

    SoundPreference soundPreference;

    public static final int CALLBACK_RINGTONE = 1;
    public static final int CALLBACK_APP_SOUND = 2;

    private int callBackTye = CALLBACK_RINGTONE;

    public int getCallBackTye() {
        return callBackTye;
    }

    public SoundPreferenceDialog setCallBackTye(int callBackTye) {
        this.callBackTye = callBackTye;
        return this;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        soundPreference = new SoundPreference(getContext());

        final Map<String, Uri> sounds = new LinkedHashMap<>();

        switch (getCallBackTye()) {
            case CALLBACK_RINGTONE:
                Uri uriDefault = RingtoneManager.getDefaultUri(soundPreference.ringtoneType);
                if (uriDefault != null) {
                    Ringtone ringtoneDefault = RingtoneManager.getRingtone(soundPreference.ctxt, uriDefault);
                    if (ringtoneDefault != null) {
                        sounds.put(ringtoneDefault.getTitle(soundPreference.ctxt), uriDefault);
                    }
                }
                break;
            case CALLBACK_APP_SOUND:
                for (CharSequence extraRingtone : soundPreference.extraRingtones) {
                    Uri uri = soundPreference.uriFromRaw(extraRingtone.toString());
                    String title = getExtraRingtoneTitle(extraRingtone);

                    sounds.put(title, uri);
                }
                break;
        }

        sounds.putAll(getSounds(RingtoneManager.TYPE_NOTIFICATION));

        final String[] titleArray = sounds.keySet().toArray(new String[0]);
        final Uri[] uriArray = sounds.values().toArray(new Uri[0]);

        int index = soundPreference.value != null ? Arrays.asList(uriArray).indexOf(Uri.parse(soundPreference.value)) : -1;

        builder.setSingleChoiceItems(titleArray, index, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                if (soundPreference.ringtone != null)
                    soundPreference.ringtone.stop();

                Uri uri = uriArray[which];

                if (uri != null) {
                    if (uri.toString().length() > 0) {
                        soundPreference.ringtone = RingtoneManager.getRingtone(soundPreference.ctxt, uri);
                        soundPreference.ringtone.play();
                    }
                    soundPreference.value = uri.toString();
                } else soundPreference.value = null;
            }
        });

        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);
    }

    private Map<String, Uri> getSounds(int type) {

        RingtoneManager ringtoneManager = new RingtoneManager(soundPreference.ctxt);
        ringtoneManager.setType(type);
        Cursor cursor = ringtoneManager.getCursor();

        Map<String, Uri> list = new TreeMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            Uri notificationUri =  ringtoneManager.getRingtoneUri(cursor.getPosition());

            list.put(notificationTitle, notificationUri);
        }
        return list;
    }

    public String getExtraRingtoneTitle(CharSequence name) {
        if (soundPreference.extraRingtones != null && soundPreference.extraRingtoneTitles != null) {
            int index = Arrays.asList(soundPreference.extraRingtones).indexOf(name);
            return soundPreference.extraRingtoneTitles[index].toString();
        }

        return null;
    }

    @Override
    public Preference findPreference(CharSequence key) {
        return getPreference();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

        if (soundPreference.ringtone != null)
            soundPreference.ringtone.stop();

        if (positiveResult && soundPreference.callChangeListener(soundPreference.value)) {
//            soundPreference.persistString(soundPreference.value);
//            soundPreference.notifyChanged();
        }
    }
}
