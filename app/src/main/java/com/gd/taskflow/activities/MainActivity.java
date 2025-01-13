package com.gd.taskflow.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gd.taskflow.R;
import com.gd.taskflow.adapter.TaskItem;
import com.gd.taskflow.database.TaskDatabaseHelper;
import com.gd.taskflow.database.TaskModel;
import com.gd.taskflow.databinding.ActivityMainBinding;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    ActivityMainBinding mainBinding;
    private ItemAdapter<TaskItem> itemAdapter;
    private FastAdapter<TaskItem> fastAdapter;
    TaskDatabaseHelper taskDatabaseHelper;
    List<TaskModel> taskList = new ArrayList<>();
    private Set<Integer> selectedPositions = new HashSet<>();
    private ActionMode actionMode;
    private Drawable deleteIcon;
    private Drawable archiveIcon;
    private ColorDrawable deleteBackground;
    private ColorDrawable archiveBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        taskDatabaseHelper = new TaskDatabaseHelper(this);
        requestPermissionsIfNeeded();
        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        fastAdapter.withSelectable(true);
        fastAdapter.withMultiSelect(true);
        mainBinding.TaskRV.setLayoutManager(new LinearLayoutManager(this));
        mainBinding.TaskRV.setAdapter(fastAdapter);
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.icon_delete);
        archiveIcon = ContextCompat.getDrawable(this, R.drawable.icon_archive);
        deleteBackground = new ColorDrawable(Color.RED);
        archiveBackground = new ColorDrawable(Color.GREEN);
        loadTasks();
        setupSwipeGestures();
        fastAdapter.withOnLongClickListener((view, adapter, item, position) -> {
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback);}
            toggleSelection(position);  // Start selecting on long click
            return true;});
        fastAdapter.withOnClickListener((view, adapter, item, position) -> {
            if (actionMode != null) {
                toggleSelection(position);} else {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                intent.putExtra("TASK_ID", item.getTask().getId());
                startActivity(intent);}
            return true;});
        mainBinding.addBT.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
               startActivity(intent);}});
    }
    private void loadTasks() {
        itemAdapter.clear();
        taskList = taskDatabaseHelper.getAllUnarchiveTasks();
        if (currentSortOption == SortOption.TITLE) {
            Collections.sort(taskList, (task1, task2) -> task1.getTitle().compareTo(task2.getTitle()));} else if (currentSortOption == SortOption.DATE) {
            Collections.sort(taskList, (task1, task2) -> task1.getDateTime().compareTo(task2.getDateTime()));}
        if (taskList.isEmpty()) {
            mainBinding.emptyListAnim.setVisibility(View.VISIBLE);
            mainBinding.TaskRV.setVisibility(View.GONE);} else {
            mainBinding.emptyListAnim.setVisibility(View.GONE);
            mainBinding.TaskRV.setVisibility(View.VISIBLE);
            for (TaskModel task : taskList) {
                itemAdapter.add(new TaskItem(task, fastAdapter, MainActivity.this));}
        }
    }
    private void toggleSelection(int position) {
        TaskItem taskItem = fastAdapter.getItem(position);
        if (taskItem == null) return; // Check for null to avoid NullPointerException

        // Toggle the selection state
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
            taskItem.setSelected(false); // Update the TaskItem state
        } else {
            selectedPositions.add(position);
            taskItem.setSelected(true); // Update the TaskItem state
        }

        // Get the selected items as a list
        List<TaskItem> selectedItems = new ArrayList<>(fastAdapter.getSelectedItems());

        // Update the action mode title based on the selection count
        int selectedCount = selectedPositions.size();
        if (selectedCount > 0) {
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback);
            }
            actionMode.setTitle(selectedCount + " selected");
        } else if (actionMode != null) {
            actionMode.finish(); // Exit action mode if no items are selected
        }

        // Notify the adapter that the item has changed
        fastAdapter.notifyItemChanged(position);
    }
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu, menu);
            MenuItem deleteItem = menu.findItem(R.id.deleteTask);
            MenuItem searchItem  = menu.findItem(R.id.search);
            MenuItem sortItem = menu.findItem(R.id.sort);
            MenuItem archiveItem = menu.findItem(R.id.viewArchiveTask);
            deleteItem.setVisible(true);
            searchItem.setVisible(false);
            sortItem.setVisible(false);
            archiveItem.setVisible(false);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.deleteTask) {
                deleteSelectedTasks();
                mode.finish();
                return true;
            }
            return false;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            fastAdapter.deselect();
            for (int pos : selectedPositions) {
                TaskItem taskItem = fastAdapter.getItem(pos);
                if (taskItem != null) {
                    taskItem.setSelected(false);}}
            actionMode = null;
            selectedPositions.clear();
            fastAdapter.notifyDataSetChanged();
        }
    };
    private void deleteSelectedTasks() {
        for (int position : selectedPositions) {
            TaskItem taskItem = fastAdapter.getItem(position);
            if (taskItem != null) {
                taskDatabaseHelper.deleteTask(taskItem.getTask().getId());}}
        selectedPositions.clear();
        loadTasks();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem deleteItem = menu.findItem(R.id.deleteTask);
        deleteItem.setVisible(false);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search tasks...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterTasks(query);
                return false;}
            @Override
            public boolean onQueryTextChange(String newText) {
                filterTasks(newText);
                return false;}});
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sortByTitle) {
            sortTasksByTitle();
            return true;} else if (item.getItemId() == R.id.sortByDate) {
            sortTasksByDate();
            return true;} else if (item.getItemId() == R.id.viewArchiveTask) {
            Intent intent = new Intent(MainActivity.this, ArchiveTaskActivity.class);
            startActivity(intent);
            return true;} else {
            return super.onOptionsItemSelected(item);}
    }
    enum SortOption {
        TITLE, DATE
    }

    private SortOption currentSortOption = SortOption.DATE;

    private void sortTasksByTitle() {
        currentSortOption = SortOption.TITLE;
        loadTasks();
    }
    private void sortTasksByDate() {
        currentSortOption = SortOption.DATE;
        loadTasks();
    }
    private void filterTasks(String query) {
        query = query.toLowerCase().trim();
        itemAdapter.clear();
        List<TaskModel> filteredTasks = new ArrayList<>();
        for (TaskModel task : taskList) {
            if (task.getTitle().toLowerCase().contains(query)) {
                filteredTasks.add(task);}}
        for (TaskModel task : filteredTasks) {
            itemAdapter.add(new TaskItem(task, query,MainActivity.this));}
    }
    private void setupSwipeGestures() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                TaskItem taskItem = itemAdapter.getAdapterItem(position);

                if (direction == ItemTouchHelper.RIGHT) {
                    handleSwipeToDelete(taskItem, position);
                } else if (direction == ItemTouchHelper.LEFT) {
                    handleSwipeToArchive(taskItem, position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                drawSwipeBackground(c, viewHolder, dX);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(mainBinding.TaskRV);
    }
    public void handleSwipeToDelete(TaskItem taskItem, int position) {
        taskDatabaseHelper.deleteTask(taskItem.getTask().getId());
        itemAdapter.remove(position);
        loadTasks();
    }
    public void handleSwipeToArchive(TaskItem taskItem, int position) {
        taskDatabaseHelper.archiveTask(taskItem.getTask().getId());
        itemAdapter.remove(position);
        loadTasks();
    }
    private void drawSwipeBackground(Canvas c, RecyclerView.ViewHolder viewHolder, float dX) {
        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;
        if (dX > 0) {
            deleteBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
            deleteBackground.draw(c);
            int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = iconLeft + deleteIcon.getIntrinsicWidth();
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.draw(c);
        } else if (dX < 0) {
            archiveBackground.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            archiveBackground.draw(c);
            int iconMargin = (itemView.getHeight() - archiveIcon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = iconTop + archiveIcon.getIntrinsicHeight();
            int iconLeft = itemView.getRight() - iconMargin - archiveIcon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            archiveIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            archiveIcon.draw(c);
        }
    }
    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        loadTasks();
    }
}