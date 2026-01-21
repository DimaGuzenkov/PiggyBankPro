package com.example.piggybankpro.presentation.views.activities;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.databinding.ActivityMainBinding;
import com.example.piggybankpro.presentation.adapters.GoalAdapter;
import com.example.piggybankpro.presentation.utils.GoalItemTouchHelperCallback;
import com.example.piggybankpro.presentation.utils.ToastUtils;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;
import com.example.piggybankpro.presentation.viewmodels.MainViewModel;
import com.example.piggybankpro.presentation.views.dialogs.ChangeAmountDialogs;

import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;
    private GoalViewModel goalViewModel;
    private GoalAdapter goalAdapter;
    private ChangeAmountDialogs dialog;
    private GoalEntity currentDraggedGoal;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);
        dialog = new ChangeAmountDialogs(this, goalViewModel);

        setupRecyclerView();
        observeViewModel();
        setupFloatingActionButton();
        setupDragAndDrop();

        GoalItemTouchHelperCallback callback = new GoalItemTouchHelperCallback(goalAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewGoals);
    }

    private void display(String str) {
        ToastUtils.display(this, str);
    }

    private void setupRecyclerView() {
        goalAdapter = new GoalAdapter(new ArrayList<>(), new GoalAdapter.OnGoalClickListener() {
            @Override
            public void onGoalClick(GoalEntity goal) {
                mainViewModel.navigateToGoal(goal);
                observeViewModel();
            }

            @Override
            public void onGoalLongClick(GoalEntity goal, View view) {
                // Начинаем перетаскивание при долгом нажатии
                startDrag(goal, view);
            }

            @Override
            public void onGoalDetailsClick(GoalEntity goal) {
                onGoalClicked(goal);
            }

            @Override
            public void onGoalDroppedOnGoal(GoalEntity draggedGoal, GoalEntity targetGoal) {
                display(draggedGoal.getTitle() + " to " + targetGoal.getTitle());
                mainViewModel.dropInto(draggedGoal, targetGoal);
            }

            @Override
            public void onGoalDroppedBetween(GoalEntity draggedGoal, int insertIndex) {
                mainViewModel.updatePosition(draggedGoal, insertIndex, goalAdapter.getGoals());
                ToastUtils.display(MainActivity.this,
                            "Цель '" + draggedGoal.getTitle() + "' перемещена в '" + insertIndex + "'");
            }

            @Override
            public void onGoalSwiped(int position) {
                var goal = goalAdapter.getGoalByPosition(position);
                showDeleteConfirmationDialog(goal, position);
            }
        });

        mainViewModel.getCurrentParentTitle().observe(this, title ->
                Objects.requireNonNull(getSupportActionBar()).setTitle(title)
        );

        mainViewModel.getShowBackButton().observe(this, showBack ->
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(showBack)
        );

        mainViewModel.getTotalSavedAmount().observe(this, totalSaved ->
                binding.textViewTotalSaved.setText(formatAmount(totalSaved != null ? totalSaved : 0))
        );

        binding.nonDistributedAmountBlock.setOnClickListener(v -> {
            dialog.showTransferDialog(
                    mainViewModel.getCurrentParentId(),
                    mainViewModel.getCurrentParentAmount().getValue());
        });
        binding.nonDistributedAmountBlock.setVisibility(View.GONE);

        binding.recyclerViewGoals.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewGoals.setAdapter(goalAdapter);

//        binding.recyclerViewGoals.addItemDecoration(
//                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
//        );
    }

    private void showDeleteConfirmationDialog(GoalEntity goal, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление цели")
                .setMessage("Вы уверены, что хотите удалить цель \"" + goal.getTitle() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    // Подтверждение удаления
//                    goalAdapter.removeItem(position);
//                    mainViewModel.deleteGoal(goal.getId());
                    ToastUtils.display(this, "Цель удалена: " + goal.getTitle());
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    // Отменяем свайп - восстанавливаем элемент
                    goalAdapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> {
                    // Если диалог отменен (например, нажата кнопка назад)
                    goalAdapter.notifyItemChanged(position);
                })
                .create()
                .show();
    }

    private void setupDragAndDrop() {
        // Устанавливаем слушатель на корневой layout для перетаскивания вне списка
        binding.getRoot().setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // Проверяем тип данных
                        return event.getClipDescription() != null &&
                                event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);

                    case DragEvent.ACTION_DROP:
                        // Если отпустили вне RecyclerView, перемещаем на корневой уровень
//                        Object localState = event.getLocalState();
                        if (event.getLocalState() instanceof GoalEntity draggedGoal) {
                            mainViewModel.dropOut(draggedGoal);
                        }
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        // Сбрасываем подсветку
//                        goalAdapter.clearHighlight();
                        return true;
                }
                return false;
            }
        });
    }

    private void startDrag(GoalEntity goal, View view) {
        // Сохраняем перетаскиваемую цель
        currentDraggedGoal = goal;
        goalAdapter.setDraggedGoal(goal);

        ClipData data = ClipData.newPlainText("goal_id", goal.getId());

        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

        view.setAlpha(0.5f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(data, shadowBuilder, goal, 0);
        } else {
            view.startDrag(data, shadowBuilder, goal, 0);
        }

        // Устанавливаем слушатель для восстановления видимости
        view.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                    v.setAlpha(1.0f);
//                    goalAdapter.clearHighlight();
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mainViewModel.navigateBack()) {
            observeViewModel();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (mainViewModel.navigateBack()) {
            observeViewModel();
            return true;
        } else {
            return false;
        }
    }

    private void observeViewModel() {
        mainViewModel.getCurrentGoals().observe(this, goals -> {
            if (goals != null && !goals.isEmpty()) {
                goalAdapter.updateGoals(goals);
                binding.textViewEmptyState.setVisibility(View.GONE);
                binding.recyclerViewGoals.setVisibility(View.VISIBLE);
            } else {
                binding.textViewEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewGoals.setVisibility(View.GONE);
            }
        });

        if (mainViewModel.getCurrentParentId() == null) {
            binding.nonDistributedAmountBlock.setVisibility(View.GONE);
        } else {
            mainViewModel.getCurrentParentAmount().observe(this, amount -> {
                binding.textViewNonDistributed.setText(formatAmount(amount));
                binding.nonDistributedAmountBlock.setVisibility(View.VISIBLE);
            });
        }

        mainViewModel.getTotalSavedAmount().observe(this, totalSaved ->
            binding.textViewTotalSaved.setText(formatAmount(totalSaved != null ? totalSaved : 0))
        );
    }

    private void setupFloatingActionButton() {
        binding.fabAddGoal.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CreateGoalActivity.class);
            String currentParentId = mainViewModel.getCurrentParentId();
            intent.putExtra("parent_id", currentParentId);
            intent.putExtra("order_position", goalAdapter.getGoalCount());
            startActivity(intent);
        });
    }

    private void onGoalClicked(GoalEntity goal) {
        Intent intent = new Intent(this, GoalDetailActivity.class);
        intent.putExtra("goal_id", goal.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_auto_deposit) {
            openAutoDeposit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openAutoDeposit() {
        Intent intent = new Intent(this, AutoDepositActivity.class);
        startActivity(intent);
    }
}