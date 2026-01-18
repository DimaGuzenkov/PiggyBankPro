package com.example.piggybankpro.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.AutoDepositEntity;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.utils.AmountUtils;
import com.example.piggybankpro.presentation.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AutoDepositAdapter extends RecyclerView.Adapter<AutoDepositAdapter.AutoDepositViewHolder> {

    public interface OnAutoDepositClickListener {
        void onAutoDepositClick(AutoDepositEntity autoDeposit);
        void onAutoDepositLongClick(AutoDepositEntity autoDeposit, View view);
        void onToggleStatus(AutoDepositEntity autoDeposit, boolean isActive);
    }

    private List<AutoDepositEntity> autoDeposits;
    private final OnAutoDepositClickListener listener;

    public AutoDepositAdapter(List<AutoDepositEntity> autoDeposits, OnAutoDepositClickListener listener) {
        this.autoDeposits = autoDeposits;
        this.listener = listener;
    }

    public void updateAutoDeposits(List<AutoDepositEntity> newAutoDeposits) {
        this.autoDeposits = newAutoDeposits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AutoDepositViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_auto_deposit, parent, false);
        return new AutoDepositViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AutoDepositViewHolder holder, int position) {
        AutoDepositEntity autoDeposit = autoDeposits.get(position);
        holder.bind(autoDeposit);

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAutoDepositClick(autoDeposit);
            }
        });

        holder.cardView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onAutoDepositLongClick(autoDeposit, v);
                return true;
            }
            return false;
        });

        holder.switchActive.setOnCheckedChangeListener(null); // Отключаем предыдущий слушатель
        holder.switchActive.setChecked(autoDeposit.getIsActive());
        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleStatus(autoDeposit, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return autoDeposits != null ? autoDeposits.size() : 0;
    }

    class AutoDepositViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textViewName;
        TextView textViewAmount;
        TextView textViewPeriod;
        TextView textViewNextExecution;
        SwitchCompat switchActive;
        ImageView imageViewIcon;

        public AutoDepositViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view_auto_deposit);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewAmount = itemView.findViewById(R.id.text_view_amount);
            textViewPeriod = itemView.findViewById(R.id.text_view_period);
            textViewNextExecution = itemView.findViewById(R.id.text_view_next_execution);
            switchActive = itemView.findViewById(R.id.switch_active);
            imageViewIcon = itemView.findViewById(R.id.image_view_icon);
        }

        public void bind(AutoDepositEntity autoDeposit) {
            textViewName.setText(autoDeposit.getName());

            textViewAmount.setText(AmountUtils.formatAmount(autoDeposit.getAmount()));

            textViewPeriod.setText(DateUtils.getPeriodString(autoDeposit.getPeriodType()));

            if (autoDeposit.getNextExecutionDate() != null) {
                textViewNextExecution.setText(DateUtils.formatDate(autoDeposit.getNextExecutionDate()));
            } else {
                textViewNextExecution.setText("Не запланировано");
            }

            switchActive.setChecked(autoDeposit.getIsActive());

            imageViewIcon.setImageResource(R.drawable.ic_money);

            if (autoDeposit.getIsActive()) {
                cardView.setCardBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.active, null)
                );
            } else {
                cardView.setCardBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.inactive, null)
                );
            }
        }
    }
}