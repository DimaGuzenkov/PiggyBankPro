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
import com.example.piggybankpro.databinding.ItemCrossRefBinding;
import com.example.piggybankpro.databinding.ItemTransactionBinding;
import com.example.piggybankpro.presentation.adapters.holders.TransactionViewHolder;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionViewHolder> {

    private List<TransactionEntity> transactions;

    public TransactionAdapter(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public void updateTransactions(List<TransactionEntity> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var inflater = LayoutInflater.from(parent.getContext());
        var binding = ItemTransactionBinding.inflate(inflater, parent, false);
        return new TransactionViewHolder(binding);
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
}