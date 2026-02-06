package com.example.piggybankpro.presentation.views.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.piggybankpro.data.local.converters.IdConverter;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.data.local.entities.Id;
import com.example.piggybankpro.databinding.ActivityCreateGoalBinding;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;

import static com.example.piggybankpro.presentation.utils.AmountUtils.*;
import static com.example.piggybankpro.presentation.utils.DateUtils.formatDate;
import static com.example.piggybankpro.presentation.utils.ToastUtils.*;

import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class CreateGoalActivity extends AppCompatActivity {
    private ActivityCreateGoalBinding binding;

    private GoalViewModel goalViewModel;
    private Date targetDate;
    private int selectedColor = Color.parseColor("#4CAF50");
    private Id editingGoalId = null;

    private Id parentId;
    private Integer orderPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCreateGoalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Новая цель");

        goalViewModel = new androidx.lifecycle.ViewModelProvider(this).get(GoalViewModel.class);

        editingGoalId = IdConverter.fromStrToId(getIntent().getStringExtra("goal_id"));
        parentId = IdConverter.fromStrToId(getIntent().getStringExtra("parent_id"));
        orderPosition = getIntent().getIntExtra("order_position", 0);

        updateDateButtonText();
        updateColorPreview();
        setupDatePicker();

        setupListeners();

        if (editingGoalId != null) {
            loadGoalData();
        }

        observeViewModel();
    }

    private void setupListeners() {
        binding.editTextTargetAmount.addTextChangedListener(new AmountTextWatcher(binding.editTextTargetAmount));
        binding.editTextCurrentAmount.addTextChangedListener(new AmountTextWatcher(binding.editTextCurrentAmount));

        binding.buttonDatePicker.setOnClickListener(v -> showDatePickerDialog());
        binding.buttonColorPicker.setOnClickListener(v -> showColorPickerDialog());
        binding.buttonSave.setOnClickListener(v -> saveGoal());
    }

    private void setupDatePicker() {
        binding.buttonDatePicker.setOnClickListener(v -> showDatePickerDialog());

        binding.buttonClearDate.setOnClickListener(v -> {
            targetDate = null;
            binding.buttonDatePicker.setText("Выбрать дату");
            binding.buttonClearDate.setVisibility(View.GONE);
        });

        binding.buttonClearDate.setVisibility(View.GONE);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, monthOfYear, dayOfMonth);
                        targetDate = selectedCalendar.getTime();

                        binding.buttonDatePicker.setText(formatDate(targetDate));
                        binding.buttonClearDate.setVisibility(View.VISIBLE);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showColorPickerDialog() {
        new ColorPickerDialog.Builder(this)
                .setTitle("Выберите цвет")
                .setColorShape(ColorShape.SQAURE)
                .setDefaultColor(selectedColor)
                .setColorListener(new ColorListener() {
                    @Override
                    public void onColorSelected(int color, @NonNull String colorHex) {
                        selectedColor = color;
                        updateColorPreview();
                    }
                })
                .show();
    }

    private void updateDateButtonText() {
        if (targetDate != null) {
            binding.buttonDatePicker.setText(formatDate(targetDate));
        }
    }

    private void updateColorPreview() {
        binding.imageViewColorPreview.setBackgroundColor(selectedColor);
    }

    private void saveGoal() {
        if (TextUtils.isEmpty(binding.editTextTitle.getText().toString().trim())) {
            binding.editTextTitle.setError("Введите название цели");
            binding.editTextTitle.requestFocus();
            return;
        }

        GoalEntity goal = new GoalEntity();

        goal.setId(Id.createIfNull(editingGoalId));

        goal.setTitle(binding.editTextTitle.getText().toString().trim());
        goal.setDescription(binding.editTextDescription.getText().toString().trim());

        var res = amountFromString(binding.editTextTargetAmount.getText().toString());
        if (!res.parsed()) {
            binding.editTextTargetAmount.setError("Неверный формат числа");
            binding.editTextTargetAmount.requestFocus();
            return;
        }
        goal.setTargetAmount(res.result());

        res = amountFromString(binding.editTextCurrentAmount.getText().toString());
        if (!res.parsed()) {
            binding.editTextCurrentAmount.setError("Неверный формат числа");
            binding.editTextCurrentAmount.requestFocus();
            return;
        }
        goal.setCurrentAmount(res.result() != null ? res.result() : 0.0);

        goal.setTargetDate(targetDate);
        goal.setColor(selectedColor);
        goal.setGoalUrl(binding.editTextGoalUrl.getText().toString().trim());
        goal.setParentId(parentId);
        goal.setOrderPosition(orderPosition);

        if (editingGoalId != null) {
            goalViewModel.updateGoal(goal);
            goalUpdated(this);
        } else {
            goalViewModel.createGoal(goal);
            goalCreated(this);
        }

        finish();
    }

    private void loadGoalData() {
        if (editingGoalId == null) return;

        goalViewModel.getGoalById(editingGoalId).observe(this, goal -> {
            if (goal == null) {
                return;
            }

            binding.editTextTitle.setText(goal.getTitle());
            binding.editTextDescription.setText(goal.getDescription());

            if (goal.getTargetAmount() != null) {
                binding.editTextTargetAmount.setText(formatAmount(goal.getTargetAmount()));
            }

            binding.editTextCurrentAmount.setText(formatAmount(goal.getCurrentAmount()));
            binding.editTextOrderPosition.setText(String.valueOf(goal.getOrderPosition()));

            if (goal.getGoalUrl() != null) {
                binding.editTextGoalUrl.setText(goal.getGoalUrl());
            }

            if (goal.getColor() != null) {
                selectedColor = goal.getColor();
                updateColorPreview();
            }

            parentId = goal.getParentId();
        });
    }

    private void observeViewModel() {
        goalViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                display(this, errorMessage);
                goalViewModel.clearError();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}