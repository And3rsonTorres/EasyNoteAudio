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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mobileapp.easynoteaudio.databinding.FragmentNoteBinding;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class noteFragment extends Fragment {

    private FragmentNoteBinding mBinding;
    private noteViewModel noteViewModel;
    private TextToSpeechHelper tts;
    private static final int SPEECH_REQUEST_CODE = 100;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = mBinding.inflate(inflater, container, false);

        View view = mBinding.getRoot();
        String content = noteFragmentArgs.fromBundle(getArguments()).getContent();

        noteViewModel = new ViewModelProvider(this).get(noteViewModel.class);
        tts = new TextToSpeechHelper(getContext());
        // Sets the toolbar title to the note's title.
        noteViewModel.setContent(content);
        getActivity().setTitle(setNewTitle(noteFragmentArgs.fromBundle(getArguments()).getTitle()));
        // Sets the note's content.
        mBinding.editTextNotes.setText(noteViewModel.getContent());

        // Sets the Bullet List Button State.
        setBulletModeButtonState();

        mBinding.bulletPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteViewModel.toggleBulletListMode();
                // Show when the button is an active state.
                setBulletModeButtonState();

            }
        });

        mBinding.speechToTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });

        mBinding.editTextNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
                if (noteViewModel.isBulletListModeActive) {
                    if (lengthAfter > lengthBefore) {
                        if (text.toString().length() == 1) {
                            text = "◎ " + text;
                            mBinding.editTextNotes.setText(text.toString());
                            mBinding.editTextNotes.setSelection(mBinding.editTextNotes.getText().length());
                        }
                        if (text.toString().endsWith("\n")) {
                            text = text.toString().replace("\n", "\n◎ ");
                            text = text.toString().replace("◎ ◎", "◎");
                            mBinding.editTextNotes.setText(text.toString());
                            mBinding.editTextNotes.setSelection(mBinding.editTextNotes.getText().length());
                        }
                    }
                }


                getActivity().setTitle(text);

            }

            @Override
            public void afterTextChanged(Editable s) {
                noteViewModel.setContent(s.toString());
            }
        });

        KeyboardVisibilityEvent.setEventListener(getActivity(), new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isKeyboardOpen) {
                if (isKeyboardOpen) {
                    mBinding.notesFeatures.setVisibility(View.VISIBLE);
                } else {
                    mBinding.notesFeatures.setVisibility(View.INVISIBLE);
                }
            }
        });
        mBinding.saveNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noteFragmentDirections.ActionNoteFragmentToNoteManager action =
                                noteFragmentDirections.actionNoteFragmentToNoteManager()
                                .setTitle(setNewTitle(noteViewModel.getContent()))
                                .setContent(noteViewModel.getContent())
                                .setPos(noteFragmentArgs.fromBundle(getArguments()).getPos())
                                ;
                        tts.stop();
                        tts.release();

                        Navigation.findNavController(requireView()).navigate(action);
                    }
                });
        mBinding.speakNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak(noteViewModel.getContent());
            }
        });

        return view;
    }

    public void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        startActivityForResult(intent, SPEECH_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && result.size() > 0) {
                String spokenText = result.get(0);

                // Insert text at selected point in editText.
                int start = Math.max(mBinding.editTextNotes.getSelectionStart(), 0);
                int end = Math.max(mBinding.editTextNotes.getSelectionEnd(), 0);

                mBinding.editTextNotes.getText().replace(Math.min(start, end), Math.max(start, end),
                        spokenText, 0, spokenText.length());
            }
        }
    }

    private void setBulletModeButtonState() {
        if (noteViewModel.isBulletListModeActive) {
            mBinding.bulletPointsButton.setAlpha(.5f);
        } else {
            mBinding.bulletPointsButton.setAlpha(1);
        }
    }
    private  String setNewTitle(String input){
        return (input.length() <= 24) ? input : input.substring(0, 24);
    }
}
