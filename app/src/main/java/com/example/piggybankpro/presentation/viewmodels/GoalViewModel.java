package com.example.piggybankpro.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.data.repository.GoalRepository;
import com.example.piggybankpro.data.repository.RepositoryFactory;

import java.util.List;

public class GoalViewModel extends AndroidViewModel {

    private final GoalRepository goalRepository;
    private final LiveData<List<GoalEntity>> allGoals;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public GoalViewModel(@NonNull Application application) {
        super(application);
        goalRepository = RepositoryFactory.getInstance(application).getGoalRepository();
        allGoals = goalRepository.getAllGoals();
    }

    public LiveData<List<GoalEntity>> getAllGoals() {
        return allGoals;
    }

    public LiveData<GoalEntity> getGoalById(String goalId) {
        return goalRepository.getGoalById(goalId);
    }

    public void createGoal(GoalEntity goal) {
        try {
            goalRepository.insert(goal);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при создании цели: " + e.getMessage());
        }
    }

    public void updateGoal(GoalEntity goal) {
        try {
            goalRepository.update(goal);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при обновлении цели: " + e.getMessage());
        }
    }

    public void deleteGoal(GoalEntity goal) {
        try {
            goalRepository.delete(goal);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при удалении цели: " + e.getMessage());
        }
    }

    public void depositToGoal(String goalId, double amount, String description) {
        try {
            if (amount <= 0) {
                errorMessage.setValue("Сумма пополнения должна быть больше нуля");
                return;
            }
            goalRepository.deposit(goalId, amount, description);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при пополнении: " + e.getMessage());
        }
    }

    public void withdrawFromGoal(String goalId, double amount, String description) {
        try {
            if (amount <= 0) {
                errorMessage.setValue("Сумма списания должна быть больше нуля");
                return;
            }
            goalRepository.withdraw(goalId, amount, description);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при списании: " + e.getMessage());
        }
    }

    public void transferBetweenGoals(String sourceGoalId, String destGoalId, double amount, String description) {
        try {
            if (amount <= 0) {
                errorMessage.setValue("Сумма перевода должна быть больше нуля");
                return;
            }
            goalRepository.transfer(sourceGoalId, destGoalId, amount, description);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при переводе: " + e.getMessage());
        }
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}