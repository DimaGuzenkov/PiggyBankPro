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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.AutoDepositEntity;
import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.databinding.ActivityCreateAutoDepositBinding;
import com.example.piggybankpro.databinding.ActivityMainBinding;
import com.example.piggybankpro.presentation.adapters.CrossRefsAdapter;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.utils.AmountUtils;
import com.example.piggybankpro.presentation.utils.DateUtils;
import com.example.piggybankpro.presentation.utils.SwipeItemTouchHelperCallback;
import com.example.piggybankpro.presentation.utils.ToastUtils;
import com.example.piggybankpro.presentation.viewmodels.AutoDepositViewModel;
import com.example.piggybankpro.presentation.views.dialogs.GoalSelectionDialogFragment;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class CreateAutoDepositActivity extends AppCompatActivity {
    private ActivityCreateAutoDepositBinding binding;

    private AutoDepositViewModel autoDepositViewModel;
    private CrossRefsAdapter crossRefsAdapter;

    private String editingDepositId;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCreateAutoDepositBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        autoDepositViewModel = new ViewModelProvider(this).get(AutoDepositViewModel.class);

        editingDepositId = getIntent().getStringExtra("auto_deposit_id");

        setupRecyclerView();
        setupPeriodAdapter();
        setupListeners();

        if (editingDepositId != null) {
            loadAutoDepositData();
            loadCrossRefs();
        }

        observeViewModel();
    }

    private void setupRecyclerView() {
        crossRefsAdapter = new CrossRefsAdapter(new ArrayList<>(), new Listener());
        binding.recyclerViewCrossRefs.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewCrossRefs.setAdapter(crossRefsAdapter);

        binding.recyclerViewCrossRefs.addItemDecoration(
            new DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        );

        var itemTouchHelper = new ItemTouchHelper(new SwipeItemTouchHelperCallback(crossRefsAdapter));
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewCrossRefs);
    }

    private void setupPeriodAdapter() {
        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.period_types,
                android.R.layout.simple_spinner_item
        );
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFrequency.setAdapter(periodAdapter);
    }

    private void setupListeners() {
        binding.editTextAmount.addTextChangedListener(new AmountTextWatcher(binding.editTextAmount));
        binding.editTextAmount.addTextChangedListener(new TextWatcher() {
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
        binding.buttonSave.setOnClickListener(v -> saveAutoDeposit());
        binding.buttonAddGoal.setOnClickListener(v -> {
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

            binding.editTextName.setText(autoDeposit.getName());
            binding.editTextAmount.setText(AmountUtils.formatAmount(autoDeposit.getAmount()));
            binding.spinnerFrequency.setSelection(autoDeposit.getPeriodType());
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
        String name = binding.editTextName.getText().toString().trim();

        if (name.isEmpty()) {
            binding.editTextName.setError("Введите название");
            binding.editTextName.requestFocus();
            return;
        }

        var amount = amountFromString(binding.editTextAmount.getText().toString()).result();
        if (amount == null) {
            binding.editTextAmount.setError("Неверный формат числа");
            binding.editTextAmount.requestFocus();
            return;
        }

        if (amount <= 0.0) {
            binding.editTextAmount.setError("Неверный формат числа");
            binding.editTextAmount.requestFocus();
            return;
        }

        if (amount < crossRefsAdapter.getTotalAmount()) {
            binding.editTextAmount.setError("Недостаточно средств для распределения");
            binding.editTextAmount.requestFocus();
            return;
        }

        AutoDepositEntity autoDeposit = new AutoDepositEntity();

        autoDeposit.setId(Objects.requireNonNullElseGet(editingDepositId, () -> UUID.randomUUID().toString()));
        autoDeposit.setName(name);
        autoDeposit.setAmount(amount);
        autoDeposit.setPeriodType(DateUtils.getPeriodTypeFromPosition(binding.spinnerFrequency.getSelectedItemPosition()));

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
        binding.textViewSumLeft.setText(AmountUtils.formatAmount(text));

        if (text < 0.0) {
            binding.textViewSumLeft.setTextColor(Color.RED);
            return false;
        } else {
            binding.textViewSumLeft.setTextColor(Color.GREEN);
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
                binding.textViewEmpty.setVisibility(TextView.VISIBLE);
                binding.recyclerViewCrossRefs.setVisibility(TextView.GONE);
            } else {
                binding.textViewEmpty.setVisibility(TextView.GONE);
                binding.recyclerViewCrossRefs.setVisibility(TextView.VISIBLE);
            }
        }
    }
}
