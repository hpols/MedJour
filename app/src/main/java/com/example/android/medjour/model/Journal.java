package com.example.android.medjour.model;

public class Journal {

    private long date;
    private long prepTime;
    private long medTime;
    private long revTime;
    private String assessment;


    public long getDate() {
        return date;
    }

    public void setDate(long date) {
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
}
