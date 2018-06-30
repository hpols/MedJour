package com.example.android.medjour.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.databinding.ActivityOverviewBinding;
import com.example.android.medjour.model.EntryExecutor;
import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.settings.SettingsActivity;
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
                String cumulativetv;
                if (JournalUtils.getCumulativeTime(dB) == 0) {
                    cumulativetv = getString(R.string.overview_no_entries);
                    overviewBinder.mainJournalBt.setVisibility(View.GONE);
                } else {
                    cumulativetv = "Cumulative Time: "
                            + JournalUtils.toMinutes(JournalUtils.getCumulativeTime(dB));
                    overviewBinder.mainJournalBt.setVisibility(View.VISIBLE);
                }

                overviewBinder.mainCumulativeTv.setText(cumulativetv);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.menu_activation:
                //TODO: create activation dialogue
                break;
            case R.id.menu_guidelines:
                //TODO: go to guidelines
        }
        return super.onOptionsItemSelected(item);
    }
}