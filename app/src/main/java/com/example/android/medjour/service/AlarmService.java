package com.example.android.medjour.service;

import android.app.IntentService;
import android.content.Intent;

import timber.log.Timber;

public class AlarmService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        assert action != null;
        AlarmTask.executeTask(this, action);

        Timber.v("on Handle Intent called");
    }
}