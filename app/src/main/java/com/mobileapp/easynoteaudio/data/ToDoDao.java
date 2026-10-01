package com.mobileapp.easynoteaudio.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ToDoDao {

    @Query("SELECT * FROM todo_tasks ORDER BY status ASC, priority DESC, id DESC")
    LiveData<List<ToDoEntity>> getAllTasks();

    @Query("SELECT * FROM todo_tasks WHERE status = :status ORDER BY priority DESC, id DESC")
    LiveData<List<ToDoEntity>> getTasksByStatus(int status);

    @Query("SELECT * FROM todo_tasks WHERE task LIKE '%' || :query || '%' ORDER BY status ASC, priority DESC, id DESC")
    LiveData<List<ToDoEntity>> searchTasks(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ToDoEntity task);

    @Update
    void update(ToDoEntity task);

    @Query("UPDATE todo_tasks SET status = :status WHERE id = :id")
    void updateStatus(int id, int status);

    @Query("UPDATE todo_tasks SET task = :text WHERE id = :id")
    void updateTaskText(int id, String text);

    @Delete
    void delete(ToDoEntity task);

    @Query("DELETE FROM todo_tasks WHERE id = :id")
    void deleteById(int id);
}
