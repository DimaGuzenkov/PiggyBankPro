package com.example.piggybankpro.presentation.views.dialogs;

import static com.example.piggybankpro.presentation.utils.ToastUtils.depositAmount;
import static com.example.piggybankpro.presentation.utils.ToastUtils.withdrawAmount;

import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.utils.AmountUtils;
import com.example.piggybankpro.presentation.utils.ToastUtils;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;

import java.text.ParseException;

public class ChangeAmountDialogs {
    private final GoalViewModel goalViewModel;
    private final AppCompatActivity activity;

    private EditText editTextAmount;
    private EditText editTextDescription;
    private Button buttonGoalPicker;

    private GoalEntity destGoal = null;

    public ChangeAmountDialogs(AppCompatActivity activity, GoalViewModel goalViewModel) {
        this.activity = activity;
        this.goalViewModel = goalViewModel;
    }

    private AlertDialog.Builder initUi(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_deposit_withdraw, null);
        builder.setView(dialogView);

        editTextAmount = dialogView.findViewById(R.id.edit_text_amount);
        editTextDescription = dialogView.findViewById(R.id.edit_text_description);
        buttonGoalPicker = dialogView.findViewById(R.id.button_goal_picker);

        editTextAmount.addTextChangedListener(new AmountTextWatcher(editTextAmount));

        buttonGoalPicker.setOnClickListener(v -> {
            GoalSelectionDialogFragment dialog = GoalSelectionDialogFragment.newInstance();
            dialog.setOnGoalSelectedListener(goal -> {
                destGoal = goal;
                buttonGoalPicker.setText(destGoal.getTitle());
            });

            dialog.show(activity.getSupportFragmentManager(), "GoalSelectionDialog");
        });

        builder.setNegativeButton("Отмена", null);

        return builder;
    }

    public void showDepositDialog(String goalId) {
        var builder = initUi("Пополнение цели");
        buttonGoalPicker.setVisibility(View.GONE);
        builder.setPositiveButton("Пополнить", (dialog, which) ->
            depositDialog(goalId)
        );
        builder.show();
    }

    private void depositDialog(String goalId) {
        String description = editTextDescription.getText().toString().trim();

        var amount = AmountUtils.amountFromString(editTextAmount.getText().toString()).result();
        if (amount == null) {
            editTextAmount.setError("Неверный формат суммы");
            editTextAmount.requestFocus();
            return;
        }

        if (amount > 0) {
            goalViewModel.depositToGoal(goalId, amount, description);
            depositAmount(activity);
        } else {
            editTextAmount.setError("Неверный формат суммы");
            editTextAmount.requestFocus();
        }
    }

    public void showWithdrawDialog(String goalId, double goalAmount) {
        var builder = initUi("Списание с цели");
        buttonGoalPicker.setVisibility(View.GONE);
        builder.setPositiveButton("Списать", (dialog, which) ->
                withdrawDialog(goalId, goalAmount));
        builder.show();
    }

    private void withdrawDialog(String goalId, double goalAmount) {
        String description = editTextDescription.getText().toString().trim();

        var amount = AmountUtils.amountFromString(editTextAmount.getText().toString()).result();
        if (amount == null) {
            editTextAmount.setError("Неверный формат суммы");
            editTextAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            editTextAmount.setError("Неверный формат суммы");
            editTextAmount.requestFocus();
            return;
        }

        if (goalAmount >= amount) {
            goalViewModel.withdrawFromGoal(goalId, amount, description);
            withdrawAmount(activity);
        } else {
            editTextAmount.setError("Недостаточно средств");
            editTextAmount.requestFocus();
        }
    }

    public void showTransferDialog(String sourceGoalId, double amount) {
        var builder = initUi("Перевод средств");
        buttonGoalPicker.setVisibility(View.VISIBLE);

        builder.setPositiveButton("Перевести", (dialog, which) ->
            transferDialog(sourceGoalId, amount)
        );
        builder.show();
    }

    private void transferDialog(String sourceGoalId, double sourceAmount) {
        if (destGoal == null) {
            buttonGoalPicker.setError("Выберите цель для перевода");
            buttonGoalPicker.requestFocus();
            return;
        }

        if (sourceGoalId.equals(destGoal.getId())) {
            buttonGoalPicker.setError("Выберите другую цель");
            buttonGoalPicker.requestFocus();
            return;
        }

        var amount = AmountUtils.amountFromString(editTextAmount.getText().toString()).result();
        if (amount == null) {
            editTextAmount.setError("Неверный формат суммы");
            editTextAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            editTextAmount.setError("Неверный формат суммы");
            editTextAmount.requestFocus();
            return;
        }

        if (sourceAmount >= amount) {
            String description = editTextDescription.getText().toString().trim();
            goalViewModel.transferBetweenGoals(sourceGoalId, destGoal.getId(), amount, description);
        } else {
            editTextAmount.setError("Недостаточно средств");
            editTextAmount.requestFocus();
        }
    }
}
