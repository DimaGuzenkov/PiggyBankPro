package com.example.piggybankpro.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.piggybankpro.data.local.converters.DateConverter;
import com.example.piggybankpro.data.local.dao.AutoDepositDao;
import com.example.piggybankpro.data.local.dao.GoalDao;
import com.example.piggybankpro.data.local.dao.GoalDepositCrossRefDao;
import com.example.piggybankpro.data.local.dao.TransactionDao;
import com.example.piggybankpro.data.local.entities.AutoDepositEntity;
import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.data.local.entities.TransactionEntity;

@Database(
        entities = {
                GoalEntity.class,
                AutoDepositEntity.class,
                GoalDepositCrossRefEntity.class,
                TransactionEntity.class
        },
        version = 6,
        exportSchema = true
//        autoMigrations = {
//                @AutoMigration(from = 2, to = 3)
//        }
)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

//    private static final Migration MIGRATION_2_3 = new Migration2to4();
//
//    public static AppDatabase create(Context context) {
//        Log.d("Database", "Start");
//        return Room.databaseBuilder(
//                        context.getApplicationContext(),
//                        AppDatabase.class,
//                        "piggybankpro-database"
//                )
//                .addMigrations(MIGRATION_2_3)
//                .addCallback(new RoomDatabase.Callback() {
//                    @Override
//                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
//                        super.onCreate(db);
//                        Log.d("Database", "Database created");
//                    }
//
//                    @Override
//                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
//                        super.onOpen(db);
//                        Log.d("Database", "Database opened");
//                    }
//                })
//                .build();
//    }

    public abstract GoalDao goalDao();
    public abstract AutoDepositDao autoDepositDao();
    public abstract GoalDepositCrossRefDao goalDepositCrossRefDao();
    public abstract TransactionDao transactionDao();
}