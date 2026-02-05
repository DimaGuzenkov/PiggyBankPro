package com.example.piggybankpro.data.local.converters;

import androidx.room.TypeConverter;

import com.example.piggybankpro.data.local.entities.Id;

public class IdConverter {
    @TypeConverter
    public String fromIdToStr(Id id) {
        return id == null ? null : id.toString();
    }

    @TypeConverter
    public Id fromStrToId(String id) {
        return id == null ? new Id() : new Id(id);
    }
}
