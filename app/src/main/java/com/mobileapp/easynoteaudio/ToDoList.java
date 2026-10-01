package com.mobileapp.easynoteaudio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.mobileapp.easynoteaudio.Adapter.ToDoAdapter;
import com.mobileapp.easynoteaudio.data.ToDoEntity;
import com.mobileapp.easynoteaudio.databinding.FragmentToDoListBinding;
import com.mobileapp.easynoteaudio.viewmodel.ToDoViewModel;

public class ToDoList extends Fragment {

    private FragmentToDoListBinding binding;
    private ToDoViewModel toDoViewModel;
    private ToDoAdapter adapter;

    public ToDoList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentToDoListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toDoViewModel = new ViewModelProvider(requireActivity()).get(ToDoViewModel.class);

        setupRecyclerView();
        setupFilterChips();
        setupFab();
        observeTasks();
    }

    private void setupRecyclerView() {
        adapter = new ToDoAdapter(
                (task, isCompleted) -> toDoViewModel.updateStatus(task.getId(), isCompleted ? 1 : 0),
                this::openEditTaskDialog
        );

        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.tasksRecyclerView.setAdapter(adapter);

        // Setup Swipe actions
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < adapter.getCurrentList().size()) {
                    ToDoEntity task = adapter.getTaskAt(position);
                    if (direction == ItemTouchHelper.LEFT) {
                        // Swipe Left: Delete task
                        confirmDeleteTask(task, position);
                    } else {
                        // Swipe Right: Edit task
                        adapter.notifyItemChanged(position);
                        openEditTaskDialog(task);
                    }
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.tasksRecyclerView);
    }

    private void setupFilterChips() {
        binding.filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipPending)) {
                toDoViewModel.setFilter(0);
            } else if (checkedIds.contains(R.id.chipCompleted)) {
                toDoViewModel.setFilter(1);
            } else {
                toDoViewModel.setFilter(-1);
            }
        });
    }

    private void setupFab() {
        binding.fab.setOnClickListener(v -> {
            AddNewTask dialog = AddNewTask.newInstance(-1, "", 0, 0);
            dialog.show(getParentFragmentManager(), AddNewTask.TAG);
        });
    }

    private void observeTasks() {
        toDoViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);
            if (tasks == null || tasks.isEmpty()) {
                binding.emptyTasksLayout.setVisibility(View.VISIBLE);
                binding.tasksRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyTasksLayout.setVisibility(View.GONE);
                binding.tasksRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void openEditTaskDialog(ToDoEntity task) {
        AddNewTask dialog = AddNewTask.newInstance(task.getId(), task.getTask(), task.getPriority(), task.getDueDate());
        dialog.show(getParentFragmentManager(), AddNewTask.TAG);
    }

    private void confirmDeleteTask(ToDoEntity task, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_note)
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    toDoViewModel.deleteTask(task);
                    Snackbar.make(binding.getRoot(), R.string.task_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v -> toDoViewModel.insertTask(task))
                            .show();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> adapter.notifyItemChanged(position))
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}