package com.example.piggybankpro.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.piggybankpro.data.local.entities.GoalEntity;

public class SharedViewModel extends AndroidViewModel {

    private final MutableLiveData<GoalEntity> selectedGoalForOperation = new MutableLiveData<>();
    private final MutableLiveData<Double> quickEditAmount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isQuickEditDeposit = new MutableLiveData<>(true);
    private final MutableLiveData<String> quickEditDescription = new MutableLiveData<>();

    public SharedViewModel(@NonNull Application application) {
        super(application);
    }

    // Быстрое редактирование цели
    public MutableLiveData<GoalEntity> getSelectedGoalForOperation() {
        return selectedGoalForOperation;
    }

    public void clearSelectedGoalForOperation() {
        selectedGoalForOperation.setValue(null);
    }

    public MutableLiveData<Double> getQuickEditAmount() {
        return quickEditAmount;
    }

    public void clearQuickEditAmount() {
        quickEditAmount.setValue(null);
    }

    public MutableLiveData<Boolean> getIsQuickEditDeposit() {
        return isQuickEditDeposit;
    }

    // Описание для быстрого редактирования
    public MutableLiveData<String> getQuickEditDescription() {
        return quickEditDescription;
    }

    public void clearQuickEditDescription() {
        quickEditDescription.setValue(null);
    }

    public void resetQuickEdit() {
        clearSelectedGoalForOperation();
        clearQuickEditAmount();
        clearQuickEditDescription();
        isQuickEditDeposit.setValue(true);
    }
}