package com.gd.taskflow.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    public Context context;
    private static final String DATABASE_NAME = "task_db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TASK = "task";
    public static final String COLUMN_DATE_TIME = "date_time";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_BACKGROUND = "background";
    private static final String COLUMN_PHOTO = "photo";
    public static final String COLUMN_ARCHIVED = "archived";
    public static final String COLUMN_COMPLETED = "completed";


    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_TASK + " TEXT, "
                + COLUMN_DATE_TIME + " TEXT,"
                + COLUMN_COLOR + " TEXT,"
                + COLUMN_BACKGROUND + " TEXT,"
                + COLUMN_PHOTO + " TEXT,"
                + COLUMN_ARCHIVED + " INTEGER DEFAULT 0, "
                + COLUMN_COMPLETED + " INTEGER DEFAULT 0"
                +")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Check if COLUMN_COMPLETED already exists before adding it
            if (!isColumnExists(db, TABLE_TASKS, COLUMN_COMPLETED)) {
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_COMPLETED + " INTEGER DEFAULT 0");
            }
        }
    }

    private boolean isColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        boolean exists = false;
        Cursor cursor = null;
        try {
            // Query the table to check if the column exists
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            int nameColumnIndex = cursor.getColumnIndexOrThrow("name");
            while (cursor.moveToNext()) {
                if (cursor.getString(nameColumnIndex).equals(columnName)) {
                    exists = true;
                    break;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return exists;
    }

    public void addTask(String title, String task, String dateTime, String color, String backgroundUri, String photoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_TASK, task);
        values.put(COLUMN_DATE_TIME, dateTime);
        values.put(COLUMN_COLOR, color);

        values.put(COLUMN_BACKGROUND, backgroundUri);

        values.put(COLUMN_BACKGROUND, backgroundUri);

        values.put(COLUMN_PHOTO, photoUri);

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }
    public void updateTask(int id, String title, String task, String dateTime, String color, String backgroundUri, String photoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_TASK, task);
        values.put(COLUMN_DATE_TIME, dateTime);
        values.put(COLUMN_COLOR, color);
        values.put(COLUMN_BACKGROUND, backgroundUri);
        values.put(COLUMN_PHOTO, photoUri);

        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateTaskImagePath(int taskId, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTO, imageUri);  // Update the photo column to the new image URI or null
        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public void archiveTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ARCHIVED, 1);  // Mark the task as archived
        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void unarchiveTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ARCHIVED, 0); // Mark as not archived
        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public void markTaskComplete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETED, 1); // Set as completed
        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void markTaskIncomplete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETED, 0); // Set as incomplete
        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<TaskModel> getAllArchiveTasks() {
        List<TaskModel> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_ARCHIVED + " = 1", null);  // Fetch only non-archived tasks
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String task = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME));
                String color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR));
                String background = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BACKGROUND));
                String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO));
                int isComplete = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED));


                taskList.add(new TaskModel(id,title,task,dateTime,color,background,photoPath,isComplete));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return taskList;
    }

    public TaskModel getTaskById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
            String task = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK));
            String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME));
            String color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR));
            String background = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BACKGROUND));
            String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO));

            cursor.close();
            return new TaskModel(id, title, task, dateTime, color, background, photoPath);
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public List<TaskModel> getAllUnarchiveTasks() {
        List<TaskModel> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_ARCHIVED + " = 0 ", null);  // Fetch only non-archived tasks
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String task = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME));
                String color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR));
                String background = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BACKGROUND));
                String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO));
                int isComplete = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED));

                taskList.add(new TaskModel(id,title,task,dateTime,color,background,photoPath,isComplete));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return taskList;
    }


}
