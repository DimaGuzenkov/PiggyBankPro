package com.example.piggybankpro.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.piggybankpro.data.local.entities.AutoDepositEntity;
import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.data.local.entities.Id;
import com.example.piggybankpro.data.repository.AutoDepositRepository;
import com.example.piggybankpro.data.repository.RepositoryFactory;

import java.util.List;

public class AutoDepositViewModel extends AndroidViewModel {

    private final AutoDepositRepository autoDepositRepository;
    private final LiveData<List<AutoDepositEntity>> allAutoDeposits;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AutoDepositViewModel(@NonNull Application application) {
        super(application);
        autoDepositRepository = RepositoryFactory.getInstance(application).getAutoDepositRepository();
        allAutoDeposits = autoDepositRepository.getAllAutoDeposits();
    }

    // Получение данных
    public LiveData<List<AutoDepositEntity>> getAllAutoDeposits() {
        return allAutoDeposits;
    }

    public LiveData<AutoDepositEntity> getAutoDepositById(Id id) {
        return autoDepositRepository.getAutoDepositById(id);
    }

    public void createAutoDeposit(AutoDepositEntity autoDeposit, List<GoalDepositCrossRefEntity> crossRefs) {
        try {
            validateAutoDeposit(autoDeposit);
            autoDepositRepository.insert(autoDeposit, crossRefs);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при создании автопополнения: " + e.getMessage());
        }
    }

    public void updateAutoDeposit(AutoDepositEntity autoDeposit, List<GoalDepositCrossRefEntity> crossRefs) {
        try {
            validateAutoDeposit(autoDeposit);
            autoDepositRepository.update(autoDeposit, crossRefs);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при обновлении автопополнения: " + e.getMessage());
        }
    }

    public void deleteAutoDeposit(AutoDepositEntity autoDeposit) {
        try {
            autoDepositRepository.delete(autoDeposit);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при удалении автопополнения: " + e.getMessage());
        }
    }

    // Валидация
    private void validateAutoDeposit(AutoDepositEntity autoDeposit) throws Exception {
        if (autoDeposit.getName() == null || autoDeposit.getName().trim().isEmpty()) {
            throw new Exception("Введите название автопополнения");
        }

        if (autoDeposit.getAmount() == null || autoDeposit.getAmount() <= 0) {
            throw new Exception("Сумма должна быть больше нуля");
        }
    }

    // Управление связями с целями
    public LiveData<List<GoalDepositCrossRefEntity>> getCrossRefsByDepositId(Id depositId) {
        return autoDepositRepository.getCrossRefsByDepositId(depositId);
    }

    // Выполнение автопополнения
    public void executeAutoDeposit(Id depositId) {
        try {
            autoDepositRepository.executeAutoDeposit(depositId);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при выполнении автопополнения: " + e.getMessage());
        }
    }

    public void executeDueAutoDeposits() {
        try {
            autoDepositRepository.executeDueAutoDeposits();
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при выполнении отложенных автопополнений: " + e.getMessage());
        }
    }

    // Управление статусом
    public void toggleAutoDepositStatus(Id depositId, boolean isActive) {
        try {
            autoDepositRepository.setActive(depositId, isActive);
            errorMessage.setValue(null);
        } catch (Exception e) {
            errorMessage.setValue("Ошибка при изменении статуса: " + e.getMessage());
        }
    }

    // Ошибки
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}