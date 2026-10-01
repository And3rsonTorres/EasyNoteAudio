package com.mobileapp.easynoteaudio;

import com.mobileapp.easynoteaudio.data.NoteEntity;
import com.mobileapp.easynoteaudio.data.ToDoEntity;

import org.junit.Test;

import static org.junit.Assert.*;

public class DataModelUnitTest {

    @Test
    public void testNoteEntityCreationAndProperties() {
        long now = System.currentTimeMillis();
        NoteEntity note = new NoteEntity("Project Ideas", "Build audio accessibility features", 3, true, now, now);

        assertEquals("Project Ideas", note.getTitle());
        assertEquals("Build audio accessibility features", note.getContent());
        assertEquals(3, note.getColorTag());
        assertTrue(note.isPinned());
        assertEquals(now, note.getCreatedAt());
        assertEquals(now, note.getUpdatedAt());

        // Update properties
        note.setPinned(false);
        assertFalse(note.isPinned());

        note.setColorTag(1);
        assertEquals(1, note.getColorTag());

        note.setTitle("Updated Title");
        assertEquals("Updated Title", note.getTitle());
    }

    @Test
    public void testToDoEntityCreationAndProperties() {
        long now = System.currentTimeMillis();
        long dueDate = now + 86400000L; // 1 day later
        ToDoEntity task = new ToDoEntity("Submit assignment", 0, 2, dueDate, now);

        assertEquals("Submit assignment", task.getTask());
        assertEquals(0, task.getStatus());
        assertEquals(2, task.getPriority());
        assertEquals(dueDate, task.getDueDate());

        // Mark as completed
        task.setStatus(1);
        assertEquals(1, task.getStatus());

        // Change priority to Low (0)
        task.setPriority(0);
        assertEquals(0, task.getPriority());
    }

    @Test
    public void testNoteNullSafety() {
        NoteEntity note = new NoteEntity(null, null, 0, false, 0, 0);
        assertNotNull(note.getTitle());
        assertEquals("", note.getTitle());
        assertNotNull(note.getContent());
        assertEquals("", note.getContent());

        note.setTitle(null);
        assertEquals("", note.getTitle());

        note.setContent(null);
        assertEquals("", note.getContent());
    }

    @Test
    public void testToDoNullSafety() {
        ToDoEntity task = new ToDoEntity(null, 0, 0, 0, 0);
        assertNotNull(task.getTask());
        assertEquals("", task.getTask());

        task.setTask(null);
        assertEquals("", task.getTask());
    }
}
