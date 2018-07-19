package com.example.android.medjour.notification;

import android.app.IntentService;
import android.content.Intent;

/**
 * holds the service for the reminder
 */
public class NotificationService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public NotificationService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        assert action != null;
        NotificationTask.executeTask(this, action);
    }
}