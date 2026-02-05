package com.example.piggybankpro.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.piggybankpro.data.local.database.AppDatabase;
import com.example.piggybankpro.data.local.database.DatabaseClient;
import com.example.piggybankpro.data.local.dao.AutoDepositDao;
import com.example.piggybankpro.data.local.dao.GoalDao;
import com.example.piggybankpro.data.local.dao.GoalDepositCrossRefDao;
import com.example.piggybankpro.data.local.dao.TransactionDao;
import com.example.piggybankpro.data.local.entities.AutoDepositEntity;
import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.data.local.entities.TransactionEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AutoDepositRepository {

    private final AutoDepositDao autoDepositDao;
    private final GoalDao goalDao;
    private final GoalDepositCrossRefDao crossRefDao;
    private final TransactionDao transactionDao;
    private final Executor executor;

    public AutoDepositRepository(Application application) {
        AppDatabase database = DatabaseClient.getInstance(application).getAppDatabase();
        autoDepositDao = database.autoDepositDao();
        goalDao = database.goalDao();
        crossRefDao = database.goalDepositCrossRefDao();
        transactionDao = database.transactionDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<AutoDepositEntity>> getAllAutoDeposits() {
        return autoDepositDao.getAllAutoDeposits();
    }

    public LiveData<AutoDepositEntity> getAutoDepositById(String id) {
        return autoDepositDao.getAutoDepositById(id);
    }

    public void insert(AutoDepositEntity autoDeposit, List<GoalDepositCrossRefEntity> crossRefs) {
        executor.execute(() -> {
            autoDepositDao.insert(autoDeposit);

            if (crossRefs != null && !crossRefs.isEmpty()) {
                for (GoalDepositCrossRefEntity crossRef : crossRefs) {
                    crossRef.setAutoDepositId(autoDeposit.getId());
                    crossRefDao.insert(crossRef);
                }
            }
        });
    }

    public void update(AutoDepositEntity autoDeposit, List<GoalDepositCrossRefEntity> crossRefs) {
        executor.execute(() -> {
            autoDepositDao.update(autoDeposit);

            crossRefDao.deleteByDepositId(autoDeposit.getId());

            if (crossRefs != null && !crossRefs.isEmpty()) {
                for (GoalDepositCrossRefEntity crossRef : crossRefs) {
                    crossRef.setAutoDepositId(autoDeposit.getId());
                    crossRefDao.insert(crossRef);
                }
            }
        });
    }

    public void delete(AutoDepositEntity autoDeposit) {
        executor.execute(() -> autoDepositDao.delete(autoDeposit));
    }

    public LiveData<List<GoalDepositCrossRefEntity>> getCrossRefsByDepositId(String depositId) {
        return crossRefDao.getCrossRefsByDepositId(depositId);
    }

    public void executeAutoDeposit(String depositId) {
        executor.execute(() -> {
            try {
                AutoDepositEntity deposit = autoDepositDao.getAutoDepositByIdSync(depositId);
                if (deposit == null) {
                    Log.e("AutoDeposit", "Автопополнение не найдено: " + depositId);
                    return;
                }

                if (!deposit.getIsActive()) {
                    return;
                }

                List<GoalDepositCrossRefEntity> crossRefs = crossRefDao.getCrossRefsByDepositIdSync(depositId);

                if (crossRefs == null || crossRefs.isEmpty()) {
                    return;
                }

                for (GoalDepositCrossRefEntity crossRef : crossRefs) {
                    double amountForGoal = crossRef.getAmount();

                    if (amountForGoal > 0) {
                        GoalEntity goal = goalDao.getGoalByIdSync(crossRef.getGoalId());
                        if (goal == null) {
                            continue;
                        }

                        double newAmount = goal.getCurrentAmount() + amountForGoal;
                        goal.setCurrentAmount(newAmount);
                        goalDao.updateWithParentUpdate(goal);

                        TransactionEntity transaction = new TransactionEntity(
                                crossRef.getGoalId(),
                                depositId,
                                deposit.getName(),
                                amountForGoal
                        );
                        transactionDao.insert(transaction);
                    }
                }

                deposit.calculateNextExecution();
                autoDepositDao.update(deposit);
            } catch (Exception e) {
                Log.e("AutoDeposit", "Ошибка при выполнении автопополнения: " + e.getMessage(), e);
            }
        });
    }

    public void executeDueAutoDeposits() {
        executor.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();

                List<AutoDepositEntity> dueDeposits = autoDepositDao.getDueAutoDepositsSync(currentTime);

                if (dueDeposits != null && !dueDeposits.isEmpty()) {
                    for (AutoDepositEntity deposit : dueDeposits) {
                        executeAutoDeposit(deposit.getId());
                    }
                }
            } catch (Exception e) {
                Log.e("AutoDeposit", "Ошибка при выполнении просроченных автопополнений: " + e.getMessage(), e);
            }
        });
    }

    public void setActive(String depositId, boolean isActive) {
        executor.execute(() -> {
            autoDepositDao.setActive(depositId, isActive);
        });
    }
}