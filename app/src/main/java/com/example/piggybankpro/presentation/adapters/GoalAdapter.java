package com.example.piggybankpro.presentation.adapters;

import android.content.ClipDescription;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.presentation.utils.ViewUtils;

import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;
import static com.example.piggybankpro.presentation.utils.DateUtils.formatDays;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnGoalClickListener {
        void onGoalClick(GoalEntity goal);
        void onGoalLongClick(GoalEntity goal, View view);
        void onGoalDetailsClick(GoalEntity goal);
        void onGoalDroppedOnGoal(GoalEntity draggedGoal, GoalEntity targetGoal);
        void onGoalDroppedBetween(GoalEntity draggedGoal, int insertIndex);
    }

    private static final int TYPE_GOAL = 0;
    private static final int TYPE_DIVIDER = 1;

    private List<GoalEntity> goals = new ArrayList<>();
    private final List<DividerPlaceholder> dividers = new ArrayList<>();
    public final OnGoalClickListener listener;
    private GoalEntity draggedGoal;
    private int highlightedDividerPosition = -1;

    public GoalAdapter(List<GoalEntity> goals, OnGoalClickListener listener) {
        this.listener = listener;
        updateGoals(goals);
    }

    private void createDividers(int count) {
        dividers.clear();
        for (int i = 0; i < count; i++) {
            dividers.add(new DividerPlaceholder());
        }
    }

    public void updateGoals(List<GoalEntity> goals) {
        this.goals = goals;
        createDividers(goals.size() + 1);

        notifyDataSetChanged();
    }

    public int getGoalCount() {
        return goals.size();
    }

    public void removeItem(int position) {
        goals.remove(position / 2);
        dividers.remove(dividers.size() - 1);
        notifyItemRemoved(position);
        notifyItemRemoved(getItemCount() + 1);
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 == 1 ? TYPE_GOAL : TYPE_DIVIDER;
    }

    @Override
    public int getItemCount() {
        return goals.size() + dividers.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_GOAL) {
            View view = inflater.inflate(R.layout.item_goal, parent, false);
            return new GoalViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_divider, parent, false);
            return new DividerViewHolder(view);
        }
    }

    public List<GoalEntity> getGoals() {
        return goals;
    }

    public GoalEntity getGoalByPosition(int position) {
        return goals.get((position - 1) / 2);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GoalViewHolder goalHolder) {
            GoalEntity goal = getGoalByPosition(position);
            goalHolder.bind(goal);

            setupGoalView(goalHolder, position);

        } else if (holder instanceof DividerViewHolder dividerHolder) {
            setupDividerView(dividerHolder, position);
        }
    }

    private void setupGoalView(GoalViewHolder holder, int position) {
        var goal = getGoalByPosition(position);
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoalClick(goal);
            }
        });

        holder.cardView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onGoalLongClick(goal, v);
                return true;
            }
            return false;
        });

        holder.imageViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoalDetailsClick(goal);
            }
        });

        holder.cardView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    ClipDescription clipDescription = event.getClipDescription();
                    return clipDescription != null &&
                            clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                case DragEvent.ACTION_DRAG_ENTERED:
                    if (draggedGoal != null && !draggedGoal.getId().equals(goal.getId())) {
                        holder.setHighlighted(true);
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED, DragEvent.ACTION_DRAG_ENDED:
                    holder.setHighlighted(false);
                    return true;

                case DragEvent.ACTION_DROP:
                    if (draggedGoal != null && !draggedGoal.getId().equals(goal.getId())) {
                        listener.onGoalDroppedOnGoal(draggedGoal, goal);
                    }
                    holder.setHighlighted(false);
                    return true;
            }
            return false;
        });
    }

    public void setDraggedGoal(GoalEntity draggedGoal) {
        this.draggedGoal = draggedGoal;
    }

    private void setupDividerView(DividerViewHolder holder, int position) {
        holder.setHighlighted(position == highlightedDividerPosition);

        holder.itemView.setOnDragListener((v, event) -> {
            var insertIndex = position / 2;
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    ClipDescription clipDescription = event.getClipDescription();
                    return clipDescription != null &&
                            clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                case DragEvent.ACTION_DRAG_ENTERED:
                    var goal = (GoalEntity) event.getLocalState();
                    if (goal.getOrderPosition() != insertIndex && goal.getOrderPosition() != insertIndex - 1) {
                        highlightedDividerPosition = position;
                        notifyItemChanged(position);
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    if (highlightedDividerPosition == position) {
                        highlightedDividerPosition = -1;
                        notifyItemChanged(position);
                    }
                    return true;

                case DragEvent.ACTION_DROP:
                    if (draggedGoal != null && insertIndex != -1) {
                        listener.onGoalDroppedBetween(draggedGoal, insertIndex);
                    }

                    highlightedDividerPosition = -1;
                    notifyItemChanged(position);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    highlightedDividerPosition = -1;
                    notifyItemChanged(position);
                    return true;
            }
            return false;
        });
    }

     static class GoalViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textViewTitle;
        TextView textViewTargetAmount;
        TextView textViewCurrentAmount;
        TextView textViewProgressPercentage;
        TextView textViewDaysLeft;
        ProgressBar progressBar;
        ImageView imageViewIcon;
        ImageView imageViewCompleted;
        ImageView imageViewDetails;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view_goal);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewTargetAmount = itemView.findViewById(R.id.text_view_target_amount);
            textViewCurrentAmount = itemView.findViewById(R.id.text_view_current_amount);
            textViewProgressPercentage = itemView.findViewById(R.id.text_view_progress_percentage);
            textViewDaysLeft = itemView.findViewById(R.id.text_view_days_left);
            progressBar = itemView.findViewById(R.id.progress_bar);
            imageViewIcon = itemView.findViewById(R.id.image_view_icon);
            imageViewCompleted = itemView.findViewById(R.id.image_view_completed);
            imageViewDetails = itemView.findViewById(R.id.image_view_details);
        }

        public void bind(GoalEntity goal) {
            textViewTitle.setText(goal.getTitle());

            if (goal.getTargetAmount() != null) {
                textViewTargetAmount.setText(formatAmount(goal.getTargetAmount()));
                textViewTargetAmount.setVisibility(View.VISIBLE);
            } else {
                textViewTargetAmount.setVisibility(View.GONE);
            }

            textViewCurrentAmount.setText(formatAmount(goal.getCalculatedAmount()));

            Double progress = goal.getProgressPercentage();
            if (progress != null) {
                ViewUtils.updateGoalProgress(progressBar, textViewProgressPercentage, progress);
            } else {
                textViewProgressPercentage.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }

            Long daysLeft = goal.getDaysRemaining();
            if (daysLeft != null) {
                textViewDaysLeft.setText(formatDays(daysLeft));
                textViewDaysLeft.setVisibility(View.VISIBLE);
            } else {
                textViewDaysLeft.setVisibility(View.GONE);
            }

            if (goal.getColor() != null) {
                imageViewIcon.setColorFilter(goal.getColor());
            }

            if (goal.getIsCompleted() != null && goal.getIsCompleted()) {
                imageViewCompleted.setVisibility(View.VISIBLE);
                cardView.setAlpha(0.8f);
            } else {
                imageViewCompleted.setVisibility(View.GONE);
                cardView.setAlpha(1.0f);
            }

            imageViewIcon.setImageResource(R.drawable.ic_goal);
        }

        public void setHighlighted(boolean highlighted) {
            if (highlighted) {
                cardView.setCardBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.highlight_color, null)
                );
                cardView.setCardElevation(8f);
            } else {
                cardView.setCardBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.surface, null)
                );
                cardView.setCardElevation(2f);
            }
        }
    }

    static class DividerViewHolder extends RecyclerView.ViewHolder {
        View dividerView;

        public DividerViewHolder(@NonNull View itemView) {
            super(itemView);
            dividerView = itemView.findViewById(R.id.divider_view);
        }

        public void setHighlighted(boolean highlighted) {
            if (highlighted) {
                dividerView.setBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.highlight_color, null)
                );
            } else {
                dividerView.setBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.background, null)
                );
            }
        }
    }

    private static class DividerPlaceholder {
    }
}