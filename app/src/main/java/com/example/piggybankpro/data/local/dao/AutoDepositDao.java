package com.example.piggybankpro.data.local.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Relation;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.piggybankpro.data.local.entities.AutoDepositEntity;
import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.data.local.entities.Id;

import java.util.List;

@Dao
public interface AutoDepositDao {

    // CRUD операции
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AutoDepositEntity autoDeposit);

    @Update
    void update(AutoDepositEntity autoDeposit);

    @Delete
    void delete(AutoDepositEntity autoDeposit);

    // Получение данных
    @Query("SELECT * FROM auto_deposits ORDER BY created_at DESC")
    LiveData<List<AutoDepositEntity>> getAllAutoDeposits();

    @Query("SELECT * FROM auto_deposits WHERE id = :depositId")
    AutoDepositEntity getAutoDepositByIdSync(Id depositId);

    @Query("SELECT * FROM auto_deposits WHERE id = :depositId")
    LiveData<AutoDepositEntity> getAutoDepositById(Id depositId);

    @Query("SELECT * FROM auto_deposits WHERE is_active = 1 AND next_execution_date <= :currentTime")
    List<AutoDepositEntity> getDueAutoDepositsSync(long currentTime);

    // Обновление статусов
    @Query("UPDATE auto_deposits SET is_active = :isActive WHERE id = :depositId")
    void setActive(Id depositId, boolean isActive);
}
