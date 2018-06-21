package com.example.android.medjour.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "journal")
public class JournalEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private Date date;
    private long prepTime;
    private long medTime;
    private long revTime;
    private String assessment;

    //constructor without ID
    @Ignore
    public JournalEntry(Date date, long prepTime, long medTime, long revTime, String assessment) {
        this.date = date;
        this.prepTime = prepTime;
        this.medTime = medTime;
        this.revTime = revTime;
        this.assessment = assessment;
    }

    //constructor with ID for Room
    public JournalEntry(int id, Date date, long prepTime, long medTime, long revTime, String assessment) {
        this.id = id;
        this.date = date;
        this.prepTime = prepTime;
        this.medTime = medTime;
        this.revTime = revTime;
        this.assessment = assessment;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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
