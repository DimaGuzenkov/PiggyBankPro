package com.example.piggybankpro.data.local.entities;

import androidx.annotation.NonNull;

import java.util.UUID;

public class Id {
    private final String id;

    public Id(String id) {
        this.id = id;
    }

    public Id() {
        id = UUID.randomUUID().toString();
    }

    @NonNull
    @Override
    public String toString() {
        return id;
    }
}
