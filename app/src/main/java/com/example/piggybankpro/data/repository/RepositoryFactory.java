package com.example.piggybankpro.data.repository;

import android.app.Application;

public class RepositoryFactory {

    private static RepositoryFactory instance;
    private final Application application;

    private GoalRepository goalRepository;
    private AutoDepositRepository autoDepositRepository;
    private TransactionRepository transactionRepository;

    private RepositoryFactory(Application application) {
        this.application = application;
    }

    public static synchronized RepositoryFactory getInstance(Application application) {
        if (instance == null) {
            instance = new RepositoryFactory(application);
        }
        return instance;
    }

    public GoalRepository getGoalRepository() {
        if (goalRepository == null) {
            goalRepository = new GoalRepository(application);
        }
        return goalRepository;
    }

    public AutoDepositRepository getAutoDepositRepository() {
        if (autoDepositRepository == null) {
            autoDepositRepository = new AutoDepositRepository(application);
        }
        return autoDepositRepository;
    }

    public TransactionRepository getTransactionRepository() {
        if (transactionRepository == null) {
            transactionRepository = new TransactionRepository(application);
        }
        return transactionRepository;
    }
}