package com.example.android.medjour.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.example.android.medjour.service.AlarmJobService;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class AlarmUtils {

    //Flextime for window. The callback time is retrieved from SettingsUtils (based on user input)
    private static final int ALARM_FLEX_TIME = (int) TimeUnit.MINUTES.toSeconds(1); //leeway: 1h

    private static final String ALARM_JOB_TAG = "medjour_alarm_tag";
    private static boolean isInitialized;
    private static FirebaseJobDispatcher dispatcher;
    private static SettingsUtils utils;

    public static synchronized void scheduleAlarm(@NonNull final Context ctxt) {
        utils = new SettingsUtils(ctxt);

        Timber.v("scheduleAlarm called");

        if (isInitialized) return;

        Driver driver = new GooglePlayDriver(ctxt);
        dispatcher = new FirebaseJobDispatcher(driver);

        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(AlarmJobService.class)
                .setTag(ALARM_JOB_TAG)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(utils.getMeditationLength(ctxt),
                        utils.getMeditationLength(ctxt) + ALARM_FLEX_TIME))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintReminderJob);

        isInitialized = true;
    }

    public static void issueAlarm(Context ctxt) {
        //RingtoneManager.getRingtone(ctxt, SettingsUtils.getSound(ctxt));
        Timber.v("issue Alarm called");
    }
}