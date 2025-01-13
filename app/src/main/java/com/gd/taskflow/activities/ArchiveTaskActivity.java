package com.gd.taskflow.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gd.taskflow.R;
import com.gd.taskflow.adapter.TaskItem;
import com.gd.taskflow.database.TaskDatabaseHelper;
import com.gd.taskflow.database.TaskModel;
import com.gd.taskflow.databinding.ActivityArchiveTaskBinding;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.List;

public class ArchiveTaskActivity extends AppCompatActivity {

    ActivityArchiveTaskBinding archivedBinding;
    private ItemAdapter<TaskItem> itemAdapter;
    private FastAdapter<TaskItem> fastAdapter;
    TaskDatabaseHelper taskDatabaseHelper;
    List<TaskModel> archivedTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        archivedBinding = ActivityArchiveTaskBinding.inflate(getLayoutInflater());
        setContentView(archivedBinding.getRoot());

        if (getSupportActionBar() !=null){
            getSupportActionBar().setTitle("Archived Tasks");
        }

        taskDatabaseHelper = new TaskDatabaseHelper(this);
        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);

        archivedBinding.archivedTasksRV.setLayoutManager(new LinearLayoutManager(this));
        archivedBinding.archivedTasksRV.setAdapter(fastAdapter);

        loadArchivedTasks();

        enableSwipeToUnarchive();

    }

    private void loadArchivedTasks() {
        itemAdapter.clear();
        archivedTasks = taskDatabaseHelper.getAllArchiveTasks(); // Get archived tasks

        if (archivedTasks.isEmpty()) {
            // Show Lottie animation when there are no tasks
            archivedBinding.emptyListAnim.setVisibility(View.VISIBLE);
            archivedBinding.archivedTasksRV.setVisibility(View.GONE);
        } else {
            // Hide Lottie animation and show the RecyclerView when there are tasks
            archivedBinding.emptyListAnim.setVisibility(View.GONE);
            archivedBinding.archivedTasksRV.setVisibility(View.VISIBLE);

            for (TaskModel task : archivedTasks) {
                itemAdapter.add(new TaskItem(task,fastAdapter,ArchiveTaskActivity.this));
            }
        }
    }

    public void enableSwipeToUnarchive() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // We don't need drag & drop functionality, so return false
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Get the position of the swiped item
                int position = viewHolder.getAdapterPosition();
                TaskItem taskItem = itemAdapter.getAdapterItem(position);
                // Unarchive the task in the database
                handleSwipeToUnarchive(taskItem,position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Draw blue background and unarchive icon on swipe
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    final int backgroundColor = Color.BLUE;
                    final Paint paint = new Paint();
                    paint.setColor(backgroundColor);
                    RectF background = new RectF(
                            viewHolder.itemView.getRight() + dX,
                            viewHolder.itemView.getTop(),
                            viewHolder.itemView.getRight(),
                            viewHolder.itemView.getBottom()
                    );
                    c.drawRect(background, paint);

                    // Draw unarchive icon
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_unarchive);
                    final int iconMargin = (viewHolder.itemView.getHeight() - icon.getHeight()) / 2;
                    final int iconTop = viewHolder.itemView.getTop() + iconMargin;
                    final int iconBottom = iconTop + icon.getHeight();
                    final int iconLeft = viewHolder.itemView.getRight() - icon.getWidth() - iconMargin;
                    final int iconRight = viewHolder.itemView.getRight() - iconMargin;

                    c.drawBitmap(icon, new Rect(0, 0, icon.getWidth(), icon.getHeight()), new Rect(iconLeft, iconTop, iconRight, iconBottom), paint);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(archivedBinding.archivedTasksRV);
    }

    public void handleSwipeToUnarchive(TaskItem taskItem, int position) {
        taskDatabaseHelper.unarchiveTask(taskItem.getTask().getId());
        // Remove the task from the adapter and refresh the list
        itemAdapter.remove(position);
        loadArchivedTasks();

    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}