package com.mobileapp.easynoteaudio.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.mobileapp.easynoteaudio.data.AppDatabase;
import com.mobileapp.easynoteaudio.data.AppExecutors;
import com.mobileapp.easynoteaudio.data.NoteEntity;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private final AppDatabase database;
    private final AppExecutors executors;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<NoteEntity>> notes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        executors = AppExecutors.getInstance();

        notes = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return database.noteDao().getAllNotes();
            } else {
                return database.noteDao().searchNotes(query.trim());
            }
        });
    }

    public LiveData<List<NoteEntity>> getNotes() {
        return notes;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<NoteEntity> getNoteById(int id) {
        return database.noteDao().getNoteById(id);
    }

    public void insertNote(NoteEntity note, Consumer<Long> callback) {
        executors.diskIO().execute(() -> {
            long newId = database.noteDao().insert(note);
            if (callback != null) {
                executors.mainThread().execute(() -> callback.accept(newId));
            }
        });
    }

    public void updateNote(NoteEntity note) {
        executors.diskIO().execute(() -> {
            note.setUpdatedAt(System.currentTimeMillis());
            database.noteDao().update(note);
        });
    }

    public void deleteNote(NoteEntity note) {
        executors.diskIO().execute(() -> database.noteDao().delete(note));
    }

    public void togglePin(NoteEntity note) {
        note.setPinned(!note.isPinned());
        updateNote(note);
    }

    public void getAllNotesSync(Consumer<List<NoteEntity>> callback) {
        executors.diskIO().execute(() -> {
            List<NoteEntity> list = database.noteDao().getAllNotesSync();
            executors.mainThread().execute(() -> callback.accept(list));
        });
    }
}
