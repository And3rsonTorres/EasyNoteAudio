package com.mobileapp.easynoteaudio.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.mobileapp.easynoteaudio.data.AppDatabase;
import com.mobileapp.easynoteaudio.data.AppExecutors;
import com.mobileapp.easynoteaudio.data.ToDoEntity;

import java.util.List;

public class ToDoViewModel extends AndroidViewModel {

    private final AppDatabase database;
    private final AppExecutors executors;
    private final MutableLiveData<Integer> statusFilter = new MutableLiveData<>(-1); // -1 = All, 0 = Pending, 1 = Completed
    private final LiveData<List<ToDoEntity>> tasks;

    public ToDoViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        executors = AppExecutors.getInstance();

        tasks = Transformations.switchMap(statusFilter, filter -> {
            if (filter == 0) {
                return database.toDoDao().getTasksByStatus(0);
            } else if (filter == 1) {
                return database.toDoDao().getTasksByStatus(1);
            } else {
                return database.toDoDao().getAllTasks();
            }
        });
    }

    public LiveData<List<ToDoEntity>> getTasks() {
        return tasks;
    }

    public void setFilter(int filter) {
        statusFilter.setValue(filter);
    }

    public void insertTask(ToDoEntity task) {
        executors.diskIO().execute(() -> database.toDoDao().insert(task));
    }

    public void updateTask(ToDoEntity task) {
        executors.diskIO().execute(() -> database.toDoDao().update(task));
    }

    public void updateStatus(int id, int status) {
        executors.diskIO().execute(() -> database.toDoDao().updateStatus(id, status));
    }

    public void deleteTask(ToDoEntity task) {
        executors.diskIO().execute(() -> database.toDoDao().delete(task));
    }
}
