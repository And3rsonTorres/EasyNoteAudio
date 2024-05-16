package com.mobileapp.easynoteaudio;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mobileapp.easynoteaudio.Model.ToDoModel;
import com.mobileapp.easynoteaudio.Adapter.ToDoAdapter;
import com.mobileapp.easynoteaudio.Utils.DataBaseHandler;
import com.mobileapp.easynoteaudio.databinding.FragmentToDoListBinding;


public class ToDoList extends Fragment implements DialogCloseListener {

    private ToDoAdapter tasksAdapter;
    private List<ToDoModel> taskList;
    private DataBaseHandler db;
    private final AddNewTask addNewTaskFragment = new AddNewTask(this);
    private FragmentToDoListBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentToDoListBinding.inflate(inflater);
        View view = binding.getRoot();

        db = new DataBaseHandler((FragmentActivity) getContext());
        db.openDataBase();

        taskList = new ArrayList<>();

        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tasksAdapter = new ToDoAdapter(db,this);
        binding.tasksRecyclerView.setAdapter(tasksAdapter);



        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(binding.tasksRecyclerView);


        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AddNewTask.newInstance().show(getFragmentManager(), AddNewTask.TAG);
                addNewTaskFragment.show(getFragmentManager(), AddNewTask.TAG);

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
    }



}