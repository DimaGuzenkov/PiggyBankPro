package com.example.piggybankpro.presentation.adapters.holders;

import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;
import static com.example.piggybankpro.presentation.utils.DateUtils.formatDays;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.databinding.ItemGoalBinding;

public class GoalViewHolder extends RecyclerView.ViewHolder {
    ItemGoalBinding binding;

    public GoalViewHolder(@NonNull ItemGoalBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(GoalEntity goal) {
        binding.textViewTitle.setText(goal.getTitle());

        binding.textViewTotalAmount.setText(formatAmount(goal.getCalculatedAmount()));
        binding.textViewOwnAmount.setText(formatAmount(goal.getCurrentAmount()));
        if (goal.getCalculatedAmount().equals(goal.getCurrentAmount())) {
            binding.textViewOwnAmount.setVisibility(View.GONE);
        } else {
            binding.textViewOwnAmount.setVisibility(View.VISIBLE);
        }

        Double progress = goal.getProgressPercentage();
        if (progress != null) {
            binding.progressBar.setProgress(progress);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
        }

        Long daysLeft = goal.getDaysRemaining();
        if (daysLeft != null) {
            binding.textViewDaysLeft.setText(formatDays(daysLeft));
            binding.textViewDaysLeft.setVisibility(View.VISIBLE);
        } else {
            binding.textViewDaysLeft.setVisibility(View.GONE);
        }
    }

    public void setHighlighted(boolean highlighted) {
        if (highlighted) {
            binding.cardViewGoal.setCardBackgroundColor(
                    itemView.getContext().getResources().getColor(R.color.highlight_color, null)
            );
            binding.cardViewGoal.setCardElevation(8f);
        } else {
            binding.cardViewGoal.setCardBackgroundColor(
                    itemView.getContext().getResources().getColor(R.color.surface, null)
            );
            binding.cardViewGoal.setCardElevation(2f);
        }
    }

    public ItemGoalBinding getBinding() {
        return binding;
    }
}
