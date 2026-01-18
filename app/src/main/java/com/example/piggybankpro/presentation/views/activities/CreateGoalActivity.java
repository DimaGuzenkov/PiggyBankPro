package com.example.piggybankpro.presentation.views.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.utils.AmountUtils;
import com.example.piggybankpro.presentation.utils.ViewUtils;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;

import static com.example.piggybankpro.presentation.utils.AmountUtils.amountFromString;
import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;
import static com.example.piggybankpro.presentation.utils.DateUtils.formatDate;
import static com.example.piggybankpro.presentation.utils.ToastUtils.*;

import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class CreateGoalActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextTargetAmount;
    private EditText editTextCurrentAmount;
    private EditText editTextGoalUrl;
    private Button buttonDatePicker;
    private Button buttonClearDate;
    private Button buttonColorPicker;
    private Button buttonSave;
    private ImageView imageViewColorPreview;
    private EditText editTextOrderPosition;

    private GoalViewModel goalViewModel;
    private Date targetDate;
    private int selectedColor = Color.parseColor("#4CAF50");
    private String editingGoalId = null;

    private String parentId;
    private Integer orderPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Новая цель");

        goalViewModel = new androidx.lifecycle.ViewModelProvider(this).get(GoalViewModel.class);

        editingGoalId = getIntent().getStringExtra("goal_id");
        parentId = getIntent().getStringExtra("parent_id");
        orderPosition = getIntent().getIntExtra("order_position", 0);

        initViews();

        setupDatePicker();

        setupListeners();

        if (editingGoalId != null) {
            loadGoalData();
        }

        observeViewModel();
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextTargetAmount = findViewById(R.id.edit_text_target_amount);
        editTextCurrentAmount = findViewById(R.id.edit_text_current_amount);
        editTextGoalUrl = findViewById(R.id.edit_text_goal_url);
        buttonDatePicker = findViewById(R.id.button_date_picker);
        buttonClearDate = findViewById(R.id.button_clear_date);
        buttonColorPicker = findViewById(R.id.button_color_picker);
        buttonSave = findViewById(R.id.button_save);
        imageViewColorPreview = findViewById(R.id.image_view_color_preview);
        editTextOrderPosition = findViewById(R.id.edit_text_order_position);

        updateDateButtonText();
        updateColorPreview();
    }

    private void setupListeners() {
        editTextTargetAmount.addTextChangedListener(new AmountTextWatcher(editTextTargetAmount));
        editTextCurrentAmount.addTextChangedListener(new AmountTextWatcher(editTextCurrentAmount));

        buttonDatePicker.setOnClickListener(v -> showDatePickerDialog());
        buttonColorPicker.setOnClickListener(v -> showColorPickerDialog());
        buttonSave.setOnClickListener(v -> saveGoal());
    }

    private void setupDatePicker() {
        buttonDatePicker.setOnClickListener(v -> showDatePickerDialog());

        buttonClearDate.setOnClickListener(v -> {
            targetDate = null;
            buttonDatePicker.setText("Выбрать дату");
            buttonClearDate.setVisibility(View.GONE);
        });

        buttonClearDate.setVisibility(View.GONE);
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

                        buttonDatePicker.setText(formatDate(targetDate));
                        buttonClearDate.setVisibility(View.VISIBLE);
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
            buttonDatePicker.setText(formatDate(targetDate));
        }
    }

    private void updateColorPreview() {
        imageViewColorPreview.setBackgroundColor(selectedColor);
    }

    private void saveGoal() {
        if (TextUtils.isEmpty(editTextTitle.getText().toString().trim())) {
            editTextTitle.setError("Введите название цели");
            editTextTitle.requestFocus();
            return;
        }

        GoalEntity goal = new GoalEntity();

        goal.setId(Objects.requireNonNullElseGet(editingGoalId, () -> UUID.randomUUID().toString()));

        goal.setTitle(editTextTitle.getText().toString().trim());
        goal.setDescription(editTextDescription.getText().toString().trim());

        var res = amountFromString(editTextTargetAmount.getText().toString());
        if (!res.parsed()) {
            editTextTargetAmount.setError("Неверный формат числа");
            editTextTargetAmount.requestFocus();
            return;
        }
        goal.setTargetAmount(res.result());

        res = amountFromString(editTextCurrentAmount.getText().toString());
        if (!res.parsed()) {
            editTextCurrentAmount.setError("Неверный формат числа");
            editTextCurrentAmount.requestFocus();
            return;
        }
        goal.setCurrentAmount(res.result() != null ? res.result() : 0.0);

        goal.setTargetDate(targetDate);
        goal.setColor(selectedColor);
        goal.setGoalUrl(editTextGoalUrl.getText().toString().trim());
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

            editTextTitle.setText(goal.getTitle());
            editTextDescription.setText(goal.getDescription());

            if (goal.getTargetAmount() != null) {
                editTextTargetAmount.setText(formatAmount(goal.getTargetAmount()));
            }

            editTextCurrentAmount.setText(formatAmount(goal.getCurrentAmount()));
            editTextOrderPosition.setText(String.valueOf(goal.getOrderPosition()));

            if (goal.getGoalUrl() != null) {
                editTextGoalUrl.setText(goal.getGoalUrl());
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