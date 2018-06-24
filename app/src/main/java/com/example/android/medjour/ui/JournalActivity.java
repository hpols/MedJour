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
import com.example.android.medjour.model.JournalViewModel;
import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.model.data.JournalEntry;

import java.util.List;

import timber.log.Timber;

public class JournalActivity extends AppCompatActivity {

    ActivityJournalBinding journalBinder;
    JournalDb dB;
    JournalAdapter journalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        journalBinder = DataBindingUtil.setContentView(this, R.layout.activity_journal);
        setContentView(R.layout.activity_journal);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        dB = JournalDb.getInstance(getApplicationContext());

        //setup viewModel
        JournalViewModel viewModel = ViewModelProviders.of(this).get(JournalViewModel.class);
        viewModel.getJournalEntries().observe(this, new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(@Nullable List<JournalEntry> journalEntries) {
                Timber.d("Updating entries from LiveData in ViewModel");
                journalAdapter.setJournalEntries(journalEntries);
            }
        });

        //setup adapter and recyclerview
        journalAdapter = new JournalAdapter();
        journalBinder.journalRv.setAdapter(journalAdapter);
        journalBinder.journalRv.setLayoutManager(new LinearLayoutManager(this));
        journalAdapter.notifyDataSetChanged();
    }
}
