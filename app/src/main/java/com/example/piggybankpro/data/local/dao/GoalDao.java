package com.example.piggybankpro.data.local.dao;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.data.local.entities.Id;

import java.util.List;

@Dao
public interface GoalDao {

    // ========== CRUD операции ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GoalEntity goal);

    @Update
    void update(GoalEntity goal);

    @Delete
    void delete(GoalEntity goal);

    // ========== Транзакции с обновлением кэша ==========

    /**
     * Вставляет цель и автоматически обновляет кэшированные суммы у родителей
     */
    @Transaction
    default void insertWithParentUpdate(GoalEntity goal) {
        insert(goal);

        updateCalculatedAmountForGoal(goal.getId());

        updateCalculatedAmountForAncestors(goal.getParentId());

        updatePositions(goal.getParentId());
    }

    default void updateCalculatedAmountForAncestors(Id id) {
        while (id != null) {
            updateCalculatedAmountForAncestor(id);
            id = getGoalByIdSync(id).getParentId();
        }
    }

    default void updatePositions(Id parentId) {
        var goals = parentId == null ? getRootGoalsSync() : getSubGoalsSync(parentId);
        for (int i = 0; i < goals.size(); ++i) {
            var goal = goals.get(i);
            if (goal.getOrderPosition() != i) {
                goal.setOrderPosition(i);
                update(goal);
            }
        }

    }

    @Transaction
    default void updateWithParentUpdate(GoalEntity goal) {
        GoalEntity oldGoal = getGoalByIdSync(goal.getId());
        var oldParentId = oldGoal.getParentId();
        var newParentId = goal.getParentId();
        double oldCurrentAmount = oldGoal.getCurrentAmount();
        boolean needToUpdate = oldCurrentAmount != goal.getCurrentAmount();

        update(goal);

        if (oldParentId != null && !oldParentId.equals(newParentId)) {
            updateCalculatedAmountForAncestors(oldParentId);
        }

        updateCalculatedAmountForGoal(goal.getId());
        if (needToUpdate || newParentId != null && !newParentId.equals(oldParentId)) {
            updateCalculatedAmountForAncestors(newParentId);
        }

        updatePositions(oldParentId);
        updatePositions(newParentId);
    }

    /**
     * Удаляет цель и обновляет кэшированные суммы у родителя
     */
    @Transaction
    default void deleteWithParentUpdate(GoalEntity goal) {
        var parentId = goal.getParentId();

        delete(goal);

        Log.d("GoalsUpdate", "Delete with parent id " + parentId);

        updateCalculatedAmountForAncestors(parentId);
        updatePositions(parentId);
    }

    // ========== Методы для обновления кэша ==========

    /**
     * Обновляет calculated_amount для конкретной цели
     * (current_amount + сумма calculated_amount всех прямых потомков)
     */
    @Query("UPDATE goals " +
            "SET calculated_amount = " +
            "    COALESCE(current_amount, 0) + " +
            "    COALESCE((SELECT SUM(calculated_amount) FROM goals AS children WHERE children.parent_id = goals.id), 0) " +
            "WHERE id = :goalId")
    void updateCalculatedAmountForGoal(Id goalId);

    @Query("UPDATE goals " +
            "SET calculated_amount = " +
            "    COALESCE(current_amount, 0) + " +
            "    COALESCE((SELECT SUM(calculated_amount) FROM goals AS children WHERE children.parent_id = goals.id), 0) " +
            "WHERE id = :goalId")
    void updateCalculatedAmountForAncestor(Id goalId);

    @Query("SELECT * FROM goals ORDER BY created_at DESC")
    LiveData<List<GoalEntity>> getAllGoals();

    @Query("SELECT * FROM goals WHERE id = :goalId")
    LiveData<GoalEntity> getGoalById(Id goalId);

    @Query("SELECT * FROM goals WHERE id = :goalId")
    GoalEntity getGoalByIdSync(Id goalId);

    @Query("SELECT * FROM goals WHERE parent_id IS NULL ORDER BY order_position, created_at DESC")
    LiveData<List<GoalEntity>> getRootGoals();

    @Query("SELECT * FROM goals WHERE parent_id = :parentId ORDER BY order_position, created_at DESC")
    LiveData<List<GoalEntity>> getSubGoals(Id parentId);

    @Query("SELECT * FROM goals WHERE parent_id IS NULL ORDER BY order_position, created_at DESC")
    List<GoalEntity> getRootGoalsSync();

    @Query("SELECT * FROM goals WHERE parent_id = :parentId ORDER BY order_position, created_at DESC")
    List<GoalEntity> getSubGoalsSync(Id parentId);

    @Query("SELECT SUM(calculated_amount) FROM goals WHERE parent_id = :parentId")
    LiveData<Double> getTotalSavedAmount(Id parentId);

    @Query("SELECT SUM(calculated_amount) FROM goals WHERE parent_id IS NULL")
    LiveData<Double> getRootTotalSavedAmount();
}