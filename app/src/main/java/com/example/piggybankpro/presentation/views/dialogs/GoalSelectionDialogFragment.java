package com.example.piggybankpro.presentation.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.presentation.adapters.GoalSelectionAdapter;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;

import java.util.ArrayList;
import java.util.List;

public class GoalSelectionDialogFragment extends DialogFragment {

    private GoalSelectionAdapter adapter;
    private GoalViewModel viewModel;
    private OnGoalSelectedListener listener;

    public interface OnGoalSelectedListener {
        void onGoalSelected(GoalEntity goal);
    }

    public static GoalSelectionDialogFragment newInstance() {
        return new GoalSelectionDialogFragment();
    }

    public void setOnGoalSelectedListener(OnGoalSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        View view = LayoutInflater.from(requireActivity())
                .inflate(R.layout.dialog_goal_selection, null);

        setupViews(view);
        setupViewModel();

        builder.setView(view)
                .setTitle("Выберите цель")
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    private void setupViews(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        EditText searchEditText = view.findViewById(R.id.search_edit_text);

        // Настраиваем RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GoalSelectionAdapter(goal -> {
            if (listener != null) {
                listener.onGoalSelected(goal);
            }
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                v.clearFocus();
            }
        });

        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                filterGoals(s.toString());
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(GoalViewModel.class);

        viewModel.getAllGoals().observe(this, goals -> {
            if (goals != null && !goals.isEmpty()) {
                adapter.setGoals(goals);
                updateEmptyView(false);
            } else {
                updateEmptyView(true);
            }
        });
    }

    private void filterGoals(String query) {
        viewModel.getAllGoals().observe(this, goals -> {
            if (goals == null) {
                updateEmptyView(true);
                return;
            }

            if (query == null || query.trim().isEmpty()) {
                adapter.setGoals(goals);
                updateEmptyView(goals.isEmpty());
                return;
            }

            List<GoalEntity> filtered = new ArrayList<>();
            String searchQuery = query.toLowerCase();

            for (GoalEntity goal : goals) {
                if (goal.getTitle().toLowerCase().contains(searchQuery)) {
                    filtered.add(goal);
                }
            }

            adapter.setGoals(filtered);
            updateEmptyView(filtered.isEmpty());
        });
    }

    private void updateEmptyView(boolean isEmpty) {
        View view = getView();
        if (view != null) {
            RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
            TextView emptyView = view.findViewById(R.id.empty_view);

            if (isEmpty) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Цели не найдены");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        }
    }
}
