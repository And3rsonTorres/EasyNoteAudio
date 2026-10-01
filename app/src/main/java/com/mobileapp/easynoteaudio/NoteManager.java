package com.mobileapp.easynoteaudio;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.mobileapp.easynoteaudio.Adapter.NoteAdapter;
import com.mobileapp.easynoteaudio.data.NoteEntity;
import com.mobileapp.easynoteaudio.databinding.FragmentNoteManagerBinding;
import com.mobileapp.easynoteaudio.viewmodel.NoteViewModel;

import java.util.List;

public class NoteManager extends Fragment {

    private FragmentNoteManagerBinding binding;
    private NoteViewModel noteViewModel;
    private NoteAdapter adapter;
    private TextToSpeechHelper tts;

    public NoteManager() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNoteManagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tts = new TextToSpeechHelper(requireContext());
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupFabButtons();
        observeNotes();
    }

    private void setupRecyclerView() {
        adapter = new NoteAdapter(this::openNote, this::showNoteOptionsDialog);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.notesRecyclerView.setLayoutManager(layoutManager);
        binding.notesRecyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString().trim();
                binding.clearSearchButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                noteViewModel.setSearchQuery(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.clearSearchButton.setOnClickListener(v -> {
            binding.searchEditText.setText("");
            binding.clearSearchButton.setVisibility(View.GONE);
        });
    }

    private void setupFabButtons() {
        // Add note FAB
        binding.addNote.setOnClickListener(v -> openNote(null));

        // Speak all notes FAB
        binding.speakNotes.setOnClickListener(v -> {
            if (tts != null && tts.isSpeaking()) {
                tts.stop();
            } else if (tts != null) {
                noteViewModel.getAllNotesSync(this::speakAllNotes);
            }
        });
    }

    private void observeNotes() {
        noteViewModel.getNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.submitList(notes);
            if (notes == null || notes.isEmpty()) {
                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.notesRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyStateLayout.setVisibility(View.GONE);
                binding.notesRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void openNote(@Nullable NoteEntity note) {
        if (tts != null) {
            tts.stop();
        }
        int noteId = note != null ? note.getId() : -1;
        NoteManagerDirections.ActionNoteManagerToNoteFragment action =
                NoteManagerDirections.actionNoteManagerToNoteFragment().setNoteId(noteId);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void showNoteOptionsDialog(NoteEntity note, View anchorView) {
        String pinActionText = note.isPinned() ? getString(R.string.unpin_note) : getString(R.string.pin_note);
        CharSequence[] options = new CharSequence[]{
                pinActionText,
                getString(R.string.share_note),
                getString(R.string.delete_note)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(note.getTitle().isEmpty() ? getString(R.string.notes) : note.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Toggle Pin
                            noteViewModel.togglePin(note);
                            break;
                        case 1: // Share Note
                            shareNote(note);
                            break;
                        case 2: // Delete Note
                            confirmDeleteNote(note);
                            break;
                    }
                })
                .show();
    }

    private void shareNote(NoteEntity note) {
        String shareBody = (note.getTitle().isEmpty() ? "" : note.getTitle() + "\n\n") + note.getContent();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_note_title)));
    }

    private void confirmDeleteNote(NoteEntity note) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_note)
                .setMessage(R.string.delete_note_confirm)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    noteViewModel.deleteNote(note);
                    Snackbar.make(binding.getRoot(), R.string.note_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v -> noteViewModel.insertNote(note, null))
                            .show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void speakAllNotes(List<NoteEntity> notes) {
        if (tts == null) return;
        if (notes == null || notes.isEmpty()) {
            tts.speak("You have no notes to read.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (NoteEntity note : notes) {
            sb.append("Note ").append(index).append(". ");
            if (!note.getTitle().isEmpty()) {
                sb.append(note.getTitle()).append(". ");
            }
            sb.append(note.getContent()).append(". ");
            index++;
        }
        tts.speak(sb.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) {
            tts.release();
            tts = null;
        }
        binding = null;
    }
}
