package com.example.piggybankpro.presentation.adapters.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.AutoDepositEntity;
import com.example.piggybankpro.databinding.ItemAutoDepositBinding;
import com.example.piggybankpro.presentation.utils.AmountUtils;
import com.example.piggybankpro.presentation.utils.DateUtils;

public class AutoDepositViewHolder extends RecyclerView.ViewHolder {
    private final ItemAutoDepositBinding binding;

    public AutoDepositViewHolder(@NonNull ItemAutoDepositBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(AutoDepositEntity autoDeposit) {
        binding.textViewName.setText(autoDeposit.getName());

        binding.textViewAmount.setText(AmountUtils.formatAmount(autoDeposit.getAmount()));

        binding.textViewPeriod.setText(DateUtils.getPeriodString(autoDeposit.getPeriodType()));

        if (autoDeposit.getNextExecutionDate() != null) {
            binding.textViewNextExecution.setText(DateUtils.formatDate(autoDeposit.getNextExecutionDate()));
        } else {
            binding.textViewNextExecution.setText("Не запланировано");
        }

        binding.switchActive.setChecked(autoDeposit.getIsActive());

        binding.imageViewIcon.setImageResource(R.drawable.ic_money);

        if (autoDeposit.getIsActive()) {
            binding.cardViewAutoDeposit.setCardBackgroundColor(
                    itemView.getContext().getResources().getColor(R.color.active, null)
            );
        } else {
            binding.cardViewAutoDeposit.setCardBackgroundColor(
                    itemView.getContext().getResources().getColor(R.color.inactive, null)
            );
        }
    }

    public ItemAutoDepositBinding getBinding() {
        return binding;
    }
}
