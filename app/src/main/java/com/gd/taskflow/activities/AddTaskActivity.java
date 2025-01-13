package com.gd.taskflow.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gd.taskflow.R;
import com.gd.taskflow.database.TaskDatabaseHelper;
import com.gd.taskflow.database.TaskModel;
import com.gd.taskflow.databinding.ActivityAddTaskBinding;
import com.gd.taskflow.helperclasses.SimpleTextWatcher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;

public class AddTaskActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_BG = 1;
    private static final int PICK_IMAGE_TASK = 2;
    private static final int STORAGE_PERMISSION_CODE = 100;
    ActivityAddTaskBinding addTaskBinding;
    TaskDatabaseHelper taskDbHelper;
    private String selectedBackgroundUri = null;
    private String selectedPhotoUri = null;
    private int isComplete = 0;
    private String selectedColor = "#FFFFFF";
    private int taskId = -1;
    private boolean isEditMode = false;
    private boolean hasEdits = false;
    private EditText taskTitleET;
    private TextView dateTimeSubtitle;
    private MenuItem searchMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addTaskBinding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(addTaskBinding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.custom_actionbar);
        }
        taskTitleET = findViewById(R.id.editTextTitle);
        dateTimeSubtitle = findViewById(R.id.textViewSubtitle);
        taskDbHelper = new TaskDatabaseHelper(this);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TASK_ID")) {
            isEditMode = true;
            taskId = intent.getIntExtra("TASK_ID", -1);
            loadTaskData(taskId);
            addTaskBinding.taskSaveBTTV.setText("Update Task");}
        else {
            String currentDateTime = new SimpleDateFormat("dd/MM/yyyy hh:mm a ", Locale.getDefault()).format(new Date());
            dateTimeSubtitle.setText(currentDateTime);}
        dateTimeSubtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }});
        setupChangeListeners();
        addTaskBinding.taskSaveBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTask();
            }});
        addTaskBinding.taskColorBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPicker();
                hasEdits = true;}});
        addTaskBinding.taskBgBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker(PICK_IMAGE_BG);
                hasEdits = true;}});
        addTaskBinding.taskAddImgBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker(PICK_IMAGE_TASK);
                hasEdits = true;}});
        addTaskBinding.taskShareBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTask();
            }});
        addTaskBinding.taskImageIV.setOnLongClickListener(view -> {
            if (selectedPhotoUri != null) {
                showDeleteImageDialog();}
            return true;});}
    private void loadTaskData(int taskId) {
        TaskModel task = taskDbHelper.getTaskById(taskId);
        if (task != null) {
            taskTitleET.setText(task.getTitle());
            addTaskBinding.taskWriteET.setText(task.getTask());
            dateTimeSubtitle.setText(task.getDateTime());
            selectedBackgroundUri = task.getBackground();
            selectedPhotoUri = task.getPhotoPath();
            if (selectedBackgroundUri != null && !selectedBackgroundUri.isEmpty()) {
                addTaskBinding.blurOverlayView.setVisibility(View.VISIBLE);
                addTaskBinding.taskWriteET.setTextColor(Color.WHITE);
                addTaskBinding.taskWriteET.setHintTextColor(Color.WHITE);
                Glide.with(this)
                        .load(selectedBackgroundUri)
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                addTaskBinding.taskBgView.setBackground(resource);}
                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {}});}
            else { addTaskBinding.taskBgView.setBackgroundColor(Color.parseColor(task.getColor()));}
            if (selectedPhotoUri != null && !selectedPhotoUri.isEmpty()) {
                addTaskBinding.taskImageIV.setVisibility(View.VISIBLE);
                Glide.with(AddTaskActivity.this)
                        .load(selectedPhotoUri)
                        .into(addTaskBinding.taskImageIV);}
            else { addTaskBinding.taskImageIV.setVisibility(View.GONE);} }}
    private void setupChangeListeners() {
        taskTitleET.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                hasEdits = true;}
        });
        addTaskBinding.taskWriteET.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                hasEdits = true; }
        });
        dateTimeSubtitle.setOnClickListener(v -> {
            showDateTimePicker();
            hasEdits = true;});}
    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar selectedDate = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                selectedDate.set(year, monthOfYear, dayOfMonth);
                showTimePicker(selectedDate);}}, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();}
    private void showTimePicker(final Calendar selectedDate) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDate.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                String formattedDateTime = sdf.format(selectedDate.getTime());
                dateTimeSubtitle.setText(formattedDateTime);}}, selectedDate.get(Calendar.HOUR_OF_DAY), selectedDate.get(Calendar.MINUTE), false);
        timePickerDialog.show();}
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_task, menu);
        searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                highlightText(query);
                return true;}
            @Override
            public boolean onQueryTextChange(String newText) {
                highlightText(newText);
                return true;}});
        return true;}
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackPress();
            return true;}
        return super.onOptionsItemSelected(item);}
    private void highlightText(String searchText) {
        String fullText = addTaskBinding.taskWriteET.getText().toString();
        Spannable spannable = new SpannableString(fullText);
        addTaskBinding.taskWriteET.setText(spannable);
        if (!searchText.isEmpty()) {
            int index = fullText.toLowerCase().indexOf(searchText.toLowerCase());
            while (index >= 0) {
                spannable.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.highlight_color)),
                        index, index + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = fullText.toLowerCase().indexOf(searchText.toLowerCase(), index + searchText.length());}
            addTaskBinding.taskWriteET.setText(spannable);}}
    private void showDeleteImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete this image?");
        builder.setPositiveButton("Yes", (dialog, which) -> deleteTaskImage());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();}
    private void deleteTaskImage() {
        addTaskBinding.taskImageIV.setImageDrawable(null);
        addTaskBinding.taskImageIV.setVisibility(View.GONE);
        selectedPhotoUri = null;
        if (isEditMode && taskId != -1) {
            taskDbHelper.updateTaskImagePath(taskId, null);}}
    private void shareTask() {
        String taskTitle = taskTitleET.getText().toString();
        String taskContent = addTaskBinding.taskWriteET.getText().toString();
        String taskDateTime = dateTimeSubtitle.getText().toString();
        if (taskTitle.isEmpty() || taskContent.isEmpty()) {
            Toast.makeText(this, "Task title or content cannot be empty", Toast.LENGTH_SHORT).show();
            return;}
        String shareMessage = "Task: " + taskTitle + "\n\nDate & Time: " + taskDateTime + "\n\n" + taskContent;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share Task via"));}
    private void openColorPicker() {
        int currentColor = Color.parseColor(selectedColor);
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, currentColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {}
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                selectedColor = String.format("#%06X", (0xFFFFFF & color));
                addTaskBinding.taskBgView.setBackgroundColor(color);}});
        colorPicker.show();}
    private void openImagePicker(int requestCode) {
        if (checkStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, requestCode);} else {
            requestStoragePermission();}}
    private void saveTask() {
        String title = taskTitleET.getText().toString();
        String task = addTaskBinding.taskWriteET.getText().toString();
        String dateTime = dateTimeSubtitle.getText().toString();
        if (!title.isEmpty() && !task.isEmpty()) {
            if (isEditMode) {
                taskDbHelper.updateTask(taskId, title, task, dateTime, selectedColor, selectedBackgroundUri, selectedPhotoUri);
                Toast.makeText(AddTaskActivity.this, "Task updated!", Toast.LENGTH_SHORT).show();
            } else {
                taskDbHelper.addTask(title, task, dateTime, selectedColor, selectedBackgroundUri, selectedPhotoUri);
                Toast.makeText(AddTaskActivity.this, "Task saved!", Toast.LENGTH_SHORT).show();}
            finish();} else {
            Toast.makeText(AddTaskActivity.this, "Please enter a title and task", Toast.LENGTH_SHORT).show();}}
    private void showExitWithoutSavingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit without saving?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();}});
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();}});
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();}}
        } else if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            if (requestCode == PICK_IMAGE_BG) {
                this.selectedBackgroundUri = selectedImageUri.toString();
                addTaskBinding.blurOverlayView.setVisibility(View.VISIBLE);
                addTaskBinding.taskWriteET.setTextColor(Color.WHITE);
                addTaskBinding.taskWriteET.setHintTextColor(Color.WHITE);
                Glide.with(this)
                        .load(selectedImageUri)
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                addTaskBinding.taskBgView.setBackground(resource);}
                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {}});
            } else if (requestCode == PICK_IMAGE_TASK) {
                this.selectedPhotoUri = selectedImageUri.toString();
                Glide.with(this)
                        .load(selectedImageUri)
                        .into(addTaskBinding.taskImageIV);
                addTaskBinding.taskImageIV.setVisibility(View.VISIBLE);}}}
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;}}
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);}
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);}}
    private void handleBackPress() {
        if (hasEdits) {
            showExitWithoutSavingDialog();
        } else {
            super.onBackPressed();}}
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        handleBackPress();
    }
}