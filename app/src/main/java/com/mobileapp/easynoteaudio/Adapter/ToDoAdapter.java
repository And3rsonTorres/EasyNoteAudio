package com.mobileapp.easynoteaudio.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mobileapp.easynoteaudio.R;
import com.mobileapp.easynoteaudio.data.ToDoEntity;
import com.mobileapp.easynoteaudio.databinding.TaskLayoutBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ToDoAdapter extends ListAdapter<ToDoEntity, ToDoAdapter.ViewHolder> {

    public interface OnTaskStatusChangeListener {
        void onStatusChange(ToDoEntity task, boolean isCompleted);
    }

    public interface OnTaskClickListener {
        void onTaskClick(ToDoEntity task);
    }

    private final OnTaskStatusChangeListener statusChangeListener;
    private final OnTaskClickListener clickListener;
    private static final SimpleDateFormat DUE_DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.getDefault());

    public static final DiffUtil.ItemCallback<ToDoEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<ToDoEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ToDoEntity oldItem, @NonNull ToDoEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ToDoEntity oldItem, @NonNull ToDoEntity newItem) {
            return java.util.Objects.equals(oldItem.getTask(), newItem.getTask())
                    && oldItem.getStatus() == newItem.getStatus()
                    && oldItem.getPriority() == newItem.getPriority()
                    && oldItem.getDueDate() == newItem.getDueDate();
        }
    };

    public ToDoAdapter(OnTaskStatusChangeListener statusChangeListener, OnTaskClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.statusChangeListener = statusChangeListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TaskLayoutBinding binding = TaskLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ToDoEntity task = getItem(position);
        holder.bind(task, statusChangeListener, clickListener);
    }

    public ToDoEntity getTaskAt(int position) {
        return getItem(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TaskLayoutBinding binding;

        public ViewHolder(TaskLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ToDoEntity task, OnTaskStatusChangeListener statusChangeListener, OnTaskClickListener clickListener) {
            Context context = itemView.getContext();
            binding.todoCheckBox.setText(task.getTask());

            // Strikethrough effect when completed
            boolean isCompleted = task.getStatus() != 0;
            binding.todoCheckBox.setOnCheckedChangeListener(null);
            binding.todoCheckBox.setChecked(isCompleted);

            if (isCompleted) {
                binding.todoCheckBox.setPaintFlags(binding.todoCheckBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.todoCheckBox.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            } else {
                binding.todoCheckBox.setPaintFlags(binding.todoCheckBox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                binding.todoCheckBox.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            }

            binding.todoCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (statusChangeListener != null) {
                    statusChangeListener.onStatusChange(task, isChecked);
                }
            });

            // Priority Badge
            int priorityColorRes;
            String priorityText;
            switch (task.getPriority()) {
                case 2:
                    priorityColorRes = R.color.priority_high;
                    priorityText = context.getString(R.string.priority_high);
                    break;
                case 1:
                    priorityColorRes = R.color.priority_medium;
                    priorityText = context.getString(R.string.priority_medium);
                    break;
                default:
                    priorityColorRes = R.color.priority_low;
                    priorityText = context.getString(R.string.priority_low);
                    break;
            }
            binding.priorityBadge.setText(priorityText);
            binding.priorityBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, priorityColorRes))
            );

            // Due Date
            if (task.getDueDate() > 0) {
                binding.dueDateLayout.setVisibility(View.VISIBLE);
                binding.dueDateText.setText(context.getString(R.string.due_date_label) + ": " +
                        DUE_DATE_FORMAT.format(new Date(task.getDueDate())));
            } else {
                binding.dueDateLayout.setVisibility(View.GONE);
            }

            // Click listener for editing
            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTaskClick(task);
                }
            });
        }
    }
}
