package com.example.piggybankpro.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;

import java.util.List;

public class GoalSelectionAdapter extends RecyclerView.Adapter<GoalSelectionAdapter.GoalViewHolder> {

    private List<GoalEntity> goals;
    private OnGoalClickListener listener;

    public interface OnGoalClickListener {
        void onGoalClick(GoalEntity goal);
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView textName;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_goal_name);
        }
    }

    public GoalSelectionAdapter(OnGoalClickListener listener) {
        this.listener = listener;
    }

    public void setGoals(List<GoalEntity> goals) {
        this.goals = goals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_selection, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalEntity goal = goals.get(position);

        holder.textName.setText(goal.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoalClick(goal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return goals != null ? goals.size() : 0;
    }
}
