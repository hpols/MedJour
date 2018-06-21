package com.example.android.medjour.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import java.util.Date;

import timber.log.Timber;

@Database(entities = {JournalEntry.class}, version = 1, exportSchema = false)
@TypeConverters(JournalDB.DateConverter.class)
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

    class DateConverter {
        @TypeConverter
        public Date toDate(Long timeStamp) {
            return timeStamp == null ? null : new Date(timeStamp);
        }

        @TypeConverter
        public Long toTimeStamp(Date date) {
            return date == null ? null : date.getTime();
        }
    }
}
