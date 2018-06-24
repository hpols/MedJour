package com.example.android.medjour.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.databinding.ActivityOverviewBinding;
import com.example.android.medjour.model.EntryExecutor;
import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.utils.JournalUtils;
import com.facebook.stetho.Stetho;

import timber.log.Timber;

public class OverviewActivity extends AppCompatActivity {

    ActivityOverviewBinding overviewBinder;
    JournalDb dB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overviewBinder = DataBindingUtil.setContentView(this, R.layout.activity_overview);

        //integrating stetho for debugging
        Stetho.initializeWithDefaults(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        dB = JournalDb.getInstance(getApplicationContext());

        overviewBinder.mainJournalBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent journalIntent = new Intent(OverviewActivity.this,
                        JournalActivity.class);
                startActivity(journalIntent);
            }
        });

        overviewBinder.mainEntryBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newEntryIntent = new Intent(OverviewActivity.this,
                        NewEntryActivity.class);
                startActivity(newEntryIntent);
            }
        });

        EntryExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                long prepTime = dB.journalDao().getTotalPrepTime();
                Timber.v("prep: " + prepTime);
                long medTime = dB.journalDao().getTotalMedTime();
                Timber.v("med: " + medTime);
                long revTime = dB.journalDao().getTotalRevTime();
                Timber.v("rev: " + revTime);

                long cumulativeTime = prepTime + medTime + revTime;

                String cumulativetv;
                if (cumulativeTime == 0) {
                    cumulativetv= getString(R.string.overview_no_entries);
                    overviewBinder.mainJournalBt.setVisibility(View.GONE);
                } else {
                    cumulativetv = "Cumulative Time: " + JournalUtils.toMinutes(cumulativeTime);
                    overviewBinder.mainJournalBt.setVisibility(View.VISIBLE);
                }

                overviewBinder.mainCumulativeTv.setText(cumulativetv);
            }
        });
    }
}