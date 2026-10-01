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
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    LiveData<List<NoteEntity>> getAllNotes();

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY isPinned DESC, updatedAt DESC")
    LiveData<List<NoteEntity>> searchNotes(String query);

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    LiveData<NoteEntity> getNoteById(int id);

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    NoteEntity getNoteByIdSync(int id);

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    List<NoteEntity> getAllNotesSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(NoteEntity note);

    @Update
    void update(NoteEntity note);

    @Delete
    void delete(NoteEntity note);

    @Query("DELETE FROM notes WHERE id = :id")
    void deleteById(int id);
}
