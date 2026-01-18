//package com.example.piggybankpro.presentation.utils;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.piggybankpro.presentation.adapters.GoalAdapter;
//
//public class DragItemTouchHelper extends ItemTouchHelper.Callback {
//
//    private final GoalAdapter adapter;
//    private boolean isDragging = false;
//
//    public DragItemTouchHelper(GoalAdapter adapter) {
//        this.adapter = adapter;
//    }
//
//    @Override
//    public int getMovementFlags(@NonNull RecyclerView recyclerView,
//                                @NonNull RecyclerView.ViewHolder viewHolder) {
//        // Разрешаем перетаскивание во всех направлениях
//        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
//                ItemTouchHelper.START | ItemTouchHelper.END;
//        return makeMovementFlags(dragFlags, 0);
//    }
//
//    @Override
//    public boolean onMove(@NonNull RecyclerView recyclerView,
//                          @NonNull RecyclerView.ViewHolder viewHolder,
//                          @NonNull RecyclerView.ViewHolder target) {
//        int fromPosition = viewHolder.getAbsoluteAdapterPosition();
//        int toPosition = target.getAbsoluteAdapterPosition();
//
//        if (fromPosition != toPosition) {
//            adapter.moveItem(fromPosition, toPosition);
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//        // Не используем свайп
//    }
//
//    @Override
//    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//        super.onSelectedChanged(viewHolder, actionState);
//
//        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
//            isDragging = true;
//            if (viewHolder instanceof GoalAdapter.GoalViewHolder holder) {
//                holder.itemView.setAlpha(0.5f);
//
//                // Сохраняем перетаскиваемую цель
//                int position = holder.getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION) {
//                    adapter.setDraggedGoal(adapter.getGoalAt(position), position);
//                }
//            }
//        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
//            isDragging = false;
//            adapter.clearDrag();
//        }
//    }
//
//    @Override
//    public void clearView(@NonNull RecyclerView recyclerView,
//                          @NonNull RecyclerView.ViewHolder viewHolder) {
//        super.clearView(recyclerView, viewHolder);
//        viewHolder.itemView.setAlpha(1.0f);
//
//        // После завершения перетаскивания обновляем все элементы
//        adapter.notifyDataSetChanged();
//    }
//
//    @Override
//    public boolean isLongPressDragEnabled() {
//        // Отключаем стандартное long press, будем использовать свой handle
//        return false;
//    }
//
//    @Override
//    public boolean isItemViewSwipeEnabled() {
//        return false;
//    }
//
//    public boolean isDragging() {
//        return isDragging;
//    }
//}