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
import com.example.piggybankpro.databinding.ItemAutoDepositBinding;
import com.example.piggybankpro.databinding.ItemGoalBinding;
import com.example.piggybankpro.presentation.adapters.holders.AutoDepositViewHolder;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.utils.AmountUtils;
import com.example.piggybankpro.presentation.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AutoDepositAdapter extends RecyclerView.Adapter<AutoDepositViewHolder> {

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
        var inflater = LayoutInflater.from(parent.getContext());
        var binding = ItemAutoDepositBinding.inflate(inflater, parent, false);
        return new AutoDepositViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AutoDepositViewHolder holder, int position) {
        AutoDepositEntity autoDeposit = autoDeposits.get(position);
        holder.bind(autoDeposit);
        var binding = holder.getBinding();

        binding.cardViewAutoDeposit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAutoDepositClick(autoDeposit);
            }
        });

        binding.cardViewAutoDeposit.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onAutoDepositLongClick(autoDeposit, v);
                return true;
            }
            return false;
        });

        binding.switchActive.setOnCheckedChangeListener(null); // Отключаем предыдущий слушатель
        binding.switchActive.setChecked(autoDeposit.getIsActive());
        binding.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleStatus(autoDeposit, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return autoDeposits != null ? autoDeposits.size() : 0;
    }

    public AutoDepositEntity getItemByPosition(int position) {
        return autoDeposits.get(position);
    }
}