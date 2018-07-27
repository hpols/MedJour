package com.example.android.medjour.model.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import timber.log.Timber;

@Database(entities = {JournalEntry.class}, version = 1, exportSchema = false)
public abstract class JournalDb extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DB_NAME = "journal";
    private static JournalDb instance;

    public static JournalDb getInstance(Context ctxt) {
        if (instance == null) {
            synchronized (LOCK) {
                Timber.d("creating new DB instance");
                instance = Room.databaseBuilder(ctxt.getApplicationContext(), JournalDb.class,
                        JournalDb.DB_NAME).build();
            }
        }
        Timber.d("getting DB instance");
        return instance;
    }

    public abstract JournalDao journalDao();
}
