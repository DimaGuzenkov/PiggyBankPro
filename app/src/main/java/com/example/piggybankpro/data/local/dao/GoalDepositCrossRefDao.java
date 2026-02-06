package com.example.piggybankpro.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.data.local.entities.Id;

import java.util.List;

@Dao
public interface GoalDepositCrossRefDao {

    // CRUD операции
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GoalDepositCrossRefEntity crossRef);

    @Delete
    void delete(GoalDepositCrossRefEntity crossRef);

    @Query("DELETE FROM goal_deposit_cross_ref WHERE goal_id = :goalId AND auto_deposit_id = :depositId")
    void delete(Id goalId, Id depositId);

    @Query("DELETE FROM goal_deposit_cross_ref WHERE auto_deposit_id = :depositId")
    void deleteByDepositId(Id depositId);

    @Query("SELECT * FROM goal_deposit_cross_ref WHERE auto_deposit_id = :depositId")
    List<GoalDepositCrossRefEntity> getCrossRefsByDepositIdSync(Id depositId);

    // Получение данных
    @Query("SELECT * FROM goal_deposit_cross_ref WHERE auto_deposit_id = :depositId")
    LiveData<List<GoalDepositCrossRefEntity>> getCrossRefsByDepositId(Id depositId);
}
