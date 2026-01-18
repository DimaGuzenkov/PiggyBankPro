package com.example.piggybankpro.presentation.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;
import com.example.piggybankpro.presentation.viewmodels.SharedViewModel;

import java.util.Locale;

public class QuickEditDialog extends DialogFragment {

    public interface OnQuickEditListener {
        void onQuickEditCompleted();
    }

    private GoalEntity goal;
    private OnQuickEditListener listener;
    private GoalViewModel goalViewModel;
    private SharedViewModel sharedViewModel;

    private TextView textViewGoalTitle;
    private TextView textViewCurrentAmount;
    private EditText editTextAmount;
    private RadioGroup radioGroupOperation;
    private RadioButton radioButtonDeposit;
    private RadioButton radioButtonWithdraw;
    private EditText editTextDescription;
    private Button buttonConfirm;
    private Button buttonCancel;

    public QuickEditDialog() {
        // Пустой конструктор
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnQuickEditListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnQuickEditListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_quick_edit, container, false);

        // Инициализация ViewModel
        goalViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(GoalViewModel.class);
        sharedViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Инициализация представлений
        initViews(view);

        // Наблюдение за выбранной целью
        sharedViewModel.getSelectedGoalForOperation().observe(getViewLifecycleOwner(), selectedGoal -> {
            if (selectedGoal != null) {
                this.goal = selectedGoal;
                updateUI();
            }
        });

        // Наблюдение за предзаполненными данными
        sharedViewModel.getQuickEditAmount().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null && editTextAmount != null) {
                editTextAmount.setText(String.format(Locale.getDefault(), "%.2f", amount));
            }
        });

        sharedViewModel.getIsQuickEditDeposit().observe(getViewLifecycleOwner(), isDeposit -> {
            if (isDeposit != null && radioGroupOperation != null) {
                if (isDeposit) {
                    radioButtonDeposit.setChecked(true);
                } else {
                    radioButtonWithdraw.setChecked(true);
                }
            }
        });

        sharedViewModel.getQuickEditDescription().observe(getViewLifecycleOwner(), description -> {
            if (description != null && editTextDescription != null) {
                editTextDescription.setText(description);
            }
        });

        // Настройка слушателей
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        textViewGoalTitle = view.findViewById(R.id.text_view_goal_title);
        textViewCurrentAmount = view.findViewById(R.id.text_view_current_amount);
        editTextAmount = view.findViewById(R.id.edit_text_amount);
        radioGroupOperation = view.findViewById(R.id.radio_group_operation);
        radioButtonDeposit = view.findViewById(R.id.radio_button_deposit);
        radioButtonWithdraw = view.findViewById(R.id.radio_button_withdraw);
        editTextDescription = view.findViewById(R.id.edit_text_description);
        buttonConfirm = view.findViewById(R.id.button_confirm);
        buttonCancel = view.findViewById(R.id.button_cancel);
    }

    private void updateUI() {
        if (goal != null) {
            textViewGoalTitle.setText(goal.getTitle());
            textViewCurrentAmount.setText(formatCurrency(goal.getCurrentAmount()));

            // Устанавливаем максимальную сумму для списания
            if (radioButtonWithdraw.isChecked()) {
                editTextAmount.setHint("Макс: " + formatCurrency(goal.getCurrentAmount()));
            }
        }
    }

    private void setupListeners() {
        // Слушатель для изменения суммы
        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInput();
            }
        });

        // Слушатель для радио-кнопок
        radioGroupOperation.setOnCheckedChangeListener((group, checkedId) -> {
            validateInput();
            updateUI(); // Обновляем подсказку
        });

        // Кнопка подтверждения
        buttonConfirm.setOnClickListener(v -> {
            performOperation();
        });

        // Кнопка отмены
        buttonCancel.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void validateInput() {
        String amountStr = editTextAmount.getText().toString().trim();
        boolean isDeposit = radioButtonDeposit.isChecked();

        if (amountStr.isEmpty()) {
            buttonConfirm.setEnabled(false);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            if (amount <= 0) {
                buttonConfirm.setEnabled(false);
                return;
            }

            // Для списания проверяем, достаточно ли средств
            if (!isDeposit && goal != null && amount > goal.getCurrentAmount()) {
                buttonConfirm.setEnabled(false);
                return;
            }

            buttonConfirm.setEnabled(true);

        } catch (NumberFormatException e) {
            buttonConfirm.setEnabled(false);
        }
    }

    private void performOperation() {
        if (goal == null) {
            Toast.makeText(getContext(), "Цель не выбрана", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = editTextAmount.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        boolean isDeposit = radioButtonDeposit.isChecked();

        try {
            double amount = Double.parseDouble(amountStr);

            if (isDeposit) {
                goalViewModel.depositToGoal(goal.getId(), amount, description);
                Toast.makeText(getContext(), "Цель пополнена", Toast.LENGTH_SHORT).show();
            } else {
                goalViewModel.withdrawFromGoal(goal.getId(), amount, description);
                Toast.makeText(getContext(), "Средства списаны", Toast.LENGTH_SHORT).show();
            }

            // Сбрасываем SharedViewModel
            sharedViewModel.resetQuickEdit();

            // Уведомляем слушателя
            if (listener != null) {
                listener.onQuickEditCompleted();
            }

            dismiss();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Неверный формат суммы", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "%.2f ₽", amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Сбрасываем данные при закрытии диалога
        sharedViewModel.resetQuickEdit();
    }
}