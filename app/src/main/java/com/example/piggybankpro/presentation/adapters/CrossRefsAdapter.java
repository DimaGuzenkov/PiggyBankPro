package com.example.piggybankpro.presentation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.databinding.ItemCrossRefBinding;
import com.example.piggybankpro.presentation.adapters.holders.CrossRefsViewHolder;
import com.example.piggybankpro.presentation.utils.SwipeItemTouchHelperCallback;

import java.util.List;

public class CrossRefsAdapter extends RecyclerView.Adapter<CrossRefsViewHolder> implements
        SwipeItemTouchHelperCallback.OnSwipeListener,
        CrossRefsViewHolder.OnChangeListener {
    private List<GoalDepositCrossRefEntity> crossRefs;
    private final OnCrossRefsChangeListener listener;

    public interface OnCrossRefsChangeListener {
        boolean onChange();
        void onCrossRefsCountChange();
    }

    public CrossRefsAdapter(List<GoalDepositCrossRefEntity> crossRefs, OnCrossRefsChangeListener listener) {
        this.crossRefs = crossRefs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CrossRefsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var inflater = LayoutInflater.from(parent.getContext());
        var binding = ItemCrossRefBinding.inflate(inflater, parent, false);
        return new CrossRefsViewHolder(binding, this);
    }

    @Override
    public void onBindViewHolder(@NonNull CrossRefsViewHolder holder, int position) {
        var crossRef = crossRefs.get(position);
        holder.bind(crossRef);
    }

    @Override
    public int getItemCount() {
        return crossRefs.size();
    }

    public void updateCrossRefs(List<GoalDepositCrossRefEntity> newCrossRefs) {
        this.crossRefs = newCrossRefs;
        update();
    }

    public void addCrossRef(GoalDepositCrossRefEntity crossRef) {
        crossRefs.add(crossRef);
        update();
    }

    private void update() {
        listener.onChange();
        listener.onCrossRefsCountChange();
        notifyDataSetChanged();
    }

    public List<GoalDepositCrossRefEntity> getCrossRefs() {
        return crossRefs;
    }

    public double getTotalAmount() {
        var amount = 0.0;
        for (var crossRef: crossRefs) {
            if (crossRef.getAmount() != null) {
                amount += crossRef.getAmount();
            }
        }

        return amount;
    }


    @Override
    public void deleteItem(int position) {
        if (position != RecyclerView.NO_POSITION) {
            var goal = crossRefs.get(position);
            crossRefs.remove(goal);
            update();
        }
    }

    @Override
    public boolean changeAmount(double amount, int position) {
        crossRefs.get(position).setAmount(amount);
        return listener.onChange();
    }
}
