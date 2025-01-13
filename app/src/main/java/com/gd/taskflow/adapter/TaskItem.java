package com.gd.taskflow.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gd.taskflow.R;
import com.gd.taskflow.activities.ArchiveTaskActivity;
import com.gd.taskflow.activities.MainActivity;
import com.gd.taskflow.database.TaskDatabaseHelper;
import com.gd.taskflow.database.TaskModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class TaskItem extends AbstractItem<TaskItem,TaskItem.ViewHolder> {
    private TaskModel task;
    private String searchQuery;
    private Context context;
    private FastAdapter<TaskItem> fastAdapter;
    private boolean selected = false;
    public TaskItem(TaskModel taskModel,FastAdapter<TaskItem> fastAdapter, Context context) {
        this.task = taskModel;
        this.searchQuery = "";
        this.context = context;
        this.fastAdapter = fastAdapter;}
    public TaskItem(TaskModel taskModel, String searchQuery,Context context) {
        this.task = taskModel;
        this.searchQuery = searchQuery;
        this.context = context;
    }
    @Override
    public int getType() {
        return R.id.taskItemCard;
    }
    @Override
    public int getLayoutRes() {
        return R.layout.task_item;
    }
    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View view) {
        return new ViewHolder(view);
    }
    @SuppressLint("RestrictedApi")
    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        if (!searchQuery.isEmpty()) {
            String title = task.getTitle();
            SpannableString spannableTitle = new SpannableString(title);
            int startPos = title.toLowerCase().indexOf(searchQuery.toLowerCase());
            if (startPos != -1) {
                int endPos = startPos + searchQuery.length();
                spannableTitle.setSpan(new BackgroundColorSpan(Color.YELLOW), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);}
            holder.taskItemTitleTV.setText(spannableTitle);} else {
            holder.taskItemTitleTV.setText(task.getTitle());}
        holder.taskItemDateTimeTV.setText(task.getDateTime());
        String photoPath = task.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty() ) {
                Glide.with(context)
                        .load(photoPath)
                        .error(R.drawable.task_icon)
                        .into(holder.taskItemImageIV);
        }
        holder.taskItemCB.setOnCheckedChangeListener(null);
        holder.taskItemCB.setChecked(task.isComplete()==1);
        holder.taskItemCompleteIV.setVisibility(task.isComplete()==1 ? View.VISIBLE : View.INVISIBLE);
        holder.taskItemCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TaskDatabaseHelper dbHelper = new TaskDatabaseHelper(context);
            if (isChecked) {
                holder.taskItemCompleteIV.setVisibility(View.VISIBLE);
                dbHelper.markTaskComplete(task.getId());
                task.setComplete(1);
            } else {
                holder.taskItemCompleteIV.setVisibility(View.INVISIBLE);
                dbHelper.markTaskIncomplete(task.getId());  // Mark task as incomplete in database
                task.setComplete(0);}});
        if (isSelected()) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
            holder.taskMenuIV.setVisibility(View.INVISIBLE);} else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.taskMenuIV.setVisibility(View.VISIBLE);}
        holder.taskMenuIV.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            if (context instanceof ArchiveTaskActivity) {
                popup.getMenuInflater().inflate(R.menu.task_options_menu_archive, popup.getMenu());
            } else if (context instanceof MainActivity) {
                popup.getMenuInflater().inflate(R.menu.task_options_menu_unarchive, popup.getMenu());
                MenuItem completeMenuItem = popup.getMenu().findItem(R.id.task_menu_complete);
                if (completeMenuItem != null) {
                    if (task.isComplete() == 1) {
                        completeMenuItem.setTitle("Uncomplete");} else {
                        completeMenuItem.setTitle("Complete");}}}
            popup.setOnMenuItemClickListener(menuItem -> {
                int position = holder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return false;
                TaskItem currentTaskItem = fastAdapter.getItem(position);
                if (menuItem.getItemId() == R.id.task_menu_unarchive) {
                    ((ArchiveTaskActivity) context).handleSwipeToUnarchive(currentTaskItem, position);
                    return true;} else if (menuItem.getItemId() == R.id.task_menu_complete) {
                    holder.taskItemCB.setChecked(!holder.taskItemCB.isChecked());
                    return true;} else if (menuItem.getItemId() == R.id.task_menu_archive) {
                    ((MainActivity) context).handleSwipeToArchive(currentTaskItem, position);
                    return true;} else if (menuItem.getItemId() == R.id.task_menu_delete) {
                    ((MainActivity) context).handleSwipeToDelete(currentTaskItem, position);
                    return true;}
                return false;});
            popup.show();});}
    @Override
    public void unbindView(@NonNull ViewHolder holder) {
        super.unbindView(holder);
        holder.taskItemTitleTV.setText(null);
        holder.taskItemDateTimeTV.setText(null);
        holder.taskItemCB.setOnCheckedChangeListener(null);
        holder.taskItemCB.setChecked(false);
        holder.itemView.setBackgroundColor(Color.TRANSPARENT);}
    public TaskModel getTask() {
        return task;
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskItemTitleTV, taskItemDateTimeTV;
        CheckBox taskItemCB;
        ImageView taskItemImageIV,taskItemCompleteIV,taskMenuIV;
        private FastAdapter<TaskItem> fastAdapter;
        public ViewHolder(View itemView) {
            super(itemView);
            taskItemTitleTV = itemView.findViewById(R.id.taskItemTitleTV);
            taskItemDateTimeTV = itemView.findViewById(R.id.taskItemDateTime);
            taskItemCB = itemView.findViewById(R.id.taskItemCB);
            taskItemImageIV = itemView.findViewById(R.id.taskItemIV);
            taskItemCompleteIV = itemView.findViewById(R.id.taskItemCompleteIV);
            taskMenuIV = itemView.findViewById(R.id.taskMenuIV);
            itemView.setOnClickListener(v -> {
                if (fastAdapter != null) {
                    fastAdapter.toggleSelection(getAdapterPosition());}});}}}
