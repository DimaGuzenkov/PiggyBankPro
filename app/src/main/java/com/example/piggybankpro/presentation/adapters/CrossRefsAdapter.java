package com.example.piggybankpro.presentation.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.utils.AmountUtils;

import java.util.List;

public class CrossRefsAdapter extends RecyclerView.Adapter<CrossRefsAdapter.CrossRefsViewHolder> {
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cross_ref, parent, false);
        return new CrossRefsViewHolder(view);
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

    public class CrossRefsViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final EditText editTextAmount;

        public CrossRefsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            ImageButton deleteButton = itemView.findViewById(R.id.button_delete);
            editTextAmount = itemView.findViewById(R.id.edit_text_amount);

            editTextAmount.addTextChangedListener(new AmountTextWatcher(editTextAmount));
            editTextAmount.addTextChangedListener(new TextWatcher() {
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
                    crossRefs.get(position).setAmount(amount);

                    if (!listener.onChange()) {
                        editTextAmount.setError("Недостаточно средств");
                        editTextAmount.requestFocus();
                    }
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    var goal = crossRefs.get(position);
                    crossRefs.remove(goal);
                    update();
                }
            });
        }

        public void bind(GoalDepositCrossRefEntity crossRef) {
            textViewTitle.setText(crossRef.getGoalTitle());
            if (crossRef.getAmount() != null) {
                editTextAmount.setText(AmountUtils.formatAmount(crossRef.getAmount()));
            }
        }
    }
}
