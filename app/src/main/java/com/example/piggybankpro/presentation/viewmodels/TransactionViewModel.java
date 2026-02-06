package com.example.piggybankpro.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.piggybankpro.data.local.entities.Id;
import com.example.piggybankpro.data.local.entities.TransactionEntity;
import com.example.piggybankpro.data.repository.TransactionRepository;
import com.example.piggybankpro.data.repository.RepositoryFactory;

import java.util.Date;
import java.util.List;

public class TransactionViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = RepositoryFactory.getInstance(application).getTransactionRepository();
    }

    public LiveData<List<TransactionEntity>> getTransactionsByGoalId(Id goalId) {
        return transactionRepository.getTransactionsByGoalId(goalId);
    }

    public void deleteTransaction(TransactionEntity transaction) {
        transactionRepository.delete(transaction);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}