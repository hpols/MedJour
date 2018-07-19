package com.example.android.medjour.notification;

import android.content.Context;
import android.os.AsyncTask;

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
                NotificationTask.executeTask(ctxt, NotificationTask.ACTION_NOTIFICATION);

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
}