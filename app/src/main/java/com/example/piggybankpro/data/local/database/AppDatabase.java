package com.example.piggybankpro.data.local.database;

import androidx.room.AutoMigration;
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
        version = 2,
        exportSchema = true,
        autoMigrations = {
                @AutoMigration(from = 1, to = 2)
        }
)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract GoalDao goalDao();
    public abstract AutoDepositDao autoDepositDao();
    public abstract GoalDepositCrossRefDao goalDepositCrossRefDao();
    public abstract TransactionDao transactionDao();
}