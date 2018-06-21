package com.example.android.medjour.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface JournalDAO {

    @Query("SELECT * FROM journal")
    public List<JournalEntry> getAllEntries();

    @Insert
    void createEntry (JournalEntry journalEntry);

    @Update (onConflict = OnConflictStrategy.REPLACE)
    void updateEntry (JournalEntry journalEntry);

    @Delete
    void deleteEntry (JournalEntry journalEntry);


}
