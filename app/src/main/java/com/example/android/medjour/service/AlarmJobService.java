package com.example.android.medjour.service;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import timber.log.Timber;

public class AlarmJobService extends JobService {

    private AsyncTask backgroundTask;

    @Override
    public boolean onStartJob(final JobParameters job) {
        backgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context ctxt = AlarmJobService.this;
                AlarmTask.executeTask(ctxt, AlarmTask.ACTION_ALARM);

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

        Timber.v("on Start Job called");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (backgroundTask != null) backgroundTask.cancel(true);

        Timber.v("on Stop Job called");
        return false;
    }
}