package com.example.piggybankpro.presentation.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.data.local.entities.Id;
import com.example.piggybankpro.data.repository.GoalRepository;
import com.example.piggybankpro.data.repository.RepositoryFactory;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class MainViewModel extends AndroidViewModel {

    private final GoalRepository goalRepository;
    private final MutableLiveData<Id> currentParentId = new MutableLiveData<>(null);
    private final Stack<Id> navigationStack = new Stack<>();

    private LiveData<List<GoalEntity>> currentGoals;

    public LiveData<String> currentParentTitle;
    public LiveData<Double> currentParentAmount;
    public LiveData<Boolean> showBackButton;

    private final MediatorLiveData<Double> totalSavedAmount = new MediatorLiveData<>();
    private LiveData<Double> currentTotalSource;

    public MainViewModel(@NonNull Application application) {
        super(application);
        goalRepository = RepositoryFactory.getInstance(application).getGoalRepository();

        initLiveData();
    }

    private void initLiveData() {
        currentGoals = Transformations.switchMap(currentParentId, goalRepository::getSubGoals);

        LiveData<GoalEntity> currentParentGoal = Transformations.switchMap(currentParentId, parentId -> {
            if (parentId == null) {
                return AbsentLiveData.create();
            } else {
                return goalRepository.getGoalById(parentId);
            }
        });

        // Производные LiveData
        currentParentTitle = Transformations.map(currentParentGoal, goal ->
                goal != null ? goal.getTitle() : "Мои цели"
        );

        currentParentAmount = Transformations.map(currentParentGoal, goal ->
                goal != null ? goal.getCurrentAmount() : 0.0
        );

        showBackButton = Transformations.map(currentParentId, Objects::nonNull);

        // Динамически меняем источник totalSavedAmount в зависимости от currentParentId
        totalSavedAmount.addSource(currentParentId, parentId -> {
            // Удаляем предыдущий источник, если он есть
            if (currentTotalSource != null) {
                totalSavedAmount.removeSource(currentTotalSource);
            }

            // Подписываемся на новый источник
            currentTotalSource = goalRepository.getTotalSavedAmount(parentId);
            totalSavedAmount.addSource(currentTotalSource, totalSavedAmount::setValue);
        });
    }

    public LiveData<List<GoalEntity>> getCurrentGoals() {
        return currentGoals;
    }

    public LiveData<Double> getTotalSavedAmount() {
        return totalSavedAmount;
    }

    public Id getCurrentParentId() {
        return currentParentId.getValue();
    }

    // Навигация
    public void navigateToGoal(GoalEntity goal) {
        navigationStack.push(currentParentId.getValue());
        currentParentId.setValue(goal.getId());
    }

    public boolean navigateBack() {
        if (!navigationStack.isEmpty()) {
            currentParentId.setValue(navigationStack.pop());
            return true;
        }
        return false;
    }

    public void dropInto(GoalEntity droppedGoal, GoalEntity targetGoal) {
        droppedGoal.setParentId(targetGoal.getId());
        droppedGoal.setOrderPosition(-1);
        goalRepository.update(droppedGoal);
    }

    public void dropOut(GoalEntity goal) {
        if (navigationStack.isEmpty()) {
            return;
        }
        goal.setParentId(navigationStack.peek());
        goal.setOrderPosition(-1);
        goalRepository.update(goal);
    }

    public void deleteGoal(GoalEntity goal) {
        goalRepository.delete(goal);
    }

    public void setCurrentParentId(Id parentId) {
        navigationStack.clear();
        currentParentId.setValue(parentId);
    }

    public void updatePosition(GoalEntity draggedGoal, int newPosition) {
        if (newPosition == draggedGoal.getOrderPosition()) {
            return;
        }

        draggedGoal.setOrderPosition(newPosition);
        goalRepository.update(draggedGoal);
    }

    public LiveData<List<GoalEntity>> getAllGoals() {
        return goalRepository.getAllGoals();
    }

    public static class AbsentLiveData<T> extends LiveData<T> {
        public static <T> LiveData<T> create() {
            return new AbsentLiveData<>();
        }

        private AbsentLiveData() {
            postValue(null);
        }
    }
}