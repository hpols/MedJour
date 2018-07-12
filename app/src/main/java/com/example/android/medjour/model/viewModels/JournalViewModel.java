package com.example.android.medjour.model.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.model.data.JournalEntry;

import java.util.List;

import timber.log.Timber;

public class JournalViewModel extends AndroidViewModel {

    private LiveData<List<JournalEntry>> journalEntries;

    public JournalViewModel(@NonNull Application application) {
        super(application);
        JournalDb db = JournalDb.getInstance(this.getApplication());
        Timber.d("ViewModel is retrieving the entries from the db");
        journalEntries = db.journalDao().getAllEntries();
    }

    public LiveData<List<JournalEntry>> getJournalEntries() {
        return journalEntries;
    }
}
