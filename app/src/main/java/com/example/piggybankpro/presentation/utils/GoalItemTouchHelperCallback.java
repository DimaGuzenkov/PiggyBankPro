package com.example.piggybankpro.presentation.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.presentation.adapters.GoalAdapter;

public class GoalItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final GoalAdapter adapter;
    private final Paint paint = new Paint();
    private Drawable deleteIcon;
    private int iconMargin;
    private boolean initiated = false;

    // Цвета для свайпа
    private final int swipeRightColor = Color.parseColor("#F44336"); // Красный
    private final int dragColor = Color.parseColor("#2196F3"); // Синий для подсветки

    public GoalItemTouchHelperCallback(GoalAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        // Drag направления: вверх и вниз
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

        // Swipe направления: только вправо для удаления
        int swipeFlags = ItemTouchHelper.RIGHT;

        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
//        // Меняем элементы местами в адаптере
//        int fromPosition = viewHolder.getAdapterPosition();
//        int toPosition = target.getAdapterPosition();
//
//        if (fromPosition != toPosition && fromPosition >= 0 && toPosition >= 0) {
//            adapter.moveItem(fromPosition, toPosition);
//            return true;
//        }

        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (adapter.listener != null) {
//            adapter.listener.onGoalSwiped(position);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Начало перетаскивания
//            adapter.setDragging(true);

            if (viewHolder != null) {
                // Визуальная обратная связь
                viewHolder.itemView.setAlpha(0.7f);
                viewHolder.itemView.setElevation(8f);
            }

        } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Начало свайпа
//            adapter.setDragging(false);

        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            // Завершение всех действий
//            adapter.setDragging(false);
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        // Восстанавливаем внешний вид элемента
        viewHolder.itemView.setAlpha(1.0f);
        viewHolder.itemView.setElevation(0f);
        viewHolder.itemView.setTranslationX(0);
        viewHolder.itemView.setTranslationY(0);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState,
                            boolean isCurrentlyActive) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX > 0) {
            // Swipe вправо - рисуем красный фон с иконкой корзины
            drawSwipeBackground(c, viewHolder, dX);

            // Смещаем элемент
            viewHolder.itemView.setTranslationX(dX);

        } else if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Drag - стандартное поведение
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                    actionState, isCurrentlyActive);
        } else {
            // Для других случаев
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                    actionState, isCurrentlyActive);
        }
    }

    private void drawSwipeBackground(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX) {
        View itemView = viewHolder.itemView;

        // Инициализируем ресурсы при первом вызове
        if (!initiated) {
            deleteIcon = ContextCompat.getDrawable(itemView.getContext(), android.R.drawable.ic_menu_delete);
            if (deleteIcon != null) {
                iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            }
            initiated = true;
        }

        // Ограничиваем максимальное смещение
        float maxSwipeDistance = itemView.getWidth() * 0.8f;
        if (dX > maxSwipeDistance) {
            dX = maxSwipeDistance;
        }

        // Рисуем красный фон с закругленными углами
        RectF background = new RectF(
                itemView.getLeft(),
                itemView.getTop() + 8,
                dX,
                itemView.getBottom() - 8
        );

        paint.setColor(swipeRightColor);
        canvas.drawRoundRect(background, 16, 16, paint);

        // Рисуем иконку корзины
        if (deleteIcon != null) {
            int iconSize = deleteIcon.getIntrinsicHeight();
            int iconLeft = itemView.getLeft() + 48;
            int iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
            int iconRight = iconLeft + iconSize;
            int iconBottom = iconTop + iconSize;

            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.draw(canvas);
        }

        // Рисуем текст "Удалить"
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(42);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.LEFT);

        String text = "Удалить";
        float textX = itemView.getLeft() + 112; // Отступ от иконки
        float textY = itemView.getTop() + (itemView.getHeight() / 2) + 15;

        canvas.drawText(text, textX, textY, textPaint);

        // Подсказка при полном свайпе
        if (dX > itemView.getWidth() * 0.6f) {
            Paint hintPaint = new Paint();
            hintPaint.setColor(Color.WHITE);
            hintPaint.setTextSize(32);
            hintPaint.setTextAlign(Paint.Align.CENTER);
            hintPaint.setAlpha(150);

            String hint = "Отпустите для удаления";
            float hintX = dX / 2;
            float hintY = itemView.getBottom() - 24;

            canvas.drawText(hint, hintX, hintY, hintPaint);
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.5f; // Нужно свайпнуть минимум на 50%
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * 2; // Быстрее для удобства
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true; // Включаем перетаскивание по долгому нажатию
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true; // Включаем свайп
    }

    @Override
    public long getAnimationDuration(@NonNull RecyclerView recyclerView,
                                     int animationType,
                                     float animateDx,
                                     float animateDy) {
        // Ускоряем анимации
        if (animationType == ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL) {
            return 200; // Быстрая отмена
        }
        return 300; // Нормальная скорость
    }
}