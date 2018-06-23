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
import com.example.android.medjour.model.JournalDb;
import com.example.android.medjour.utils.UiUtils;
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
                long prepTime = dB.journalDao().getTotalPrep();
                Timber.v("prep: " + prepTime);
                long medTime = dB.journalDao().getTotalMed();
                Timber.v("med: " + medTime);
                long revTime = dB.journalDao().getTotalRev();
                Timber.v("rev: " + revTime);

                long cumulativeTime = prepTime + medTime + revTime;

                String cumulativeTV = null;
                if (cumulativeTime == 0) {
                    cumulativeTV= getString(R.string.overview_no_entries);
                } else {
                    cumulativeTV = "Cumulative Time: " + UiUtils.toMinutes(cumulativeTime);
                }


                overviewBinder.mainCumulativeTv.setText(cumulativeTV);
            }
        });

    }
}
