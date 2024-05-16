package com.mobileapp.easynoteaudio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mobileapp.easynoteaudio.databinding.FragmentNoteManagerBinding;
import com.mobileapp.easynoteaudio.databinding.GridNotesBinding;

public class NoteManager extends Fragment {
    private FragmentNoteManagerBinding binding;
    private List<noteViewModel> noteList;
    private ArrayAdapter<noteViewModel> adapter;
    private TextToSpeechHelper tts;
    public NoteManager() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = binding.inflate(inflater, container, false);
        tts= new TextToSpeechHelper(getContext());
        View view = binding.getRoot();
        noteList = loadNotes();
        adapter = new NoteAdapter(requireContext(), R.layout.grid_notes, noteList);
        binding.gridNotes.setAdapter(adapter);

        // Set up item click listeners
        binding.gridNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                noteViewModel selectedNote = noteList.get(position);
                openNote(selectedNote.getTitle(),selectedNote.getContent(),position);
                    }

            });

        // Set up delete
        binding.gridNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // long click delete the selected note
                deleteNote(position);
                return true;
            }
        });

        binding.addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNote("","",-1);

            }
        });
        if (!NoteManagerArgs.fromBundle(getArguments()).getContent().isEmpty()){

            saveNote(NoteManagerArgs.fromBundle(getArguments()).getTitle(),
                    NoteManagerArgs.fromBundle(getArguments()).getContent(),
                    NoteManagerArgs.fromBundle(getArguments()).getPos());

        }
        binding.speakNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak(getAllContent());
            }
        });



        return view;
    }

    private void saveNote(String title, String content, int pos) {
        noteViewModel newNote = new noteViewModel(title, content);
        boolean isDuplicate= checkForDuplicate(content);
        // Check if the note already exists in the list
        if (isDuplicate) {
            Toast.makeText(this.getContext(), "NO CHANGES OR SIMILAR NOTE EXIST", Toast.LENGTH_SHORT).show();

        }else{
            if (pos > -1 && pos < noteList.size()) {
                noteList.set(pos, newNote);
            } else if (pos == -1) {
                noteList.add(newNote);
            }

            saveNotes(noteList);
        }


    }
    private boolean checkForDuplicate(String toFind) {
        for (noteViewModel note : noteList) {
            if (note.getContent().equals(toFind)) {
                return true; // Duplicate title found
            }
        }
        return false; // No duplicate title found
    }


    private void saveNotes(List<noteViewModel> notes) {
        Gson gson = new Gson();
        String json = gson.toJson(notes);
        SharedPreferences.Editor editor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString("notes", json);
        editor.apply();
    }

    private List<noteViewModel> loadNotes() {
        // Load the notes from SharedPreferences
        SharedPreferences prefs = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String json = prefs.getString("notes", null);

        if (json != null) {
            Gson gson = new Gson();
            noteViewModel[] noteArray = gson.fromJson(json, noteViewModel[].class);

            if (noteArray != null) {
                return new ArrayList<>(Arrays.asList(noteArray));
            }
        }
        return new ArrayList<>();
    }

    private void deleteNote(int position) {
        if (position >= 0 && position < noteList.size()) {
            noteList.remove(position);
            saveNotes(noteList);
            adapter.notifyDataSetChanged(); // Update the GridView
        }
    }
    private void openNote(String title, String content, int position) {
        NoteManagerDirections.ActionNoteManagerToNoteFragment action = NoteManagerDirections.actionNoteManagerToNoteFragment()
                    .setTitle(title)
                    .setContent(content)
                    .setPos(position);
        Navigation.findNavController(requireView()).navigate(action);
        tts.stop();
        tts.release();

    }
 public  String getAllContent(){
        StringBuilder content= new StringBuilder();
        Integer i=1;
         for(noteViewModel note : noteList ){
            content.append(" Note number" + i.toString() + " is").append(note.getContent());
            i++;
     }
         return content.toString();
 }

    // Custom ArrayAdapter to display both title and content in the GridView
    private static class NoteAdapter extends ArrayAdapter<noteViewModel> {
        private final int layoutResource;
        private GridNotesBinding gridBinding;
        public NoteAdapter(Context context, int resource, List<noteViewModel> notes) {
            super(context, resource, notes);
            this.layoutResource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                gridBinding = GridNotesBinding.inflate(inflater, parent, false);
                gridBinding.getRoot().setTag(gridBinding);
            } else {
                gridBinding = (GridNotesBinding) convertView.getTag();
            }

            noteViewModel note = getItem(position);

            if (note != null) {

                gridBinding.textViewTitle.setText(note.getTitle());
                gridBinding.textViewContent.setText(trimContent(note.getContent()));
            }

            return gridBinding.getRoot();
        }
        //make the content 1 line of min 15
        private String trimContent(String input) {
            int maxLength = 15;
            int index = Math.min(maxLength, input.length());
            int newlineIndex = input.indexOf('\n');
            if (newlineIndex != -1 && newlineIndex < index) {
                index = newlineIndex;
            }
            return input.substring(0, index);
        }

    }
}
