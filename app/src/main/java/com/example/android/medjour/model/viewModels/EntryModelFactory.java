package com.example.android.medjour.model.viewModels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.android.medjour.model.data.JournalDb;

public class EntryModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final JournalDb Db;
    private final int entryId;

    public EntryModelFactory(JournalDb database, int entryId) {
        this.Db= database;
        this.entryId = entryId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EntryViewModel(Db, entryId);
    }
}
