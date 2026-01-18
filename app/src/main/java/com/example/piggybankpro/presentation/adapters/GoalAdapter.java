package com.example.piggybankpro.presentation.adapters;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.data.local.entities.GoalEntity;
import com.example.piggybankpro.presentation.utils.ViewUtils;

import static com.example.piggybankpro.presentation.utils.AmountUtils.formatAmount;
import static com.example.piggybankpro.presentation.utils.DateUtils.formatDays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GoalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(goals, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(goals, i, i - 1);
            }
        }

        // Обновляем порядковые позиции
//        for (int i = 0; i < goals.size(); i++) {
//            goals.get(i).setOrderPosition(i);
//        }

        notifyItemMoved(fromPosition, toPosition);

        if (listener != null) {
//            listener.onGoalMoved(fromPosition, toPosition);
        }
    }

    public interface OnGoalClickListener {
        void onGoalClick(GoalEntity goal);
        void onGoalLongClick(GoalEntity goal, View view);
        void onGoalDetailsClick(GoalEntity goal);
        void onGoalDroppedOnGoal(GoalEntity draggedGoal, GoalEntity targetGoal);
        void onGoalDroppedBetween(GoalEntity draggedGoal, int insertIndex);
    }

    private static final int TYPE_GOAL = 0;
    private static final int TYPE_DIVIDER = 1;

//    private final List<Object> items = new ArrayList<>(); // Смешанный список: цели + разделители
    private List<GoalEntity> goals = new ArrayList<>();
    private final List<DividerPlaceholder> dividers = new ArrayList<>();
    public final OnGoalClickListener listener;
    private GoalEntity draggedGoal;
    private int highlightedDividerPosition = -1;

    public GoalAdapter(List<GoalEntity> goals, OnGoalClickListener listener) {
        this.listener = listener;
        updateGoals(goals);
    }

    private void createDividers(int count) {
        dividers.clear();
        for (int i = 0; i < count; i++) {
            dividers.add(new DividerPlaceholder());
        }
    }

    public void updateGoals(List<GoalEntity> goals) {
//        items.clear();
//
//        items.add(new DividerPlaceholder());
//        // Добавляем цели и разделители
//        for (int i = 0; i < goals.size(); i++) {
//            // Добавляем цель
//            items.add(goals.get(i));
//
//            // Добавляем разделитель после каждой цели, кроме последней
//            if (i < goals.size() - 1) {
//                items.add(new DividerPlaceholder()); // Объект-маркер для разделителя
//            }
//        }
        this.goals = goals;
        Log.d("Drag", "Goals count " + goals.size());
        createDividers(goals.size() + 1);

        notifyDataSetChanged();
    }

    public int getGoalCount() {
//        return items.size() / 2;
        return goals.size();
    }

    @Override
    public int getItemViewType(int position) {
//        Object item = items.get(position);
//        return item instanceof GoalEntity ? TYPE_GOAL : TYPE_DIVIDER;
        return position % 2 == 1 ? TYPE_GOAL : TYPE_DIVIDER;
    }

    @Override
    public int getItemCount() {
//        return items.size();
        return goals.size() + dividers.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_GOAL) {
            View view = inflater.inflate(R.layout.item_goal, parent, false);
            return new GoalViewHolder(view);
        } else {
            // Создаем View для разделителя
            View view = inflater.inflate(R.layout.item_divider, parent, false);
            return new DividerViewHolder(view);
        }
    }

    public List<GoalEntity> getGoals() {
        return goals;
    }

    private GoalEntity getGoalByPosition(int position) {
        return goals.get((position - 1) / 2);
    }

    private DividerPlaceholder getDividerByPosition(int position) {
        return dividers.get(position / 2);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GoalViewHolder goalHolder) {
//            Log.d("Drag", "in viewHolder");
            GoalEntity goal = getGoalByPosition(position);
//            Log.d("Drag", "Bind " + goal.getTitle() + " at position " + position);
            goalHolder.bind(goal);

            // Устанавливаем обработчики для цели
            setupGoalView(goalHolder, position);

        } else if (holder instanceof DividerViewHolder dividerHolder) {
            // Устанавливаем обработчики для разделителя
            setupDividerView(dividerHolder, position);
        }
    }

    private void setupGoalView(GoalViewHolder holder, int position) {
//        GoalEntity goal = (GoalEntity) items.get(position);
        var goal = getGoalByPosition(position);
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoalClick(goal);
            }
        });

        holder.cardView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onGoalLongClick(goal, v);
                return true;
            }
            return false;
        });

        holder.imageViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoalDetailsClick(goal);
            }
        });

