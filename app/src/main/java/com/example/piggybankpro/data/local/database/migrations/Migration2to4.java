package com.example.piggybankpro.data.local.database.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration2to4 extends Migration {

    private final String create = "id TEXT PRIMARY KEY NOT NULL, " +
            "title TEXT NOT NULL DEFAULT 'Новая цель', " +
            "description TEXT, " +
            "target_amount REAL, " +
            "current_amount REAL NOT NULL DEFAULT 0.0, " +
            "calculated_amount REAL NOT NULL DEFAULT 0.0, " +
            "currency TEXT DEFAULT 'RUB', " +
            "target_date INTEGER, " +
            "created_at INTEGER, " +
            "updated_at INTEGER, " +
            "color INTEGER, " +
            "goal_url TEXT, " +
            "parent_id TEXT, " +
            "order_position INTEGER NOT NULL DEFAULT 0, " +
            "is_completed INTEGER DEFAULT 0, " +
            "completed_date INTEGER";

    private final String insert =
            "id, " +
            "title, " +
            "description, " +
            "target_amount, " +
            "current_amount, " +
            "calculated_amount, " +
            "currency, " +
            "target_date, " +
            "created_at, " +
            "updated_at, " +
            "color, " +
            "goal_url, " +
            "CASE WHEN parent_id='' THEN NULL ELSE parent_id END, " +
            "order_position, " +
            "is_completed, " +
            "completed_date ";

    public Migration2to4() {
        super(5, 6);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS goals_temp (" + create + ")");

            database.execSQL("INSERT INTO goals_temp (" +
                    "id, title, description, target_amount, current_amount, calculated_amount, " +
                    "currency, target_date, created_at, updated_at, color, goal_url, " +
                    "parent_id, order_position, is_completed, completed_date) " +
                    "SELECT " + insert +
                    "FROM goals");

            database.execSQL("DROP TABLE IF EXISTS goals");

            database.execSQL("CREATE TABLE IF NOT EXISTS goals (" + create + ", " +
                    "FOREIGN KEY(parent_id) REFERENCES goals(id) ON UPDATE CASCADE ON DELETE CASCADE)");

            database.execSQL("INSERT INTO goals SELECT * FROM goals_temp");

            database.execSQL("DROP TABLE IF EXISTS goals_temp");

            database.execSQL("CREATE INDEX IF NOT EXISTS index_goals_parent_id ON goals (parent_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_goals_target_date ON goals (target_date)");
        } catch (Exception e) {
            throw new RuntimeException("Migration failed", e);
        }
    }
}