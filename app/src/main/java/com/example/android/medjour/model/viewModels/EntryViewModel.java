package com.example.android.medjour.model.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.model.data.JournalEntry;

public class EntryViewModel extends ViewModel {
    private LiveData<JournalEntry> entry;

    public EntryViewModel(JournalDb dB, int entryId) {
        entry = dB.journalDao().getEntry(entryId);
    }

    public LiveData<JournalEntry> getTask() {
        return entry;
    }
}
