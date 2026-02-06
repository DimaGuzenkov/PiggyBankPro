package com.example.piggybankpro.data.local.entities;

import androidx.annotation.NonNull;

import java.util.UUID;

public record Id(@NonNull String id) {
    public Id(String id) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
    }

    public Id() { this(null); }

    @NonNull
    public static Id createIfNull(Id id) {
        return id == null ? new Id() : id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id = (Id) o;
        return this.id.equals(id.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
