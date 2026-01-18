package com.example.piggybankpro.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.piggybankpro.data.local.database.AppDatabase;
import com.example.piggybankpro.data.local.database.DatabaseClient;
import com.example.piggybankpro.data.local.dao.TransactionDao;
import com.example.piggybankpro.data.local.entities.TransactionEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final Executor executor;

    public TransactionRepository(Application application) {
        AppDatabase database = DatabaseClient.getInstance(application).getAppDatabase();
        transactionDao = database.transactionDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void insert(TransactionEntity transaction) {
        executor.execute(() -> {
            transactionDao.insert(transaction);
        });
    }

    public void delete(String transactionId) {
        executor.execute(() -> {
            transactionDao.delete(transactionId);
        });
    }

    // Фильтрация
    public LiveData<List<TransactionEntity>> getTransactionsByGoalId(String goalId) {
        return transactionDao.getTransactionsByGoalId(goalId);
    }
}