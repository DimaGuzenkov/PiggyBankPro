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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.databinding.ActivityMainBinding;
import com.example.piggybankpro.presentation.adapters.GoalAdapter;
import com.example.piggybankpro.presentation.utils.SwipeItemTouchHelperCallback;
import com.example.piggybankpro.presentation.utils.ToastUtils;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;
import com.example.piggybankpro.presentation.viewmodels.MainViewModel;
import com.example.piggybankpro.presentation.views.dialogs.ChangeAmountDialogs;

import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        SwipeItemTouchHelperCallback.OnSwipeListener,
        GoalAdapter.OnGoalClickListener {

    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;
    private GoalAdapter goalAdapter;
    private ChangeAmountDialogs dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        dialog = new ChangeAmountDialogs(this, new ViewModelProvider(this).get(GoalViewModel.class));

        setupRecyclerView();
        observeViewModel();
        setupFloatingActionButton();
        setupDragAndDrop();

        var itemTouchHelper = new ItemTouchHelper(new SwipeItemTouchHelperCallback(this));
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewGoals);
    }

    private void display(String str) {
        ToastUtils.display(this, str);
    }

    private void setupRecyclerView() {
        goalAdapter = new GoalAdapter(new ArrayList<>(), this);



        binding.nonDistributedAmountBlock.setOnClickListener(v -> {
            dialog.showTransferDialog(
                    mainViewModel.getCurrentParentId(),
                    mainViewModel.currentParentAmount.getValue());
        });
        binding.nonDistributedAmountBlock.setVisibility(View.GONE);

        binding.recyclerViewGoals.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewGoals.setAdapter(goalAdapter);
    }

    @Override
    public void onGoalClick(GoalEntity goal) {
        mainViewModel.navigateToGoal(goal);
    }

    @Override
    public void onGoalLongClick(GoalEntity goal, View view) {
        goalAdapter.setDraggedGoal(goal);

        ClipData data = ClipData.newPlainText("goal_id", goal.getId());

        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

        view.setAlpha(0.5f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(data, shadowBuilder, goal, 0);
        } else {
            view.startDrag(data, shadowBuilder, goal, 0);
        }

        view.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                v.setAlpha(1.0f);
            }
            return true;
        });
    }

    @Override
    public void onGoalDetailsClick(GoalEntity goal) {
        Intent intent = new Intent(this, GoalDetailActivity.class);
        intent.putExtra("goal_id", goal.getId());
        startActivity(intent);
    }

    @Override
    public void onGoalDroppedOnGoal(GoalEntity draggedGoal, GoalEntity targetGoal) {
        display(draggedGoal.getTitle() + " to " + targetGoal.getTitle());
        mainViewModel.dropInto(draggedGoal, targetGoal);
    }

    @Override
    public void onGoalDroppedBetween(GoalEntity draggedGoal, int insertIndex) {
        mainViewModel.updatePosition(draggedGoal, insertIndex);
        ToastUtils.display(MainActivity.this,
                "Цель '" + draggedGoal.getTitle() + "' перемещена в '" + insertIndex + "'");
    }

    private void setupDragAndDrop() {
        binding.getRoot().setOnDragListener((v, event) -> switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED ->
                    event.getClipDescription() != null &&
                            event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
            case DragEvent.ACTION_DROP -> {
                if (event.getLocalState() instanceof GoalEntity draggedGoal) {
                    mainViewModel.dropOut(draggedGoal);
                }
                yield true;
            }
            case DragEvent.ACTION_DRAG_ENDED -> true;
            default -> false;
        });
    }

    @Override
    public void onBackPressed() {
        if (!mainViewModel.navigateBack()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return mainViewModel.navigateBack();
    }

    private void observeViewModel() {
        mainViewModel.currentParentTitle.observe(this, title ->
                Objects.requireNonNull(getSupportActionBar()).setTitle(title)
        );

        mainViewModel.showBackButton.observe(this, showBack -> {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(showBack);
            binding.nonDistributedAmountBlock.setVisibility(showBack ? View.VISIBLE : View.GONE);
        });

        mainViewModel.getTotalSavedAmount().observe(this, totalSaved ->
                binding.textViewTotalSaved.setText(formatAmount(totalSaved != null ? totalSaved : 0))
        );

        mainViewModel.getCurrentGoals().observe(this, goals -> {
            if (goals != null) {
                goalAdapter.updateGoals(goals);
                binding.textViewEmptyState.setVisibility(View.GONE);
                binding.recyclerViewGoals.setVisibility(View.VISIBLE);
            } else {
                binding.textViewEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewGoals.setVisibility(View.GONE);
            }
        });

        mainViewModel.currentParentAmount.observe(this, amount -> {
            binding.textViewNonDistributed.setText(formatAmount(amount));
        });

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

    @Override
    public void deleteItem(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление цели")
                .setMessage("Вы уверены, что хотите удалить эту цель? Все связанные транзакции также будут удалены.\n" + goalAdapter.getGoalByPosition(position).getTitle())
                .setPositiveButton("Удалить", (dialog, which) -> mainViewModel.deleteGoal(goalAdapter.getGoalByPosition(position)))
                .setNegativeButton("Отмена", null)
                .show();
    }
}