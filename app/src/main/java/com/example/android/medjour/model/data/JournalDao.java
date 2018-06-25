package com.example.android.medjour.model.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface JournalDao {

    @Query("SELECT * FROM journal")
    LiveData<List<JournalEntry>> getAllEntries();

    @Query("SELECT SUM(prepTime) FROM journal")
    long getTotalPrepTime();

    @Query("SELECT SUM(medTime) FROM journal")
    long getTotalMedTime();

    @Query("SELECT SUM(revTime) FROM journal")
    long getTotalRevTime();

    @Insert
    void createEntry (JournalEntry journalEntry);

    @Update (onConflict = OnConflictStrategy.REPLACE)
    void updateEntry (JournalEntry journalEntry);

    @Delete
    void deleteEntry (JournalEntry journalEntry);

}
