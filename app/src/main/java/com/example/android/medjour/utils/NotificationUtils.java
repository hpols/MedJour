package com.example.android.medjour.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.example.android.medjour.R;
import com.example.android.medjour.service.NotificationJobService;
import com.example.android.medjour.service.NotificationService;
import com.example.android.medjour.service.NotificationTask;
import com.example.android.medjour.ui.OverviewActivity;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class NotificationUtils {

    //Flextime for window (1h). The notification will be fired based on the user input from the
    // settings of by which time they intend to have meditated each day (if they choose to receive
    // this notification).
    private static final int NOTIFICATION_FLEX_TIME = (int) TimeUnit.MINUTES.toSeconds(1);

    private static final String NOTIFICATION_JOB_TAG = "medjour_notification_tag";
    private static boolean isInitialized;
    private static FirebaseJobDispatcher dispatcher;
    private static SettingsUtils utils;

    private static final String REMINDER_CHANNEL_STRING_ID = "reminder_notification_channel";

    private static final int PENDING_INTENT_REMINDER = 111;
    private static final int INTENT_IGNORE_REMINDER = 222;
    private static final int INTENT_OPEN_CHECKBUDDY = 333;
    private static final int REMINDER_CHANNEL_ID = 123;

    public static final int SET_NOTIFICATION_FOR_TODAY = 0;
    public static final int  SET_NOTIFICATION_FOR_TOMORROW = 1;

    //Schedule the notification job
    public static synchronized void scheduleNotification(@NonNull final Context ctxt, int dayIndicator) {
        utils = new SettingsUtils(ctxt);

        if (isInitialized) return;

        Driver driver = new GooglePlayDriver(ctxt);
        dispatcher = new FirebaseJobDispatcher(driver);

        int trigger = utils.getNotificationTime(ctxt);

        //add a day to the trigger-time so it activates the next day (if not canceled before hand)
        if(dayIndicator == SET_NOTIFICATION_FOR_TOMORROW) {
            trigger += (int) TimeUnit.DAYS.toSeconds(1);
        }

        //if the allotted time has already passed when activated, set to 0 so it immediately triggers.
        if (trigger < 0){
            trigger = 0;
        }

        Job constraintNotificationJob = dispatcher.newJobBuilder()
                .setService(NotificationJobService.class)
                .setTag(NOTIFICATION_JOB_TAG)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(trigger,trigger + NOTIFICATION_FLEX_TIME))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintNotificationJob);

        isInitialized = true;
    }

    //create and set the notification
    public static synchronized void issueNotification(@NonNull final Context ctxt) {
        NotificationManager notMan = (NotificationManager)
                ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notChan = new NotificationChannel(REMINDER_CHANNEL_STRING_ID,
                    ctxt.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notMan.createNotificationChannel(notChan);
        }

        NotificationCompat.Builder notBuild =
                new NotificationCompat.Builder(ctxt, REMINDER_CHANNEL_STRING_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setLargeIcon(largeIcon(ctxt))
                        .setContentTitle(ctxt.getString(R.string.notification_title))
                        .setContentText(ctxt.getString(R.string.notification_body))
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setContentIntent(reminderIntent(ctxt))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(ctxt.getString(R.string.notification_body)))
                        .addAction(openMedJour(ctxt))
                        .addAction(ignoreReminder(ctxt))
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notBuild.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }
        notMan.notify(REMINDER_CHANNEL_ID, notBuild.build());
    }

    //clear the notification
    public static void clearAllNotifications(Context ctxt) {
        NotificationManager notMan = (NotificationManager)
                ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
        notMan.cancelAll();
    }

    public static NotificationCompat.Action openApp(Context ctxt) {
        Intent openCheckBuddy = new Intent(ctxt, OverviewActivity.class);
        openCheckBuddy.setAction(NotificationTask.ACTION_OPEN_APP);
        PendingIntent openCheckBuddyPend = PendingIntent.getActivity(ctxt, INTENT_OPEN_CHECKBUDDY,
                openCheckBuddy, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Action(R.mipmap.ic_launcher_round,
                ctxt.getString(R.string.open_action_title), openCheckBuddyPend);
    }

    private static NotificationCompat.Action openMedJour(Context ctxt) {
        Intent openCheckBuddy = new Intent(ctxt, OverviewActivity.class);
        openCheckBuddy.setAction(NotificationTask.ACTION_OPEN_APP);
        PendingIntent openCheckBuddyPend = PendingIntent.getActivity(ctxt, INTENT_OPEN_CHECKBUDDY,
                openCheckBuddy, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Action(R.mipmap.ic_launcher_round,
                ctxt.getString(R.string.open_action_title), openCheckBuddyPend);
    }

    private static NotificationCompat.Action ignoreReminder(Context ctxt) {
        Intent ignoreReminder = new Intent(ctxt, NotificationService.class);
        ignoreReminder.setAction(NotificationTask.ACTION_DISMISS_NOT);
        PendingIntent ignoreReminderPend = PendingIntent.getService(ctxt, INTENT_IGNORE_REMINDER,
                ignoreReminder, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action(android.R.drawable.ic_notification_clear_all,
                ctxt.getString(R.string.ignore_action_title), ignoreReminderPend);
    }

    private static PendingIntent reminderIntent(Context ctxt) {
        Intent startActivity = new Intent(ctxt, OverviewActivity.class);

        return PendingIntent.getActivity(ctxt, PENDING_INTENT_REMINDER, startActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();

        return BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_round);
    }
}