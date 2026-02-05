package com.example.piggybankpro.data.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.piggybankpro.data.local.database.AppDatabase;
import com.example.piggybankpro.data.local.database.DatabaseClient;
import com.example.piggybankpro.data.local.dao.GoalDao;
import com.example.piggybankpro.data.local.dao.TransactionDao;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.data.local.entities.TransactionEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GoalRepository {

    private final GoalDao goalDao;
    private final TransactionDao transactionDao;
    private final LiveData<List<GoalEntity>> allGoals;
    private final Executor executor;

    public GoalRepository(Application application) {
        AppDatabase database = DatabaseClient.getInstance(application).getAppDatabase();
        goalDao = database.goalDao();
        transactionDao = database.transactionDao();
        allGoals = goalDao.getAllGoals();
        executor = Executors.newSingleThreadExecutor();
    }

    // Основные операции
    public LiveData<List<GoalEntity>> getAllGoals() {
        return allGoals;
    }

    public LiveData<GoalEntity> getGoalById(String goalId) {
        return goalDao.getGoalById(goalId);
    }

    public GoalEntity getGoalByIdSync(String goalId) {
        return goalDao.getGoalByIdSync(goalId);
    }

    public void insert(GoalEntity goal) {
        executor.execute(() -> {
            goalDao.insertWithParentUpdate(goal);
            if (goal.getCurrentAmount() > 0) {
                TransactionEntity transaction = new TransactionEntity(
                        goal.getId(),
                        goal.getCurrentAmount(),
                        "Начальная сумма",
                        TransactionEntity.TYPE_DEPOSIT
                );
                transactionDao.insert(transaction);
            }
        });
    }

    public void update(GoalEntity goal) {
        executor.execute(() -> goalDao.updateWithParentUpdate(goal));
    }

    public void delete(GoalEntity goal) {
        executor.execute(() -> {
            transactionDao.deleteByGoalId(goal.getId());
            goalDao.deleteWithParentUpdate(goal);
        });
    }

    public void deposit(String goalId, Double amount, String description) {
        executor.execute(() -> {
            GoalEntity goal = goalDao.getGoalByIdSync(goalId);

            if (goal == null) {
                return;
            }

            goal.setCurrentAmount(goal.getCurrentAmount() + amount);
            goalDao.updateWithParentUpdate(goal);

            TransactionEntity transaction = new TransactionEntity(
                    goalId,
                    amount,
                    description,
                    TransactionEntity.TYPE_DEPOSIT
            );
            transactionDao.insert(transaction);
        });
    }

    public void withdraw(String goalId, Double amount, String description) {
        executor.execute(() -> {
            try {
                GoalEntity goal = goalDao.getGoalByIdSync(goalId);

                if (goal == null) {
                    return;
                }

                if (goal.getCurrentAmount() < amount) {
                    return;
                }

                goal.setCurrentAmount(goal.getCurrentAmount() - amount);
                goalDao.updateWithParentUpdate(goal);

                TransactionEntity transaction = new TransactionEntity(
                        goalId,
                        -amount,
                        description,
                        TransactionEntity.TYPE_WITHDRAWAL
                );
                transactionDao.insert(transaction);
            } catch (Exception ignored) {
            }
        });
    }

    public void transfer(String sourceGoalId, String destGoalId, double amount, String description) {
        executor.execute(() -> {
            try {
                GoalEntity sourceGoal = goalDao.getGoalByIdSync(sourceGoalId);
                GoalEntity destGoal = goalDao.getGoalByIdSync(destGoalId);

                if (sourceGoal == null || destGoal == null) {
                    return;
                }

                if (sourceGoal.getCurrentAmount() < amount) {
                    return;
                }

                sourceGoal.setCurrentAmount(sourceGoal.getCurrentAmount() - amount);
                goalDao.updateWithParentUpdate(sourceGoal);

                destGoal.setCurrentAmount(destGoal.getCurrentAmount() + amount);
                goalDao.updateWithParentUpdate(destGoal);

                TransactionEntity sourceTransaction = new TransactionEntity(
                        sourceGoalId,
                        destGoal.getTitle(),
                        -amount,
                        description
                );

                TransactionEntity destTransaction = new TransactionEntity(
                        destGoalId,
                        sourceGoal.getTitle(),
                        amount,
                        description
                );
                transactionDao.insert(sourceTransaction);
                transactionDao.insert(destTransaction);
            } catch (Exception ignored) {
            }
        });
    }

    public void updatePositions(List<GoalEntity> goals) {
        for (int i = 0; i < goals.size(); ++i) {
            var goal = goals.get(i);
            if (goal.getOrderPosition() != i) {
                goal.setOrderPosition(i);
                update(goal);
            }
        }
    }

    public LiveData<List<GoalEntity>> getSubGoals(String parentId) {
        if (parentId == null) {
            return goalDao.getRootGoals();
        }
        return goalDao.getSubGoals(parentId);
    }

    public LiveData<Double> getTotalSavedAmount(String parentId) {
        if (parentId == null) {
            return goalDao.getRootTotalSavedAmount();
        }
        return goalDao.getTotalSavedAmount(parentId);
    }
}