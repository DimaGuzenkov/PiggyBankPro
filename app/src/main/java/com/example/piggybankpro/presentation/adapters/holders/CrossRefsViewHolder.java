package com.example.piggybankpro.presentation.adapters.holders;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.databinding.ItemCrossRefBinding;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.utils.AmountUtils;

public class CrossRefsViewHolder extends RecyclerView.ViewHolder {
    private final ItemCrossRefBinding binding;
    private final OnChangeListener listener;

    public CrossRefsViewHolder(@NonNull ItemCrossRefBinding binding, OnChangeListener listener) {
        super(binding.getRoot());
        this.binding = binding;
        this.listener = listener;

        binding.editTextAmount.addTextChangedListener(new AmountTextWatcher(binding.editTextAmount));
        binding.editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                var amount = AmountUtils.amountFromString(editable.toString()).result();
                if (amount == null) {
                    amount = 0.0;
                }

                int position = getAbsoluteAdapterPosition();
                if (!listener.changeAmount(amount, position)) {
                    binding.editTextAmount.setText("Недостаточно средств");
                    binding.editTextAmount.requestFocus();
                }
            }
        });
    }

    public void bind(GoalDepositCrossRefEntity crossRef) {
        binding.textViewTitle.setText(crossRef.getGoalTitle());
        if (crossRef.getAmount() != null) {
            binding.editTextAmount.setText(AmountUtils.formatAmount(crossRef.getAmount()));
        }
    }

    public interface OnChangeListener {
        boolean changeAmount(double amount, int position);
    }
}
