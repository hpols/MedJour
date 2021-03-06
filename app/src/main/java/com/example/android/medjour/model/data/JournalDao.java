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

    //––– CREATE Methods –––//

    @Insert
    void createEntry(JournalEntry journalEntry);

    //––– READ Methods –––//

    @Query("SELECT * FROM journal")
    LiveData<List<JournalEntry>> getAllEntries();

    @Query("SELECT date FROM journal ORDER BY date DESC LIMIT 1")
    String getLastEntryDate();

    //––– UPDATE Methods –––//

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateEntry(JournalEntry journalEntry);

    // –––DELETE METHODS –––//
    @Delete
    void deleteEntry(JournalEntry journalEntry);
}
