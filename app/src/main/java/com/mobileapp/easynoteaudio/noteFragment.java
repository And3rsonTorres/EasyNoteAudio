package com.mobileapp.easynoteaudio;

import static android.app.Activity.RESULT_OK;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mobileapp.easynoteaudio.data.NoteEntity;
import com.mobileapp.easynoteaudio.databinding.FragmentNoteBinding;
import com.mobileapp.easynoteaudio.viewmodel.NoteViewModel;

import java.util.ArrayList;
import java.util.Locale;

public class noteFragment extends Fragment {

    private FragmentNoteBinding mBinding;
    private NoteViewModel noteViewModel;
    private TextToSpeechHelper tts;

    private int noteId = -1;
    private NoteEntity currentNote = null;
    private int selectedColorTag = 0;
    private boolean isPinned = false;
    private boolean isBulletListModeActive = false;

    private final ActivityResultLauncher<Intent> speechResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> spokenResults =
                            result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (spokenResults != null && !spokenResults.isEmpty()) {
                        String spokenText = spokenResults.get(0);
                        insertTextAtCursor(spokenText);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentNoteBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tts = new TextToSpeechHelper(requireContext());
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            noteId = noteFragmentArgs.fromBundle(args).getNoteId();
        }

        setupKeyboardVisibility();
        setupColorPicker();
        setupActionButtons();
        setupTextWatchers();
        loadExistingNote();
    }

    private void loadExistingNote() {
        if (noteId != -1) {
            noteViewModel.getNoteById(noteId).observe(getViewLifecycleOwner(), note -> {
                if (note != null && currentNote == null) {
                    currentNote = note;
                    mBinding.editTextTitle.setText(note.getTitle());
                    mBinding.editTextNotes.setText(note.getContent());
                    selectedColorTag = note.getColorTag();
                    isPinned = note.isPinned();
                    updateColorSelectionUI();
                    updatePinButtonUI();
                    updateStats();
                }
            });
        } else {
            updateColorSelectionUI();
            updatePinButtonUI();
            updateStats();
        }
    }

    private void setupTextWatchers() {
        mBinding.editTextNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (isBulletListModeActive && count > before) {
                    String str = text.toString();
                    if (str.length() == 1) {
                        str = "• " + str;
                        mBinding.editTextNotes.setText(str);
                        mBinding.editTextNotes.setSelection(str.length());
                    } else if (str.endsWith("\n")) {
                        str = str.replace("\n", "\n• ");
                        str = str.replace("• •", "•");
                        mBinding.editTextNotes.setText(str);
                        mBinding.editTextNotes.setSelection(str.length());
                    }
                }
                updateStats();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mBinding.editTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateStats();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateStats() {
        String content = mBinding.editTextNotes.getText() != null ?
                mBinding.editTextNotes.getText().toString().trim() : "";
        int charCount = content.length();
        int wordCount = content.isEmpty() ? 0 : content.split("\\s+").length;
        mBinding.statsTextView.setText(getString(R.string.stats_format, wordCount, charCount));
    }

    private void setupActionButtons() {
        // Pin Button
        mBinding.buttonPin.setOnClickListener(v -> {
            isPinned = !isPinned;
            updatePinButtonUI();
        });

        // Share Button
        mBinding.buttonShare.setOnClickListener(v -> shareCurrentNote());

        // Speak Note (TTS)
        mBinding.speakNotes.setOnClickListener(v -> {
            if (tts != null && tts.isSpeaking()) {
                tts.stop();
            } else if (tts != null) {
                String title = mBinding.editTextTitle.getText().toString().trim();
                String content = mBinding.editTextNotes.getText().toString().trim();
                String toSpeak = (title.isEmpty() ? "" : title + ". ") + content;
                if (!toSpeak.trim().isEmpty()) {
                    tts.speak(toSpeak);
                }
            }
        });

        // Save Note
        mBinding.saveNote.setOnClickListener(v -> saveNoteAndExit());

        // Speech-to-Text
        mBinding.speechToTextButton.setOnClickListener(v -> startSpeechToText());

        // Bullet List Toggle
        mBinding.bulletPointsButton.setOnClickListener(v -> {
            isBulletListModeActive = !isBulletListModeActive;
            mBinding.bulletPointsButton.setAlpha(isBulletListModeActive ? 0.5f : 1.0f);
        });
    }

    private void setupColorPicker() {
        mBinding.colorTagDefault.setOnClickListener(v -> selectColorTag(0));
        mBinding.colorTagAmber.setOnClickListener(v -> selectColorTag(1));
        mBinding.colorTagGreen.setOnClickListener(v -> selectColorTag(2));
        mBinding.colorTagBlue.setOnClickListener(v -> selectColorTag(3));
        mBinding.colorTagPurple.setOnClickListener(v -> selectColorTag(4));
    }

    private void selectColorTag(int tag) {
        selectedColorTag = tag;
        updateColorSelectionUI();
    }

    private void updateColorSelectionUI() {
        float unselectedScale = 0.8f;
        float selectedScale = 1.25f;

        mBinding.colorTagDefault.setScaleX(selectedColorTag == 0 ? selectedScale : unselectedScale);
        mBinding.colorTagDefault.setScaleY(selectedColorTag == 0 ? selectedScale : unselectedScale);

        mBinding.colorTagAmber.setScaleX(selectedColorTag == 1 ? selectedScale : unselectedScale);
        mBinding.colorTagAmber.setScaleY(selectedColorTag == 1 ? selectedScale : unselectedScale);

        mBinding.colorTagGreen.setScaleX(selectedColorTag == 2 ? selectedScale : unselectedScale);
        mBinding.colorTagGreen.setScaleY(selectedColorTag == 2 ? selectedScale : unselectedScale);

        mBinding.colorTagBlue.setScaleX(selectedColorTag == 3 ? selectedScale : unselectedScale);
        mBinding.colorTagBlue.setScaleY(selectedColorTag == 3 ? selectedScale : unselectedScale);

        mBinding.colorTagPurple.setScaleX(selectedColorTag == 4 ? selectedScale : unselectedScale);
        mBinding.colorTagPurple.setScaleY(selectedColorTag == 4 ? selectedScale : unselectedScale);
    }

    private void updatePinButtonUI() {
        mBinding.buttonPin.setImageResource(isPinned ? R.drawable.ic_pin : R.drawable.ic_pin_outline);
        mBinding.buttonPin.setColorFilter(isPinned ?
                ContextCompat.getColor(requireContext(), R.color.accent) :
                ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }

    private void setupKeyboardVisibility() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            mBinding.notesFeatures.setVisibility(isKeyboardVisible ? View.VISIBLE : View.GONE);
            return insets;
        });
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.note_text_2));

        try {
            speechResultLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Speech-to-text is not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertTextAtCursor(String textToInsert) {
        int start = Math.max(mBinding.editTextNotes.getSelectionStart(), 0);
        int end = Math.max(mBinding.editTextNotes.getSelectionEnd(), 0);
        Editable editable = mBinding.editTextNotes.getText();
        if (editable != null) {
            editable.replace(Math.min(start, end), Math.max(start, end), " " + textToInsert + " ");
        }
    }

    private void shareCurrentNote() {
        String title = mBinding.editTextTitle.getText().toString().trim();
        String content = mBinding.editTextNotes.getText().toString().trim();
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(getContext(), "Cannot share an empty note", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareBody = (title.isEmpty() ? "" : title + "\n\n") + content;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_note_title)));
    }

    private void saveNoteAndExit() {
        String title = mBinding.editTextTitle.getText().toString().trim();
        String content = mBinding.editTextNotes.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            // Nothing to save
            Navigation.findNavController(requireView()).navigateUp();
            return;
        }

        long now = System.currentTimeMillis();
        if (currentNote != null) {
            currentNote.setTitle(title);
            currentNote.setContent(content);
            currentNote.setColorTag(selectedColorTag);
            currentNote.setPinned(isPinned);
            noteViewModel.updateNote(currentNote);
        } else {
            NoteEntity newNote = new NoteEntity(title, content, selectedColorTag, isPinned, now, now);
            noteViewModel.insertNote(newNote, null);
        }

        if (tts != null) {
            tts.stop();
        }
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) {
            tts.release();
            tts = null;
        }
        mBinding = null;
    }
}
