package com.example.piggybankpro.presentation.adapters;

import android.content.ClipDescription;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.databinding.ItemDividerBinding;
import com.example.piggybankpro.databinding.ItemGoalBinding;
import com.example.piggybankpro.presentation.adapters.holders.DividerViewHolder;
import com.example.piggybankpro.presentation.adapters.holders.GoalViewHolder;

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
            var binding = ItemGoalBinding.inflate(inflater, parent, false);
            return new GoalViewHolder(binding);
        } else {
            var binding = ItemDividerBinding.inflate(inflater, parent, false);
            return new DividerViewHolder(binding);
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
        var binding = holder.getBinding();
        binding.cardViewGoal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoalClick(goal);
            }
        });

        binding.cardViewGoal.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onGoalLongClick(goal, v);
                return true;
            }
            return false;
        });

        binding.viewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoalDetailsClick(goal);
            }
        });

        binding.cardViewGoal.setOnDragListener((v, event) -> {
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
                        if (draggedGoal.getOrderPosition() < insertIndex) {
                            listener.onGoalDroppedBetween(draggedGoal, insertIndex - 1);
                        } else {
                            listener.onGoalDroppedBetween(draggedGoal, insertIndex);
                        }
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

    private static class DividerPlaceholder {
    }
}