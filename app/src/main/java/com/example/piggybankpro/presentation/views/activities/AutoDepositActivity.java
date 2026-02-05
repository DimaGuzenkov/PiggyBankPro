package com.example.piggybankpro.presentation.views.activities;

import static com.example.piggybankpro.presentation.utils.AmountUtils.amountFromString;
import static com.example.piggybankpro.presentation.utils.ToastUtils.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.AutoDepositEntity;
import com.example.piggybankpro.data.local.entities.GoalDepositCrossRefEntity;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.databinding.ActivityAutoDepositBinding;
import com.example.piggybankpro.databinding.ActivityCreateAutoDepositBinding;
import com.example.piggybankpro.presentation.adapters.AutoDepositAdapter;
import com.example.piggybankpro.presentation.utils.AmountTextWatcher;
import com.example.piggybankpro.presentation.utils.AmountUtils;
import com.example.piggybankpro.presentation.utils.DateUtils;
import com.example.piggybankpro.presentation.utils.SwipeItemTouchHelperCallback;
import com.example.piggybankpro.presentation.utils.ToastUtils;
import com.example.piggybankpro.presentation.viewmodels.AutoDepositViewModel;
import com.example.piggybankpro.presentation.viewmodels.GoalViewModel;
import com.example.piggybankpro.presentation.views.dialogs.GoalSelectionDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoDepositActivity extends AppCompatActivity implements
        SwipeItemTouchHelperCallback.OnSwipeListener {
    private ActivityAutoDepositBinding binding;

    private AutoDepositViewModel autoDepositViewModel;
    private AutoDepositAdapter autoDepositAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAutoDepositBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Автопополнение");

        autoDepositViewModel = new ViewModelProvider(this).get(AutoDepositViewModel.class);

        setupRecyclerView();
        observeViewModel();
        setupButtons();

    }

    private void setupRecyclerView() {
        autoDepositAdapter = new AutoDepositAdapter(new ArrayList<>(), new AutoDepositAdapter.OnAutoDepositClickListener() {
            @Override
            public void onAutoDepositClick(AutoDepositEntity autoDeposit) {
                showEditAutoDepositDialog(autoDeposit);
            }

            @Override
            public void onAutoDepositLongClick(AutoDepositEntity autoDeposit, View view) {
                showContextMenu(autoDeposit, view);
            }

            @Override
            public void onToggleStatus(AutoDepositEntity autoDeposit, boolean isActive) {
                autoDepositViewModel.toggleAutoDepositStatus(autoDeposit.getId(), isActive);
            }
        });

        binding.recyclerViewAutoDeposits.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewAutoDeposits.setAdapter(autoDepositAdapter);

        binding.recyclerViewAutoDeposits.addItemDecoration(
                new androidx.recyclerview.widget.DividerItemDecoration(
                        this,
                        LinearLayoutManager.VERTICAL
                )
        );

        var itemTouchHelper = new ItemTouchHelper(new SwipeItemTouchHelperCallback(this));
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewAutoDeposits);
    }

    private void observeViewModel() {
        autoDepositViewModel.getAllAutoDeposits().observe(this, autoDeposits -> {
            if (autoDeposits != null && !autoDeposits.isEmpty()) {
                autoDepositAdapter.updateAutoDeposits(autoDeposits);
                binding.textViewEmptyState.setVisibility(View.GONE);
                binding.recyclerViewAutoDeposits.setVisibility(View.VISIBLE);
            } else {
                binding.textViewEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewAutoDeposits.setVisibility(View.GONE);
                binding.textViewEmptyState.setText("У вас нет настроенных автопополнений\nНажмите + чтобы добавить");
            }
        });

        autoDepositViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                display(this, errorMessage);
                autoDepositViewModel.clearError();
            }
        });
    }

    private void setupButtons() {
        binding.buttonAddAutoDeposit.setOnClickListener(v -> {
            showCreateAutoDepositDialog();
        });
    }

    private void showCreateAutoDepositDialog() {
        show(null);
    }

    private void showEditAutoDepositDialog(AutoDepositEntity autoDeposit) {
        show(autoDeposit.getId());
    }

    private void show(String id) {
        Intent intent = new Intent(this, CreateAutoDepositActivity.class);
        intent.putExtra("auto_deposit_id", id);
        startActivity(intent);
    }

    private void showContextMenu(AutoDepositEntity autoDeposit, View view) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_auto_deposit_context, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_execute_now) {
                autoDepositViewModel.executeAutoDeposit(autoDeposit.getId());
                autoDepositExecuted(this);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_auto_deposit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_execute_all) {
            autoDepositViewModel.executeDueAutoDeposits();
            autoDepositExecuted(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void deleteItem(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление автопополнения")
                .setMessage("Вы уверены, что хотите удалить это автопополнение?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    autoDepositViewModel.deleteAutoDeposit(autoDepositAdapter.getItemByPosition(position));
                    autoDepositDeleted(this);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}