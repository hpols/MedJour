package com.example.android.medjour.service;

import android.content.Context;

import com.example.android.medjour.utils.AlarmUtils;

import timber.log.Timber;

class AlarmTask {
    public static final  String ACTION_ALARM = "action_alarm";

    public static void executeTask(Context ctxt, String action) {
        switch (action) {
            case ACTION_ALARM:
                AlarmUtils.issueAlarm(ctxt);
        }
        Timber.v("execute Task called");
    }
}
