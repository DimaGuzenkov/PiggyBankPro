package com.example.piggybankpro.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.piggybankpro.data.local.database.migrations.Migration2to4;

public class DatabaseClient {

    private static DatabaseClient instance;
    private final AppDatabase appDatabase;
    private static final Migration MIGRATION_2_4 = new Migration2to4();

    private DatabaseClient(Context context) {
        appDatabase = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "savings_goals_db"
                )
//                .addMigrations(MIGRATION_2_4)  // Используем статическую миграцию
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        android.util.Log.d("Database", "Database created");
                    }

                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        super.onOpen(db);
                        android.util.Log.d("Database", "Database opened");
                    }
                })
                .build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseClient(context);
        }
        return instance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
