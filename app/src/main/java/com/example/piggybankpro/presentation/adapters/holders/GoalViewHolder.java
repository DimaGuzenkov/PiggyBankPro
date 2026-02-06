package com.example.piggybankpro.presentation.adapters.holders;

import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;
import static com.example.piggybankpro.presentation.utils.DateUtils.formatDays;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.databinding.ItemGoalBinding;
import com.example.piggybankpro.presentation.utils.ViewUtils;

public class GoalViewHolder extends RecyclerView.ViewHolder {
    ItemGoalBinding binding;

    public GoalViewHolder(@NonNull ItemGoalBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(GoalEntity goal) {
        binding.textViewTitle.setText(goal.getTitle());

//        if (goal.getTargetAmount() != null) {
//            binding.textViewTargetAmount.setText(formatAmount(goal.getTargetAmount()));
//            binding.textViewTargetAmount.setVisibility(View.VISIBLE);
//        } else {
//            binding.textViewTargetAmount.setVisibility(View.GONE);
//        }

        binding.textViewTotalAmount.setText(formatAmount(goal.getCalculatedAmount()));
        binding.textViewOwnAmount.setText(formatAmount(goal.getCurrentAmount()));

        Double progress = goal.getProgressPercentage();
        if (progress != null) {
//            ViewUtils.updateGoalProgress(binding.progressBar, binding.textViewProgressPercentage, progress);
            binding.progressBar.setProgress(progress);
        } else {
//            binding.textViewProgressPercentage.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
        }

        Long daysLeft = goal.getDaysRemaining();
        if (daysLeft != null) {
            binding.textViewDaysLeft.setText(formatDays(daysLeft));
            binding.textViewDaysLeft.setVisibility(View.VISIBLE);
        } else {
            binding.textViewDaysLeft.setVisibility(View.GONE);
        }

//        if (goal.getColor() != null) {
//            binding.imageViewIcon.setColorFilter(goal.getColor());
//        }

//        if (goal.getIsCompleted() != null && goal.getIsCompleted()) {
//            binding.imageViewCompleted.setVisibility(View.VISIBLE);
//            binding.cardViewGoal.setAlpha(0.8f);
//        } else {
//            binding.imageViewCompleted.setVisibility(View.GONE);
//            binding.cardViewGoal.setAlpha(1.0f);
//        }

//        binding.imageViewIcon.setImageResource(R.drawable.ic_goal);
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
