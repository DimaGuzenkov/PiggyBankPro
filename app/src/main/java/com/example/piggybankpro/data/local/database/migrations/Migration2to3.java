package com.example.piggybankpro.data.local.database.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration2to3 extends Migration {

    public Migration2to3() {
        super(2, 3);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        try {
            // 1. Создаем временную таблицу с новой структурой
            database.execSQL("CREATE TABLE goals_new (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
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
                    "parent_id TEXT NOT NULL DEFAULT '', " +
                    "order_position INTEGER NOT NULL DEFAULT 0, " +
                    "is_completed INTEGER NOT NULL DEFAULT 0, " +
                    "completed_date INTEGER, " +
                    "FOREIGN KEY(parent_id) REFERENCES goals_new(id) ON UPDATE CASCADE ON DELETE CASCADE)");

            // 2. Копируем данные из старой таблицы, преобразуя parent_id NULL в пустую строку
            database.execSQL("INSERT INTO goals_new (" +
                    "id, title, description, target_amount, current_amount, calculated_amount, " +
                    "currency, target_date, created_at, updated_at, color, goal_url, " +
                    "parent_id, order_position, is_completed, completed_date) " +
                    "SELECT " +
                    "id, " +
                    "CASE WHEN title IS NULL THEN 'Новая цель' ELSE title END, " +
                    "description, " +
                    "target_amount, " +
                    "COALESCE(current_amount, 0.0), " +
                    "COALESCE(calculated_amount, 0.0), " +
                    "COALESCE(currency, 'RUB'), " +
                    "target_date, " +
                    "created_at, " +
                    "updated_at, " +
                    "color, " +
                    "goal_url, " +
                    "CASE WHEN parent_id IS NULL THEN '' ELSE parent_id END, " +
                    "COALESCE(order_position, 0), " +
                    "COALESCE(is_completed, 0), " +
                    "completed_date, " +
                    "FROM goals");

            // 3. Удаляем старую таблицу
            database.execSQL("DROP TABLE goals");

            // 4. Переименовываем новую таблицу
            database.execSQL("ALTER TABLE goals_new RENAME TO goals");

            // 5. Создаем индексы
            database.execSQL("CREATE INDEX index_goals_parent_id ON goals (parent_id)");
            database.execSQL("CREATE INDEX index_goals_target_date ON goals (target_date)");
//            database.execSQL("CREATE INDEX index_goals_parent_id_order_position ON goals (parent_id, order_position)");

            // 6. Исправляем порядок позиций для каждой группы parent_id
//            fixOrderPositions(database);

        } catch (Exception e) {
            throw new RuntimeException("Migration failed", e);
        }
    }

//    private void fixOrderPositions(SupportSQLiteDatabase database) {
//        // Получаем все уникальные parent_id (включая пустую строку для корневых целей)
//        database.query("SELECT DISTINCT parent_id FROM goals").use(cursor -> {
//            while (cursor.moveToNext()) {
//                String parentId = cursor.getString(0);
//                // Исправляем порядок для каждой группы parent_id
//                fixOrderForParent(database, parentId);
//            }
//        });
//    }
//
//    private void fixOrderForParent(SupportSQLiteDatabase database, String parentId) {
//        // Получаем все цели для данного parent_id, отсортированные по текущему order_position и created_at
//        String query = "SELECT id FROM goals WHERE parent_id " +
//                (parentId == null || parentId.isEmpty() ?
//                        "IS NULL OR parent_id = ''" : "= ?") +
//                " ORDER BY order_position, created_at DESC";
//
//        if (parentId == null || parentId.isEmpty()) {
//            database.query(query).use(cursor -> {
//                int position = 0;
//                while (cursor.moveToNext()) {
//                    String goalId = cursor.getString(0);
//                    // Обновляем позицию
//                    database.execSQL("UPDATE goals SET order_position = ? WHERE id = ?",
//                            new Object[]{position++, goalId});
//                }
//            });
//        } else {
//            database.query(query, new Object[]{parentId}).use(cursor -> {
//                int position = 0;
//                while (cursor.moveToNext()) {
//                    String goalId = cursor.getString(0);
//                    // Обновляем позицию
//                    database.execSQL("UPDATE goals SET order_position = ? WHERE id = ?",
//                            new Object[]{position++, goalId});
//                }
//            });
//        }
//    }
}