package com.gd.taskflow.database;

public class TaskModel {

    private int id;
    private String title;
    private String task;
    private String dateTime;
    private String color;
    private String background;
    private String photoPath;

    private int isComplete;


    public TaskModel(int id, String title, String task, String dateTime, String color, String background, String photoPath) {
        this.id = id;
        this.title = title;
        this.task = task;
        this.dateTime = dateTime;
        this.color = color;
        this.background = background;
        this.photoPath = photoPath;
    }

    public TaskModel(int id, String title, String task, String dateTime, String color, String background, String photoPath, int isComplete) {
        this.id = id;
        this.title = title;
        this.task = task;
        this.dateTime = dateTime;
        this.color = color;
        this.background = background;
        this.photoPath = photoPath;
        this.isComplete = isComplete;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public int isComplete() {
        return isComplete;
    }

    public void setComplete(int complete) {
        isComplete = complete;
    }
}
