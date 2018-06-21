package com.example.android.medjour.model;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import timber.log.Timber;

public abstract class JournalDB extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DB_NAME = "journal";
    private static JournalDB instance;

    public static JournalDB getInstance(Context ctxt) {
        if (instance == null) {
            synchronized (LOCK) {
                Timber.d("creating new DB instance");
                instance = Room.databaseBuilder(ctxt.getApplicationContext(), JournalDB.class,
                        JournalDB.DB_NAME).build();
            }
        }
        Timber.d("getting DB instance");
        return instance;
    }
}
