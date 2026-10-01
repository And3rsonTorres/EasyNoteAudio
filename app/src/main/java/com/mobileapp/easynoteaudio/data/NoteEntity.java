package com.mobileapp.easynoteaudio.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private int colorTag; // 0=Default, 1=Amber, 2=Green, 3=Blue, 4=Purple
    private boolean isPinned;
    private long createdAt;
    private long updatedAt;

    public NoteEntity(String title, String content, int colorTag, boolean isPinned, long createdAt, long updatedAt) {
        this.title = title == null ? "" : title;
        this.content = content == null ? "" : content;
        this.colorTag = colorTag;
        this.isPinned = isPinned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? "" : content;
    }

    public int getColorTag() {
        return colorTag;
    }

    public void setColorTag(int colorTag) {
        this.colorTag = colorTag;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
