package com.example.android.medjour.notification;

import android.content.Context;
import android.os.AsyncTask;
import android.text.format.DateUtils;

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
    public void fireNotification(Context ctxt) {
        long timeSinceLastNotification = NotificationUtils.timeSinceLastNot(ctxt);

        boolean dayPassedSinceLastNot = false;

        if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
            dayPassedSinceLastNot = true;
        }

        if (dayPassedSinceLastNot) {
            NotificationTask.executeTask(ctxt, NotificationTask.ACTION_NOTIFICATION);
        } else {
            backgroundTask.cancel(true);
        }
    }
}