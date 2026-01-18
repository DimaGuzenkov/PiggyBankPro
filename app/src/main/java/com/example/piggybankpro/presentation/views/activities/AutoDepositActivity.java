package com.example.piggybankpro.presentation.views.activities;

import static com.example.piggybankpro.presentation.utils.AmountUtils.amountFromString;
import static com.example.piggybankpro.presentation.utils.ToastUtils.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.AutoDepositEntity;
import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.presentation.adapters.AutoDepositAdapter;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.utils.AmountUtils;
import com.example.piggybankpro.presentation.utils.DateUtils;
import com.example.piggybankpro.presentation.utils.ToastUtils;
import com.example.piggybankpro.presentation.viewmodels.AutoDepositViewModel;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;
import com.example.piggybankpro.presentation.views.dialogs.GoalSelectionDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoDepositActivity extends AppCompatActivity {
    private RecyclerView recyclerViewAutoDeposits;
    private TextView textViewEmptyState;
    private FloatingActionButton buttonAddAutoDeposit;

    private AutoDepositViewModel autoDepositViewModel;
    private GoalViewModel goalViewModel;
    private AutoDepositAdapter autoDepositAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_deposit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Автопополнение");

        autoDepositViewModel = new ViewModelProvider(this).get(AutoDepositViewModel.class);
        goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);

        initViews();

        setupRecyclerView();

        observeViewModel();

        setupButtons();
    }

    private void initViews() {
        recyclerViewAutoDeposits = findViewById(R.id.recycler_view_auto_deposits);
        textViewEmptyState = findViewById(R.id.text_view_empty_state);
        buttonAddAutoDeposit = findViewById(R.id.button_add_auto_deposit);
    }

    private void setupRecyclerView() {
        autoDepositAdapter = new AutoDepositAdapter(new ArrayList<>(), new AutoDepositAdapter.OnAutoDepositClickListener() {
            @Override
            public void onAutoDepositClick(AutoDepositEntity autoDeposit) {
                showEditAutoDepositDialog(autoDeposit);
            }

            @Override
            public void onAutoDepositLongClick(AutoDepositEntity autoDeposit, View view) {
                showContextMenu(autoDeposit, view);
            }

            @Override
            public void onToggleStatus(AutoDepositEntity autoDeposit, boolean isActive) {
                autoDepositViewModel.toggleAutoDepositStatus(autoDeposit.getId(), isActive);
            }
        });

        recyclerViewAutoDeposits.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAutoDeposits.setAdapter(autoDepositAdapter);

        recyclerViewAutoDeposits.addItemDecoration(
                new androidx.recyclerview.widget.DividerItemDecoration(
                        this,
                        LinearLayoutManager.VERTICAL
                )
        );
    }

    private void observeViewModel() {
        autoDepositViewModel.getAllAutoDeposits().observe(this, autoDeposits -> {
            if (autoDeposits != null && !autoDeposits.isEmpty()) {
                autoDepositAdapter.updateAutoDeposits(autoDeposits);
                textViewEmptyState.setVisibility(View.GONE);
                recyclerViewAutoDeposits.setVisibility(View.VISIBLE);
            } else {
                textViewEmptyState.setVisibility(View.VISIBLE);
                recyclerViewAutoDeposits.setVisibility(View.GONE);
                textViewEmptyState.setText("У вас нет настроенных автопополнений\nНажмите + чтобы добавить");
            }
        });

        autoDepositViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                display(this, errorMessage);
                autoDepositViewModel.clearError();
            }
        });
    }

    private void setupButtons() {
        buttonAddAutoDeposit.setOnClickListener(v -> {
            showCreateAutoDepositDialog();
        });
    }

    private void showCreateAutoDepositDialog() {
        show(null);
    }

    private void showEditAutoDepositDialog(AutoDepositEntity autoDeposit) {
        show(autoDeposit.getId());
    }

    private void show(String id) {
        Intent intent = new Intent(this, CreateAutoDepositActivity.class);
        intent.putExtra("auto_deposit_id", id);
        startActivity(intent);
    }

