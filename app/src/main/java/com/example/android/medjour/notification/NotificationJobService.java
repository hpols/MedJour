package com.example.android.medjour.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.utils.JournalUtils;
import com.example.android.medjour.utils.NotificationUtils;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import timber.log.Timber;

/**
 * Setting up the background task to run the {@link NotificationJobService} on.
 */
public class NotificationJobService extends JobService {

    private AsyncTask backgroundTask;

    @Override
    public boolean onStartJob(final JobParameters job) {
        backgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context ctxt = NotificationJobService.this;
                JournalDb dB = JournalDb.getInstance(ctxt);
                fireNotification(ctxt);

                boolean reschedule = false;
                jobFinished(job, reschedule);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(job, false);
            }
        };
        backgroundTask.execute();

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (backgroundTask != null) backgroundTask.cancel(true);

        Timber.v("on Stop Job called");
        return false;
    }

    // Only fire one notification per day, regardless of whether the user followed its prompt or
    // not.
    private void fireNotification(Context ctxt) {
        long timeSinceLastNotification = NotificationUtils.timeSinceLastNot(ctxt);

        boolean dayPassedSinceLastNot = false;

        if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
            dayPassedSinceLastNot = true;
        }

        if (dayPassedSinceLastNot && !JournalUtils.hasMeditatedToday(ctxt)) {
            NotificationTask.executeTask(ctxt, NotificationTask.ACTION_NOTIFICATION);

            //Save the last time a notification was fired.
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctxt);
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(NotificationUtils.KEY_LATEST_NOT, System.currentTimeMillis());
            editor.apply();
        } else {
            backgroundTask.cancel(true);
        }
    }
}