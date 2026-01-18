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
import java.util.Stack;

public class MainViewModel extends AndroidViewModel {

    private final GoalRepository goalRepository;
    private final MutableLiveData<String> currentParentId = new MutableLiveData<>(null);
    private final Stack<String> navigationStack = new Stack<>();

    private final MutableLiveData<String> currentParentTitle = new MutableLiveData<>("Мои цели");
    private final MutableLiveData<Double> currentParentAmount = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> showBackButton = new MutableLiveData<>(false);

    public MainViewModel(@NonNull Application application) {
        super(application);
        goalRepository = RepositoryFactory.getInstance(application).getGoalRepository();
    }

    // Получение данных с учетом текущего родителя
    public LiveData<List<GoalEntity>> getCurrentGoals() {
        if (currentParentId.getValue() == null) {
            return goalRepository.getRootGoals();
        } else {
            return goalRepository.getSubGoals(currentParentId.getValue());
        }
    }

    public void navigateToGoal(GoalEntity goal) {
        navigationStack.push(currentParentId.getValue());
        currentParentId.setValue(goal.getId());
        updateNavigationState();
    }

    public boolean navigateBack() {
        if (!navigationStack.isEmpty()) {
            currentParentId.setValue(navigationStack.pop());
            updateNavigationState();
            return true;
        }
        return false;
    }

    public void dropInto(GoalEntity droppedGoal, GoalEntity targetGoal) {
        droppedGoal.setParentId(targetGoal.getId());
        goalRepository.update(droppedGoal);
    }

    public void dropOut(GoalEntity goal) {
        if (navigationStack.isEmpty()) {
            return;
        }
        goal.setParentId(navigationStack.peek());
        goalRepository.update(goal);
    }

    // Навигация
    public void setCurrentParentId(String parentId) {
        currentParentId.setValue(parentId);
        updateNavigationState();
    }

    public String getCurrentParentId() {
        return currentParentId.getValue();
    }

    public LiveData<String> getCurrentParentTitle() {
        return currentParentTitle;
    }

    public MutableLiveData<Double> getCurrentParentAmount() {
        return currentParentAmount;
    }

    public LiveData<Boolean> getShowBackButton() {
        return showBackButton;
    }

    private void updateNavigationState() {
        String parentId = currentParentId.getValue();
        showBackButton.setValue(parentId != null);

        if (parentId == null) {
            currentParentTitle.setValue("Мои цели");
        } else {
            goalRepository.getGoalById(parentId).observeForever(goal -> {
                if (goal != null) {
                    currentParentTitle.setValue(goal.getTitle());
                    currentParentAmount.setValue(goal.getCurrentAmount());
                } else {
                    currentParentTitle.setValue("Вложенные цели");
                    currentParentAmount.setValue(0.0);
                }
            });
        }
    }

    public void updatePosition(GoalEntity draggedGoal, int newPosition, List<GoalEntity> goals) {
        int step = newPosition > draggedGoal.getOrderPosition() ? -1 : 1;
        if (newPosition > draggedGoal.getOrderPosition()) {
            --newPosition;
        }
        for (int i = newPosition; i != draggedGoal.getOrderPosition(); i += step) {
            var goal = goals.get(i);
            goal.setOrderPosition(goal.getOrderPosition() + step);
            goalRepository.update(goal);
        }
        draggedGoal.setOrderPosition(newPosition);
        goalRepository.update(draggedGoal);
    }

    public LiveData<Double> getTotalSavedAmount() {
        return goalRepository.getTotalSavedAmount(currentParentId.getValue());
    }
}