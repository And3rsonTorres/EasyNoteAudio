package com.mobileapp.easynoteaudio.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Database(entities = {NoteEntity.class, ToDoEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "easynote_audio_db";
    private static volatile AppDatabase sInstance;

    public abstract NoteDao noteDao();
    public abstract ToDoDao toDoDao();

    public static AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            AppExecutors.getInstance().diskIO().execute(() -> {
                                migrateLegacyData(context.getApplicationContext(), getInstance(context.getApplicationContext()));
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return sInstance;
    }

    private static void migrateLegacyData(Context context, AppDatabase database) {
        try {
            // 1. Migrate legacy notes from SharedPreferences (MainActivity.xml or default prefs)
            SharedPreferences prefs = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
            String json = prefs.getString("notes", null);
            if (json == null) {
                // Try default shared preferences as fallback
                prefs = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
                json = prefs.getString("notes", null);
            }

            if (json != null && !json.trim().isEmpty()) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                List<Map<String, Object>> legacyNotes = gson.fromJson(json, listType);

                if (legacyNotes != null) {
                    long now = System.currentTimeMillis();
                    for (Map<String, Object> map : legacyNotes) {
                        String title = map.get("title") != null ? map.get("title").toString() : "";
                        String content = map.get("content") != null ? map.get("content").toString() : "";
                        if (!title.isEmpty() || !content.isEmpty()) {
                            NoteEntity note = new NoteEntity(title, content, 0, false, now, now);
                            database.noteDao().insert(note);
                        }
                    }
                    Log.d(TAG, "Successfully migrated " + legacyNotes.size() + " legacy notes into Room");
                }
            }

            // 2. Migrate legacy To-Do tasks from SQLite "toDoListDataBase"
            File oldDbFile = context.getDatabasePath("toDoListDataBase");
            if (oldDbFile.exists()) {
                SQLiteDatabase oldDb = SQLiteDatabase.openDatabase(
                        oldDbFile.getAbsolutePath(),
                        null,
                        SQLiteDatabase.OPEN_READONLY
                );
                try {
                    Cursor cursor = oldDb.query("todo", null, null, null, null, null, null);
                    if (cursor != null) {
                        int taskCol = cursor.getColumnIndex("task");
                        int statusCol = cursor.getColumnIndex("status");
                        long now = System.currentTimeMillis();
                        int count = 0;
                        while (cursor.moveToNext()) {
                            String taskText = taskCol >= 0 ? cursor.getString(taskCol) : "";
                            int status = statusCol >= 0 ? cursor.getInt(statusCol) : 0;
                            if (taskText != null && !taskText.trim().isEmpty()) {
                                ToDoEntity task = new ToDoEntity(taskText, status, 0, 0, now);
                                database.toDoDao().insert(task);
                                count++;
                            }
                        }
                        cursor.close();
                        Log.d(TAG, "Successfully migrated " + count + " legacy tasks into Room");
                    }
                } finally {
                    oldDb.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during legacy data migration: " + e.getMessage(), e);
        }
    }
}
