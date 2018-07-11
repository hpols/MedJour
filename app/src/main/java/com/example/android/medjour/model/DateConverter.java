package com.example.android.medjour.model;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timeStamp) {
        return timeStamp == null ? null : new Date(timeStamp);
    }

    @TypeConverter
    public Long toTimeStamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
