package com.example.piggybankpro.presentation.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeItemTouchHelperCallback extends ItemTouchHelper.Callback {

    public interface OnSwipeListener {
        void deleteItem(int position);
    }

    private final OnSwipeListener listener;
    private final Paint paint = new Paint();
    private Drawable deleteIcon;
    private boolean initiated = false;
    private boolean isVibrated = false;
    private RecyclerView.ViewHolder viewHolder = null;

    private int iconRight;
    private final int swipeRightColor = Color.parseColor("#F44336");

    public SwipeItemTouchHelperCallback(OnSwipeListener listener) {
        this.listener = listener;
        paint.setAntiAlias(true);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        int TYPE_TO_SWIPE = 0;
        if (viewHolder.getItemViewType() == TYPE_TO_SWIPE) {
            return makeMovementFlags(0, ItemTouchHelper.RIGHT);
        }
        return makeMovementFlags(0, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder == this.viewHolder) {
            listener.deleteItem(viewHolder.getAbsoluteAdapterPosition());
            this.viewHolder = null;
        }

        super.clearView(recyclerView, viewHolder);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState,
                            boolean isCurrentlyActive) {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE || dX <= 0) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                    actionState, isCurrentlyActive);
            return;
        }

        View itemView = viewHolder.itemView;

        if (!initiated) {
            init(itemView);
        }

        calculateIconBounds(itemView);

        float maxSwipe = iconRight;
        if (dX > maxSwipe) {
            dX = maxSwipe;
        }

        drawSwipeBackground(c, itemView, dX);
        itemView.setTranslationX(dX);

        if (!isCurrentlyActive) {
            return;
        }

        if (dX >= maxSwipe) {
            this.viewHolder = viewHolder;
            if (!isVibrated) {
                vibrate(itemView);
                isVibrated = true;
            }
        } else {
            this.viewHolder = null;
            isVibrated = false;
        }
    }

    private void init(View itemView) {
        deleteIcon = ContextCompat.getDrawable(itemView.getContext(), android.R.drawable.ic_menu_delete);
        initiated = true;
    }

    private void calculateIconBounds(View itemView) {
        int iconSize = (int) (itemView.getHeight() * 0.8);
        iconRight = itemView.getLeft() + iconSize;

        int iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
        int iconLeft = itemView.getLeft();
        int iconBottom = iconTop + iconSize;

        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
    }

    private void drawSwipeBackground(Canvas canvas, View itemView, float dX) {
        RectF background = new RectF(
                itemView.getLeft(),
                itemView.getTop() + 16,
                dX + 24,
                itemView.getBottom() - 16
        );

        paint.setColor(swipeRightColor);
        canvas.drawRoundRect(background, 16, 16, paint);

        deleteIcon.draw(canvas);
    }

    private void vibrate(View itemView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Vibrator vibrator = (Vibrator) itemView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 1.0f;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return Float.MAX_VALUE;
    }

    @Override
    public long getAnimationDuration(@NonNull RecyclerView recyclerView,
                                     int animationType,
                                     float animateDx,
                                     float animateDy) {
        return 300;
    }
}