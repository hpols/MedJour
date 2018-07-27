package com.example.android.medjour.model.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "journal")
public class JournalEntry {

    public final static String PREP_TIME = "prepTime";
    public final static String MED_TIME = "medTime";
    public final static String REV_TIME = "revTime";
    public final static String ENTRY_DATE = "date";

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = ENTRY_DATE)
    private String date;
    @ColumnInfo(name = PREP_TIME)
    private long prepTime;
    @ColumnInfo(name = MED_TIME)
    private long medTime;
    @ColumnInfo(name = REV_TIME)
    private long revTime;
    private String assessment;

    //constructor without ID
    @Ignore
    public JournalEntry(String date, long prepTime, long medTime, long revTime, String assessment) {
        this.date = date;
        this.prepTime = prepTime;
        this.medTime = medTime;
        this.revTime = revTime;
        this.assessment = assessment;
    }

    //constructor with ID for Room
    public JournalEntry(int id, String date, long prepTime, long medTime, long revTime, String assessment) {
        this.id = id;
        this.date = date;
        this.prepTime = prepTime;
        this.medTime = medTime;
        this.revTime = revTime;
        this.assessment = assessment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(long prepTime) {
        this.prepTime = prepTime;
    }

    public long getMedTime() {
        return medTime;
    }

    public void setMedTime(long medTime) {
        this.medTime = medTime;
    }

    public long getRevTime() {
        return revTime;
    }

    public void setRevTime(long revTime) {
        this.revTime = revTime;
    }

    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(String assessment) {
        this.assessment = assessment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
