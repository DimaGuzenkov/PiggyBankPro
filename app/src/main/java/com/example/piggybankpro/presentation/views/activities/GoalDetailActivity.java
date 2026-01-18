package com.example.piggybankpro.presentation.views.activities;

import static com.example.piggybankpro.presentation.utils.DateUtils.*;
import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;
import static com.example.piggybankpro.presentation.utils.ToastUtils.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.presentation.adapters.TransactionAdapter;
import com.example.piggybankpro.presentation.utils.ViewUtils;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;
import com.example.piggybankpro.presentation.viewmodels.TransactionViewModel;
import com.example.piggybankpro.presentation.views.dialogs.ChangeAmountDialogs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class GoalDetailActivity extends AppCompatActivity {

    private TextView textViewTitle;
    private TextView textViewDescription;
    private TextView textViewTargetAmount;
    private TextView textViewCurrentAmount;
    private TextView textViewProgressPercentage;
    private TextView textViewDaysLeft;
    private TextView textViewAmountNeeded;
    private TextView textViewTargetDate;
    private ProgressBar progressBarGoal;
    private MaterialCardView cardViewProgress;
    private MaterialButton buttonDeposit;
    private MaterialButton buttonWithdraw;
    private MaterialButton buttonTransfer;
    private RecyclerView recyclerViewTransactions;

    private GoalViewModel goalViewModel;
    private TransactionViewModel transactionViewModel;
    private TransactionAdapter transactionAdapter;
    private ChangeAmountDialogs dialogs;

    private String goalId;
    private GoalEntity currentGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_detail);

        goalId = getIntent().getStringExtra("goal_id");
        if (goalId == null) {
            goalNotFound(this);
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        dialogs = new ChangeAmountDialogs(this, goalViewModel);

        initViews();
        setupRecyclerView();
        observeData();
        setupButtons();
        loadGoalData();
        loadTransactions();
    }

    private void initViews() {
        textViewTitle = findViewById(R.id.text_view_title);
        textViewDescription = findViewById(R.id.text_view_description);
        textViewTargetAmount = findViewById(R.id.text_view_target_amount);
        textViewCurrentAmount = findViewById(R.id.text_view_current_amount);
        textViewProgressPercentage = findViewById(R.id.text_view_progress_percentage);
        textViewDaysLeft = findViewById(R.id.text_view_days_left);
        textViewAmountNeeded = findViewById(R.id.text_view_amount_needed);
        textViewTargetDate = findViewById(R.id.text_view_target_date);
        progressBarGoal = findViewById(R.id.progress_bar_goal);
        cardViewProgress = findViewById(R.id.card_view_progress);
        buttonDeposit = findViewById(R.id.button_deposit);
        buttonWithdraw = findViewById(R.id.button_withdraw);
        buttonTransfer = findViewById(R.id.button_transfer);
        recyclerViewTransactions = findViewById(R.id.recycler_view_transactions);
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionAdapter);

        recyclerViewTransactions.addItemDecoration(
                new androidx.recyclerview.widget.DividerItemDecoration(
                        this,
                        LinearLayoutManager.VERTICAL
                )
        );
    }

    private void setupButtons() {
        buttonDeposit.setOnClickListener(v -> dialogs.showDepositDialog(goalId));
        buttonWithdraw.setOnClickListener(v -> dialogs.showWithdrawDialog(goalId, currentGoal.getCurrentAmount()));
        buttonTransfer.setOnClickListener(v -> dialogs.showTransferDialog(goalId, currentGoal.getCurrentAmount()));
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
        textViewTitle.setText(goal.getTitle());

        if (goal.getDescription() != null) {
            textViewDescription.setText(goal.getDescription());
        } else {
            textViewDescription.setText("Нет описания");
        }

        if (goal.getTargetAmount() != null) {
            textViewTargetAmount.setText(formatAmount(goal.getTargetAmount()));
            textViewTargetAmount.setVisibility(View.VISIBLE);
        } else {
            textViewTargetAmount.setVisibility(View.GONE);
        }

        textViewCurrentAmount.setText(formatAmount(goal.getCurrentAmount()));

        if (goal.getProgressPercentage() != null && goal.getTargetAmount() != null) {
            ViewUtils.updateGoalProgress(progressBarGoal, textViewProgressPercentage, goal.getProgressPercentage());
        } else {
            progressBarGoal.setVisibility(View.GONE);
            textViewProgressPercentage.setVisibility(View.GONE);
        }

        Long daysLeft = goal.getDaysRemaining();
        if (daysLeft != null) {
            textViewDaysLeft.setText(formatDays(daysLeft));
            textViewDaysLeft.setVisibility(View.VISIBLE);
        } else {
            textViewDaysLeft.setVisibility(View.GONE);
        }

        Double amountNeeded = goal.getAmountNeeded();
        if (amountNeeded != null && amountNeeded > 0) {
            textViewAmountNeeded.setText(formatAmount(amountNeeded));
            textViewAmountNeeded.setVisibility(View.VISIBLE);
        } else if (amountNeeded != null && amountNeeded <= 0) {
            textViewAmountNeeded.setText("Цель достигнута!");
            cardViewProgress.setCardBackgroundColor(
                    getResources().getColor(R.color.success_light, null)
            );

            textViewAmountNeeded.setVisibility(View.VISIBLE);
        } else {
            textViewAmountNeeded.setVisibility(View.GONE);
        }

        if (goal.getTargetDate() != null) {
            textViewTargetDate.setText(formatDate(goal.getTargetDate()));
            textViewTargetDate.setVisibility(View.VISIBLE);
        } else {
            textViewTargetDate.setVisibility(View.GONE);
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
        intent.putExtra("goal_id", goalId);
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
}