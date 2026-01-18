package com.example.piggybankpro.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.piggybankpro.data.local.entities.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TransactionEntity transaction);

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    void delete(String transactionId);

    @Query("SELECT * FROM transactions WHERE goal_id = :goalId ORDER BY transaction_date DESC")
    LiveData<List<TransactionEntity>> getTransactionsByGoalId(String goalId);
}