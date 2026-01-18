package com.example.piggybankpro.presentation.views.activities;

import static com.example.piggybankpro.presentation.utils.AmountUtils.amountFromString;
import static com.example.piggybankpro.presentation.utils.ToastUtils.autoDepositCreated;
import static com.example.piggybankpro.presentation.utils.ToastUtils.autoDepositUpdated;
import static com.example.piggybankpro.presentation.utils.ToastUtils.display;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.AutoDepositEntity;
import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.presentation.adapters.CrossRefsAdapter;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.utils.AmountUtils;
import com.example.piggybankpro.presentation.utils.DateUtils;
import com.example.piggybankpro.presentation.utils.ToastUtils;
import com.example.piggybankpro.presentation.viewmodels.AutoDepositViewModel;
import com.example.piggybankpro.presentation.views.dialogs.GoalSelectionDialogFragment;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class CreateAutoDepositActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextAmount;
    private Spinner spinnerPeriod;;
    private TextView textViewSumLeft;
    private Button buttonSelectGoals;
    private RecyclerView recyclerViewCrossRefs;
    private Button buttonSave;
    private TextView textViewEmpty;

    private AutoDepositViewModel autoDepositViewModel;
    private CrossRefsAdapter crossRefsAdapter;

    private String editingDepositId;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_auto_deposit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        autoDepositViewModel = new ViewModelProvider(this).get(AutoDepositViewModel.class);

        editingDepositId = getIntent().getStringExtra("auto_deposit_id");

        initViews();
        setupRecyclerView();
        setupPeriodAdapter();
        setupListeners();

        if (editingDepositId != null) {
            loadAutoDepositData();
            loadCrossRefs();
        }

        observeViewModel();
    }

    private void initViews() {
        editTextName = findViewById(R.id.edit_text_name);
        editTextAmount = findViewById(R.id.edit_text_amount);
        spinnerPeriod = findViewById(R.id.spinner_frequency);
        textViewSumLeft = findViewById(R.id.sum_left);
        buttonSelectGoals = findViewById(R.id.button_add_goal);
        recyclerViewCrossRefs = findViewById(R.id.recycler_view_selected_goals);
        buttonSave = findViewById(R.id.button_save);
        textViewEmpty = findViewById(R.id.text_view_empty);

        textViewEmpty.setVisibility(View.VISIBLE);
        recyclerViewCrossRefs.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        crossRefsAdapter = new CrossRefsAdapter(new ArrayList<>(), new Listener());
        recyclerViewCrossRefs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCrossRefs.setAdapter(crossRefsAdapter);

        recyclerViewCrossRefs.addItemDecoration(
            new DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        );
    }

    private void setupPeriodAdapter() {
        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.period_types,
                android.R.layout.simple_spinner_item
        );
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);
    }

    private void setupListeners() {
        editTextAmount.addTextChangedListener(new AmountTextWatcher(editTextAmount));
        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                var res = AmountUtils.amountFromString(editable.toString());
                if (res.parsed()) {
                    amount = res.result() != null ? res.result() : 0.0;
                    updateSumLeft();
                }
            }
        });
        buttonSave.setOnClickListener(v -> saveAutoDeposit());
        buttonSelectGoals.setOnClickListener(v -> {
            GoalSelectionDialogFragment dialog = GoalSelectionDialogFragment.newInstance();
            dialog.setOnGoalSelectedListener(goal -> {
                crossRefsAdapter.addCrossRef(new GoalDepositCrossRefEntity(goal.getId(), goal.getTitle(), editingDepositId));
            });

            dialog.show(getSupportFragmentManager(), "GoalSelectionDialog");
        });
    }

    private void loadAutoDepositData() {
        if (editingDepositId == null) {
            return;
        }

        autoDepositViewModel.getAutoDepositById(editingDepositId).observe(this, autoDeposit -> {
            if (autoDeposit == null) {
                return;
            }

            editTextName.setText(autoDeposit.getName());
            editTextAmount.setText(AmountUtils.formatAmount(autoDeposit.getAmount()));
            spinnerPeriod.setSelection(autoDeposit.getPeriodType());
        });
    }

    private void loadCrossRefs() {
        autoDepositViewModel.getCrossRefsByDepositId(editingDepositId).observe(this, crossRefs -> {
            crossRefsAdapter.updateCrossRefs(Objects.requireNonNullElseGet(crossRefs, ArrayList::new));
        });
    }

    private void observeViewModel() {
        autoDepositViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                display(this, errorMessage);
                autoDepositViewModel.clearError();
            }
        });
    }

    private void saveAutoDeposit() {
        String name = editTextName.getText().toString().trim();

        if (name.isEmpty()) {
            editTextName.setError("Введите название");
            editTextName.requestFocus();
            return;
        }

        var amount = amountFromString(editTextAmount.getText().toString()).result();
        if (amount == null) {
            editTextAmount.setError("Неверный формат числа");
            editTextAmount.requestFocus();
            return;
        }

        if (amount <= 0.0) {
            editTextAmount.setError("Неверный формат числа");
            editTextAmount.requestFocus();
            return;
        }

        if (amount < crossRefsAdapter.getTotalAmount()) {
            editTextAmount.setError("Недостаточно средств для распределения");
            editTextAmount.requestFocus();
            return;
        }

        AutoDepositEntity autoDeposit = new AutoDepositEntity();

        autoDeposit.setId(Objects.requireNonNullElseGet(editingDepositId, () -> UUID.randomUUID().toString()));
        autoDeposit.setName(name);
        autoDeposit.setAmount(amount);
        autoDeposit.setPeriodType(DateUtils.getPeriodTypeFromPosition(spinnerPeriod.getSelectedItemPosition()));

        if (editingDepositId != null) {
            autoDepositViewModel.updateAutoDeposit(autoDeposit, crossRefsAdapter.getCrossRefs());
            autoDepositUpdated(this);
        } else {
            autoDepositViewModel.createAutoDeposit(autoDeposit, crossRefsAdapter.getCrossRefs());
            autoDepositCreated(this);
        }

        finish();
    }

    private boolean updateSumLeft() {
        var text = amount - crossRefsAdapter.getTotalAmount();
        textViewSumLeft.setText(AmountUtils.formatAmount(text));

        if (text < 0.0) {
            textViewSumLeft.setTextColor(Color.RED);
            return false;
        } else {
            textViewSumLeft.setTextColor(Color.GREEN);
            return true;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    class Listener implements CrossRefsAdapter.OnCrossRefsChangeListener {
        @Override
        public boolean onChange() {
            return updateSumLeft();
        }

        @Override
        public void onCrossRefsCountChange() {
            if (crossRefsAdapter.getItemCount() == 0) {
                textViewEmpty.setVisibility(TextView.VISIBLE);
                recyclerViewCrossRefs.setVisibility(TextView.GONE);
            } else {
                textViewEmpty.setVisibility(TextView.GONE);
                recyclerViewCrossRefs.setVisibility(TextView.VISIBLE);
            }
        }
    }
}