//        public boolean onDrag(View v, DragEvent event) {
//            switch (event.getAction()) {
//                case DragEvent.ACTION_DRAG_STARTED:
//                    // Проверяем, что перетаскивается цель
//                    ClipDescription clipDescription = event.getClipDescription();
//                    if (clipDescription != null &&
//                            clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
//                        return true;
//                    }
//                    return false;
//
//                case DragEvent.ACTION_DRAG_ENTERED:
//                    // Когда перетаскиваемый объект ВОШЕЛ в границы этой цели
//                    Log.d("Drag", "ENTERED: " + goal.getTitle());
//
//                    // Получаем перетаскиваемую цель из localState
//                    Object localState = event.getLocalState();
//                    if (localState instanceof GoalEntity) {
//                        GoalEntity draggedGoal = (GoalEntity) localState;
//
//                        // Не подсвечиваем, если это та же самая цель
//                        if (!draggedGoal.getId().equals(goal.getId())) {
//                            setHighlightedGoal(goal.getId());
//                        }
//                    }
//                    return true;
//
//                case DragEvent.ACTION_DRAG_EXITED:
//                    // Когда перетаскиваемый объект ВЫШЕЛ из границ этой цели
//                    Log.d("Drag", "EXITED: " + goal.getTitle());
//
//                    // Убираем подсветку
//                    if (goal.getId().equals(highlightedGoalId)) {
//                        clearHighlight();
//                    }
//                    return true;
//
//                case DragEvent.ACTION_DROP:
//                    // Когда перетаскиваемый объект ОТПУЩЕН над этой целью
//                    Log.d("Drag", "DROP on: " + goal.getTitle());
//
//                    Object droppedLocalState = event.getLocalState();
//                    if (droppedLocalState instanceof GoalEntity) {
//                        GoalEntity draggedGoal = (GoalEntity) droppedLocalState;
//
//                        // Если перетащили на другую цель
//                        if (!draggedGoal.getId().equals(goal.getId()) && listener != null) {
//                            listener.onGoalDropped(draggedGoal, goal);
//                        }
//                    }
//
//                    clearHighlight();
//                    return true;
//
//                case DragEvent.ACTION_DRAG_ENDED:
//                    // Всегда сбрасываем подсветку при завершении перетаскивания
//                    Log.d("Drag", "DRAG ENDED");
//                    clearHighlight();
//                    return true;
//            }
//            return false;
//        }

        // Подсветка цели при наведении
        holder.cardView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        ClipDescription clipDescription = event.getClipDescription();
                        return clipDescription != null &&
                                clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                    case DragEvent.ACTION_DRAG_ENTERED:
                        // Подсвечиваем цель при наведении
//                        Log.d("Drag", "On goal " + goal.getTitle());
                        if (draggedGoal != null && !draggedGoal.getId().equals(goal.getId())) {
                            holder.setHighlighted(true);
                        }
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED, DragEvent.ACTION_DRAG_ENDED:
                        holder.setHighlighted(false);
                        return true;

                    case DragEvent.ACTION_DROP:
                        if (draggedGoal != null && !draggedGoal.getId().equals(goal.getId())) {
                            // Отпустили на цели
                            listener.onGoalDroppedOnGoal(draggedGoal, goal);
                        }
                        holder.setHighlighted(false);
                        return true;
                }
                return false;
            }
        });
    }

    public void setDraggedGoal(GoalEntity draggedGoal) {
        this.draggedGoal = draggedGoal;
    }

    private void setupDividerView(DividerViewHolder holder, int position) {
        // Подсвечиваем разделитель
        holder.setHighlighted(position == highlightedDividerPosition);

        // Обработчик перетаскивания для разделителя
        holder.itemView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        ClipDescription clipDescription = event.getClipDescription();
                        return clipDescription != null &&
                                clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                    case DragEvent.ACTION_DRAG_ENTERED:
                        // Подсвечиваем разделитель
                        Log.d("Drag", "On divider");
                        highlightedDividerPosition = position;
                        notifyItemChanged(position);
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        // Убираем подсветку
                        if (highlightedDividerPosition == position) {
                            highlightedDividerPosition = -1;
                            notifyItemChanged(position);
                        }
                        return true;

                    case DragEvent.ACTION_DROP:
                        // Вычисляем индекс для вставки
//                        int insertIndex = getInsertIndexFromDividerPosition(position);
                        int insertIndex = position / 2;
//                        if (insertIndex > 0) {
//                            --insertIndex;
//                        }
                        if (draggedGoal != null && insertIndex != -1) {
                            listener.onGoalDroppedBetween(draggedGoal, insertIndex);
                        }

                        // Сбрасываем подсветку
                        highlightedDividerPosition = -1;
                        notifyItemChanged(position);
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        highlightedDividerPosition = -1;
                        notifyItemChanged(position);
                        return true;
                }
                return false;
            }
        });
    }

    // Вычисляем индекс вставки на основе позиции разделителя
