package com.example.android.medjour.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.adapter.JournalAdapter;
import com.example.android.medjour.databinding.ActivityJournalBinding;
import com.example.android.medjour.model.EntryExecutor;
import com.example.android.medjour.model.viewModels.JournalViewModel;
import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.model.data.JournalEntry;
import com.example.android.medjour.utils.JournalUtils;

import java.util.List;

import timber.log.Timber;

public class JournalActivity extends AppCompatActivity {

    static ActivityJournalBinding journalBinder;
    static JournalDb dB;
    JournalAdapter journalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        journalBinder = DataBindingUtil.setContentView(this, R.layout.activity_journal);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        dB = JournalDb.getInstance(getApplicationContext());

        //setup adapter and recyclerView
        journalBinder.journalRv.setLayoutManager(new LinearLayoutManager(this));
        journalAdapter = new JournalAdapter(this, dB);
        journalBinder.journalRv.setAdapter(journalAdapter);
        Timber.v("adapter and recyclerview setup");

        //setup viewModel
        JournalViewModel viewModel = ViewModelProviders.of(this).get(JournalViewModel.class);
        viewModel.getJournalEntries().observe(this, new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(@Nullable List<JournalEntry> journalEntries) {
                Timber.d("Updating entries from LiveData in ViewModel");
                journalAdapter.setJournalEntries(journalEntries);
            }
        });


        setTotalTime();

    }

    public static void setTotalTime() {
        EntryExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                String totalTime = JournalUtils.toMinutes(JournalUtils.getCumulativeTime(dB));
                journalBinder.journalAccTimeTv.setText(totalTime);
            }

        });
    }
}
