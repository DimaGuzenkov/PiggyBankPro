package com.example.piggybankpro.data.local.converters;

import androidx.room.TypeConverter;

import com.example.piggybankpro.data.local.entities.Id;

public class IdConverter {
    @TypeConverter
    public static String fromIdToStr(Id id) {
        return id == null ? null : id.id();
    }

    @TypeConverter
    public static Id fromStrToId(String id) {
        return id == null ? null : new Id(id);
    }
}