//    private void showAutoDepositDialog(AutoDepositEntity existingAutoDeposit) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(existingAutoDeposit != null ? "Редактирование автопополнения" : "Новое автопополнение");
//
//        View dialogView = getLayoutInflater().inflate(R.layout.activity_create_auto_deposit, null);
//        builder.setView(dialogView);
//
//        EditText editTextName = dialogView.findViewById(R.id.edit_text_name);
//        EditText editTextAmount = dialogView.findViewById(R.id.edit_text_amount);
//        Spinner spinnerPeriod = dialogView.findViewById(R.id.spinner_frequency);
//        Button buttonSelectGoals = dialogView.findViewById(R.id.button_add_goal);
//
//        editTextAmount.addTextChangedListener(new AmountTextWatcher(editTextAmount));
//
//        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource(
//                this,
//                R.array.period_types,
//                android.R.layout.simple_spinner_item
//        );
//        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerPeriod.setAdapter(periodAdapter);
//
//        final List<GoalEntity> selectedGoals = new ArrayList<>();
//        final List<GoalDepositCrossRefEntity> crossRefs = new ArrayList<>();
//
//        if (existingAutoDeposit != null) {
//            editTextName.setText(existingAutoDeposit.getName());
//            editTextAmount.setText(AmountUtils.formatAmount(existingAutoDeposit.getAmount()));
//
//            int periodPosition = DateUtils.getPeriodPosition(existingAutoDeposit.getPeriodType());
//            spinnerPeriod.setSelection(periodPosition);
//
//            autoDepositViewModel.getCrossRefsByDepositId(existingAutoDeposit.getId())
//                    .observe(this, crossRefList -> {
//                        if (crossRefList != null) {
//                            crossRefs.clear();
//                            crossRefs.addAll(crossRefList);
//
//                            goalViewModel.getAllGoals().observe(this, allGoals -> {
//                                if (allGoals != null) {
//                                    selectedGoals.clear();
//                                    for (GoalDepositCrossRefEntity crossRef : crossRefList) {
//                                        for (GoalEntity goal : allGoals) {
//                                            if (goal.getId().equals(crossRef.getGoalId())) {
//                                                selectedGoals.add(goal);
//                                                break;
//                                            }
//                                        }
//                                    }
//                                }
//                            });
//                        }
//                    });
//        }
//
//        buttonSelectGoals.setOnClickListener(v -> {
//            GoalSelectionDialogFragment dialog = GoalSelectionDialogFragment.newInstance();
//            dialog.setOnGoalSelectedListener(goal -> {
//                ToastUtils.display(this, goal.getTitle());
//            });
//
//            dialog.show(getSupportFragmentManager(), "GoalSelectionDialog");
//        });
//
//        builder.setPositiveButton("Сохранить", (dialog, which) -> {
//            String name = editTextName.getText().toString().trim();
//
//            if (name.isEmpty()) {
//                editTextName.setError("Введите название");
//                editTextName.requestFocus();
//                return;
//            }
//
//            var amount = amountFromString(editTextAmount.getText().toString()).result();
//            if (amount == null) {
//                editTextAmount.setError("Неверный формат числа");
//                editTextAmount.requestFocus();
//                return;
//            }
//
//            if (amount <= 0) {
//                editTextAmount.setError("Неверный формат числа");
//                editTextAmount.requestFocus();
//                return;
//            }
//
//            AutoDepositEntity autoDeposit = existingAutoDeposit != null ?
//                    existingAutoDeposit : new AutoDepositEntity();
//
//            autoDeposit.setName(name);
//            autoDeposit.setAmount(amount);
//            autoDeposit.setPeriodType(DateUtils.getPeriodTypeFromPosition(spinnerPeriod.getSelectedItemPosition()));
//            autoDeposit.setIsActive(true);
//
//            for (var crossRef: crossRefs) {
//                crossRef.setAmount(amount);
//            }
//
//            if (existingAutoDeposit != null) {
//                autoDepositViewModel.updateAutoDeposit(autoDeposit, crossRefs);
//                autoDepositUpdated(this);
//            } else {
//                autoDepositViewModel.createAutoDeposit(autoDeposit, crossRefs);
//                autoDepositCreated(this);
//            }
//        });
//
//        builder.setNegativeButton("Отмена", null);
//
//        if (existingAutoDeposit != null) {
//            builder.setNeutralButton("Удалить", (dialog, which) ->
//                    showDeleteDialog(existingAutoDeposit));
//        }
//
//        builder.show();
//    }

//    private void showGoalSelectionDialog(List<GoalEntity> selectedGoals,
//                                         List<GoalDepositCrossRefEntity> crossRefs,
//                                         AutoDepositEntity autoDeposit) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Выбор целей для автопополнения");
//
//        // Создаем RecyclerView для выбора целей
//        RecyclerView recyclerView = new RecyclerView(this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        GoalSelectionAdapter adapter = new GoalSelectionAdapter(new ArrayList<>(), selectedGoals);
//        recyclerView.setAdapter(adapter);
//
//        // Загружаем цели
//        goalViewModel.getAllGoals().observe(this, goals -> {
//            if (goals != null) {
//                adapter.updateGoals(goals);
//            }
//        });
//
//        builder.setView(recyclerView);
//
//        builder.setPositiveButton("Выбрать", (dialog, which) -> {
//            // Обновляем crossRefs
//            crossRefs.clear();
//            for (GoalEntity goal : adapter.getSelectedGoals()) {
//                GoalDepositCrossRefEntity crossRef = new GoalDepositCrossRefEntity(goal.getId(),
//                        autoDeposit != null ? autoDeposit.getId() : "");
//                crossRefs.add(crossRef);
//            }
//        });
//
//        builder.setNegativeButton("Отмена", null);
//
//        builder.show();
//    }

    private void showContextMenu(AutoDepositEntity autoDeposit, View view) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_auto_deposit_context, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_edit) {
                showEditAutoDepositDialog(autoDeposit);
                return true;
            } else if (id == R.id.action_delete) {
                showDeleteDialog(autoDeposit);
                return true;
            } else if (id == R.id.action_execute_now) {
                autoDepositViewModel.executeAutoDeposit(autoDeposit.getId());
                autoDepositExecuted(this);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void showDeleteDialog(AutoDepositEntity autoDeposit) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление автопополнения")
                .setMessage("Вы уверены, что хотите удалить это автопополнение?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    autoDepositViewModel.deleteAutoDeposit(autoDeposit);
                    autoDepositDeleted(this);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_auto_deposit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_execute_all) {
            autoDepositViewModel.executeDueAutoDeposits();
            autoDepositExecuted(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}