package com.example.piggybankpro.presentation.adapters.holders;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.databinding.ItemDividerBinding;

public class DividerViewHolder extends RecyclerView.ViewHolder {
    private final ItemDividerBinding binding;

    public DividerViewHolder(@NonNull ItemDividerBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void setHighlighted(boolean highlighted) {
        if (highlighted) {
            binding.dividerView.setBackgroundColor(
                    itemView.getContext().getResources().getColor(R.color.highlight_color, null)
            );
        } else {
            binding.dividerView.setBackgroundColor(
                    itemView.getContext().getResources().getColor(R.color.background, null)
            );
        }
    }
}
