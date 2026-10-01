package com.mobileapp.easynoteaudio.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_tasks")
public class ToDoEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String task;
    private int status; // 0 = pending, 1 = completed
    private int priority; // 0 = Low, 1 = Medium, 2 = High
    private long dueDate; // epoch millis or 0
    private long createdAt;

    public ToDoEntity(String task, int status, int priority, long dueDate, long createdAt) {
        this.task = task == null ? "" : task;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task == null ? "" : task;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
