package com.example.piggybankpro.presentation.views.activities;

import static com.example.piggybankpro.presentation.utils.DateUtils.*;
import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;
import static com.example.piggybankpro.presentation.utils.ToastUtils.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.converters.IdConverter;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.data.local.entities.Id;
import com.example.piggybankpro.data.local.entities.TransactionEntity;
import com.example.piggybankpro.databinding.ActivityGoalDetailBinding;
import com.example.piggybankpro.presentation.adapters.TransactionAdapter;
import com.example.piggybankpro.presentation.utils.SwipeItemTouchHelperCallback;
import com.example.piggybankpro.presentation.utils.ViewUtils;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;
import com.example.piggybankpro.presentation.viewmodels.TransactionViewModel;
import com.example.piggybankpro.presentation.views.dialogs.ChangeAmountDialogs;

import java.util.ArrayList;
import java.util.Objects;

public class GoalDetailActivity extends AppCompatActivity implements
        SwipeItemTouchHelperCallback.OnSwipeListener {
    private ActivityGoalDetailBinding binding;

    private GoalViewModel goalViewModel;
    private TransactionViewModel transactionViewModel;
    private TransactionAdapter transactionAdapter;
    private ChangeAmountDialogs dialogs;

    private Id goalId;
    private GoalEntity currentGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        goalId = IdConverter.fromStrToId(getIntent().getStringExtra("goal_id"));
        if (goalId == null) {
            goalNotFound(this);
            finish();
            return;
        }

        binding = ActivityGoalDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        dialogs = new ChangeAmountDialogs(this, goalViewModel);

        setupRecyclerView();
        observeData();
        setupButtons();
        loadGoalData();
        loadTransactions();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        binding.recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTransactions.setAdapter(transactionAdapter);

        binding.recyclerViewTransactions.addItemDecoration(
                new androidx.recyclerview.widget.DividerItemDecoration(
                        this,
                        LinearLayoutManager.VERTICAL
                )
        );

        var itemTouchHelper = new ItemTouchHelper(new SwipeItemTouchHelperCallback(this));
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTransactions);
    }

    private void setupButtons() {
        binding.buttonDeposit.setOnClickListener(v -> dialogs.showDepositDialog(goalId));
        binding.buttonWithdraw.setOnClickListener(v -> dialogs.showWithdrawDialog(goalId, currentGoal.getCurrentAmount()));
        binding.buttonTransfer.setOnClickListener(v -> dialogs.showTransferDialog(goalId, currentGoal.getCurrentAmount()));
    }

    private void observeData() {
        goalViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                display(this, errorMessage);
                goalViewModel.clearError();
            }
        });

        transactionViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                display(this, errorMessage);
                transactionViewModel.clearError();
            }
        });
    }

    private void loadGoalData() {
        goalViewModel.getGoalById(goalId).observe(this, goal -> {
            if (goal != null) {
                currentGoal = goal;
                updateUI(goal);
            }
        });
    }

    private void loadTransactions() {
        transactionViewModel.getTransactionsByGoalId(goalId).observe(this, transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                transactionAdapter.updateTransactions(transactions);
            } else {
                transactionAdapter.updateTransactions(new ArrayList<>());
            }
        });
    }

    private void updateUI(GoalEntity goal) {
        binding.textViewTitle.setText(goal.getTitle());

        if (goal.getDescription() != null) {
            binding.textViewDescription.setText(goal.getDescription());
        } else {
            binding.textViewDescription.setText("Нет описания");
        }

        if (goal.getTargetAmount() != null) {
            binding.textViewTargetAmount.setText(formatAmount(goal.getTargetAmount()));
            binding.textViewTargetAmount.setVisibility(View.VISIBLE);
        } else {
            binding.textViewTargetAmount.setVisibility(View.GONE);
        }

        binding.textViewCurrentAmount.setText(formatAmount(goal.getCurrentAmount()));

        if (goal.getProgressPercentage() != null && goal.getTargetAmount() != null) {
            ViewUtils.updateGoalProgress(binding.progressBarGoal, binding.textViewProgressPercentage, goal.getProgressPercentage());
        } else {
            binding.progressBarGoal.setVisibility(View.GONE);
            binding.textViewProgressPercentage.setVisibility(View.GONE);
        }

        Long daysLeft = goal.getDaysRemaining();
        if (daysLeft != null) {
            binding.textViewDaysLeft.setText(formatDays(daysLeft));
            binding.textViewDaysLeft.setVisibility(View.VISIBLE);
        } else {
            binding.textViewDaysLeft.setVisibility(View.GONE);
        }

        Double amountNeeded = goal.getAmountNeeded();
        if (amountNeeded != null && amountNeeded > 0) {
            binding.textViewAmountNeeded.setText(formatAmount(amountNeeded));
            binding.textViewAmountNeeded.setVisibility(View.VISIBLE);
        } else if (amountNeeded != null && amountNeeded <= 0) {
            binding.textViewAmountNeeded.setText("Цель достигнута!");
            binding.cardViewProgress.setCardBackgroundColor(
                    getResources().getColor(R.color.success_light, null)
            );

            binding.textViewAmountNeeded.setVisibility(View.VISIBLE);
        } else {
            binding.textViewAmountNeeded.setVisibility(View.GONE);
        }

        if (goal.getTargetDate() != null) {
            binding.textViewTargetDate.setText(formatDate(goal.getTargetDate()));
            binding.textViewTargetDate.setVisibility(View.VISIBLE);
        } else {
            binding.textViewTargetDate.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_goal_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            editGoal();
            return true;
        } else if (id == R.id.action_delete) {
            deleteGoal();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void editGoal() {
        Intent intent = new Intent(this, CreateGoalActivity.class);
        intent.putExtra("goal_id", IdConverter.fromIdToStr(goalId));
        intent.putExtra("parent_id", IdConverter.fromIdToStr(currentGoal.getParentId()));
        intent.putExtra("order_position", currentGoal.getOrderPosition());
        startActivity(intent);
    }

    private void deleteGoal() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление цели")
                .setMessage("Вы уверены, что хотите удалить эту цель? Все связанные транзакции также будут удалены.")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    if (currentGoal != null) {
                        goalViewModel.deleteGoal(currentGoal);
                        goalDeleted(this);
                        finish();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void deleteItem(int position) {
        var transaction = transactionAdapter.getItemByPosition(position);
        if (transaction.getTransactionType() == TransactionEntity.TYPE_DEPOSIT) {

        }
    }
}