//    private int getInsertIndexFromDividerPosition(int dividerPosition) {
//        // Проходим по списку и считаем цели до этого разделителя
//        int goalCount = 0;
//        for (int i = 0; i <= dividerPosition; i++) {
//            if (items.get(i) instanceof GoalEntity) {
//                goalCount++;
//            }
//        }
//        return goalCount; // Индекс для вставки
//    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textViewTitle;
        TextView textViewTargetAmount;
        TextView textViewCurrentAmount;
        TextView textViewProgressPercentage;
        TextView textViewDaysLeft;
        ProgressBar progressBar;
        ImageView imageViewIcon;
        ImageView imageViewCompleted;
        ImageView imageViewDetails;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view_goal);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewTargetAmount = itemView.findViewById(R.id.text_view_target_amount);
            textViewCurrentAmount = itemView.findViewById(R.id.text_view_current_amount);
            textViewProgressPercentage = itemView.findViewById(R.id.text_view_progress_percentage);
            textViewDaysLeft = itemView.findViewById(R.id.text_view_days_left);
            progressBar = itemView.findViewById(R.id.progress_bar);
            imageViewIcon = itemView.findViewById(R.id.image_view_icon);
            imageViewCompleted = itemView.findViewById(R.id.image_view_completed);
            imageViewDetails = itemView.findViewById(R.id.image_view_details);
        }

        public void bind(GoalEntity goal) {
            textViewTitle.setText(goal.getTitle());

            if (goal.getTargetAmount() != null) {
                textViewTargetAmount.setText(formatAmount(goal.getTargetAmount()));
                textViewTargetAmount.setVisibility(View.VISIBLE);
            } else {
                textViewTargetAmount.setVisibility(View.GONE);
            }

            textViewCurrentAmount.setText(formatAmount(goal.getCalculatedAmount()));

            Double progress = goal.getProgressPercentage();
            if (progress != null) {
                ViewUtils.updateGoalProgress(progressBar, textViewProgressPercentage, progress);
            } else {
                textViewProgressPercentage.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }

            Long daysLeft = goal.getDaysRemaining();
            if (daysLeft != null) {
                textViewDaysLeft.setText(formatDays(daysLeft));
                textViewDaysLeft.setVisibility(View.VISIBLE);
            } else {
                textViewDaysLeft.setVisibility(View.GONE);
            }

            if (goal.getColor() != null) {
                imageViewIcon.setColorFilter(goal.getColor());
            }

            if (goal.getIsCompleted() != null && goal.getIsCompleted()) {
                imageViewCompleted.setVisibility(View.VISIBLE);
                cardView.setAlpha(0.8f);
            } else {
                imageViewCompleted.setVisibility(View.GONE);
                cardView.setAlpha(1.0f);
            }

            imageViewIcon.setImageResource(R.drawable.ic_goal);
        }

        public void setHighlighted(boolean highlighted) {
            if (highlighted) {
                cardView.setCardBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.highlight_color, null)
                );
                cardView.setCardElevation(8f);
            } else {
                cardView.setCardBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.surface, null)
                );
                cardView.setCardElevation(2f);
            }
        }
    }

    static class DividerViewHolder extends RecyclerView.ViewHolder {
        View dividerView;
        View dividerLine;

        public DividerViewHolder(@NonNull View itemView) {
            super(itemView);
            dividerView = itemView.findViewById(R.id.divider_view);
            dividerLine = itemView.findViewById(R.id.divider_line);
        }

        public void setHighlighted(boolean highlighted) {
            if (highlighted) {
                dividerLine.setBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.highlight_color, null)
                );
                dividerLine.setElevation(8f);
                dividerLine.animate()
                        .scaleX(10f)
                        .scaleY(10f)
                        .setDuration(200)
                        .start();
            } else {
                dividerLine.setBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.divider_color, null)
                );
                dividerLine.setElevation(2f);

                dividerLine.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start();
            }
        }
    }

    // Класс-маркер для разделителя
    private static class DividerPlaceholder {
        // Просто маркер для разделителя
    }
}