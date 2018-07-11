package com.example.android.medjour.service;

import android.content.Context;

import com.example.android.medjour.utils.NotificationUtils;

import timber.log.Timber;

public class NotificationTask {
    static final String ACTION_NOTIFICATION = "notification_service";
    public static final String ACTION_DISMISS_NOT = "dismiss_notification";
    public static final String ACTION_OPEN_APP = "open_app";

    public static void executeTask(Context ctxt, String action) {
        switch (action) {
            case ACTION_NOTIFICATION:
                NotificationUtils.issueNotification(ctxt);
                break;
            case ACTION_DISMISS_NOT:
                NotificationUtils.clearAllNotifications(ctxt);
                break;
            case ACTION_OPEN_APP:
                NotificationUtils.clearAllNotifications(ctxt);
                NotificationUtils.openApp(ctxt);
                break;
        }
        Timber.v("execute Task called");
    }
}
