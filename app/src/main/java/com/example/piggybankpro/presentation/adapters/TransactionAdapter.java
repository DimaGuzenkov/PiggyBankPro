package com.example.piggybankpro.presentation.adapters;

import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;
import static com.example.piggybankpro.presentation.utils.DateUtils.formatDate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.TransactionEntity;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<TransactionEntity> transactions;

    public TransactionAdapter(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public void updateTransactions(List<TransactionEntity> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    private static final int DepositColor = 0xFF4CAF50;
    private static final int WithdrawColor = 0xFFF44336;

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionEntity transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public TransactionEntity getItemByPosition(int position) {
        return transactions.get(position);
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewAmount;
        private final TextView textViewDescription;
        private final TextView textViewDate;
        private final TextView textViewType;
        private final View typeIndicator;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAmount = itemView.findViewById(R.id.text_view_amount);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewType = itemView.findViewById(R.id.text_view_type);
            typeIndicator = itemView.findViewById(R.id.view_type_indicator);
            ImageButton deleteButton = itemView.findViewById(R.id.button_delete);

            deleteButton.setOnClickListener(v -> {

            });
        }

        public void bind(TransactionEntity transaction) {
            textViewAmount.setText(formatAmount(transaction.getAmount()));

            textViewDescription.setText(transaction.getDescription());

            textViewDate.setText(formatDate(transaction.getTransactionDate()));

            textViewType.setText(transaction.getTransactionTypeString());
            typeIndicator.setBackgroundColor(transaction.getAmount() < 0 ? WithdrawColor : DepositColor);
        }
    }
}