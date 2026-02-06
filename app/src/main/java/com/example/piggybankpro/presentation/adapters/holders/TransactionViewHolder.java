package com.example.piggybankpro.presentation.adapters.holders;

import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;
import static com.example.piggybankpro.presentation.utils.DateUtils.formatDate;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.TransactionEntity;
import com.example.piggybankpro.databinding.ItemTransactionBinding;

public class TransactionViewHolder extends RecyclerView.ViewHolder {
    private static final int DepositColor = 0xFF4CAF50;
    private static final int WithdrawColor = 0xFFF44336;
    private final ItemTransactionBinding binding;

    public TransactionViewHolder(@NonNull ItemTransactionBinding binding) {
        super(binding.getRoot());
        this.binding = binding;

        binding.buttonDelete.setOnClickListener(v -> {

        });
    }

    public void bind(TransactionEntity transaction) {
        binding.textViewAmount.setText(formatAmount(transaction.getAmount()));

        binding.textViewDescription.setText(transaction.getDescription());

        binding.textViewDate.setText(formatDate(transaction.getTransactionDate()));

        binding.textViewType.setText(transaction.getTransactionTypeString());
        binding.textViewType.setBackgroundColor(transaction.getAmount() < 0 ? WithdrawColor : DepositColor);
    }
}
