package com.mobileapp.easynoteaudio;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mobileapp.easynoteaudio.data.ToDoEntity;
import com.mobileapp.easynoteaudio.databinding.NewTaskBinding;
import com.mobileapp.easynoteaudio.viewmodel.ToDoViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTaskDialog";
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_TASK_TEXT = "task_text";
    private static final String ARG_PRIORITY = "priority";
    private static final String ARG_DUE_DATE = "due_date";

    private NewTaskBinding binding;
    private ToDoViewModel toDoViewModel;

    private int taskId = -1;
    private int selectedPriority = 0; // 0=Low, 1=Medium, 2=High
    private long selectedDueDate = 0;

    private static final SimpleDateFormat DUE_DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public AddNewTask() {
        // Required default public constructor
    }

    public static AddNewTask newInstance(int id, String task, int priority, long dueDate) {
        AddNewTask fragment = new AddNewTask();
        Bundle args = new Bundle();
        args.putInt(ARG_TASK_ID, id);
        args.putString(ARG_TASK_TEXT, task);
        args.putInt(ARG_PRIORITY, priority);
        args.putLong(ARG_DUE_DATE, dueDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = NewTaskBinding.inflate(inflater, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toDoViewModel = new ViewModelProvider(requireActivity()).get(ToDoViewModel.class);

        // Parse arguments if editing
        Bundle args = getArguments();
        boolean isUpdate = false;
        if (args != null && args.containsKey(ARG_TASK_ID)) {
            taskId = args.getInt(ARG_TASK_ID, -1);
            if (taskId != -1) {
                isUpdate = true;
                binding.dialogTitle.setText(R.string.newTask);
                String taskText = args.getString(ARG_TASK_TEXT, "");
                binding.newTaskText.setText(taskText);
                selectedPriority = args.getInt(ARG_PRIORITY, 0);
                selectedDueDate = args.getLong(ARG_DUE_DATE, 0);
            }
        }

        setupPriorityChips();
        setupDueDate();
        setupTextValidation();

        final boolean isEditMode = isUpdate;
        binding.cancelButton.setOnClickListener(v -> dismiss());
        binding.newTaskButton.setOnClickListener(v -> saveTask(isEditMode));
    }

    private void setupPriorityChips() {
        if (selectedPriority == 2) {
            binding.chipPriorityHigh.setChecked(true);
        } else if (selectedPriority == 1) {
            binding.chipPriorityMedium.setChecked(true);
        } else {
            binding.chipPriorityLow.setChecked(true);
        }

        binding.priorityChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipPriorityHigh)) {
                selectedPriority = 2;
            } else if (checkedIds.contains(R.id.chipPriorityMedium)) {
                selectedPriority = 1;
            } else {
                selectedPriority = 0;
            }
        });
    }

    private void setupDueDate() {
        updateDueDateUI();

        binding.dueDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (selectedDueDate > 0) {
                calendar.setTimeInMillis(selectedDueDate);
            }

            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, dayOfMonth);
                        selectedDueDate = chosen.getTimeInMillis();
                        updateDueDateUI();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        binding.clearDueDateButton.setOnClickListener(v -> {
            selectedDueDate = 0;
            updateDueDateUI();
        });
    }

    private void updateDueDateUI() {
        if (selectedDueDate > 0) {
            binding.dueDateButton.setText(DUE_DATE_FORMAT.format(new Date(selectedDueDate)));
            binding.clearDueDateButton.setVisibility(View.VISIBLE);
        } else {
            binding.dueDateButton.setText(R.string.set_due_date);
            binding.clearDueDateButton.setVisibility(View.GONE);
        }
    }

    private void setupTextValidation() {
        String initialText = binding.newTaskText.getText() != null ?
                binding.newTaskText.getText().toString().trim() : "";
        binding.newTaskButton.setEnabled(!initialText.isEmpty());

        binding.newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s != null && !s.toString().trim().isEmpty();
                binding.newTaskButton.setEnabled(hasText);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void saveTask(boolean isEditMode) {
        String text = binding.newTaskText.getText().toString().trim();
        if (text.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (isEditMode) {
            ToDoEntity task = new ToDoEntity(text, 0, selectedPriority, selectedDueDate, now);
            task.setId(taskId);
            toDoViewModel.updateTask(task);
        } else {
            ToDoEntity task = new ToDoEntity(text, 0, selectedPriority, selectedDueDate, now);
            toDoViewModel.insertTask(task);
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
