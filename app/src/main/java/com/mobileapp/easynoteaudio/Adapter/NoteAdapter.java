package com.mobileapp.easynoteaudio.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mobileapp.easynoteaudio.R;
import com.mobileapp.easynoteaudio.data.NoteEntity;
import com.mobileapp.easynoteaudio.databinding.GridNotesBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteAdapter extends ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(NoteEntity note);
    }

    public interface OnNoteLongClickListener {
        void onNoteLongClick(NoteEntity note, View anchorView);
    }

    private final OnNoteClickListener clickListener;
    private final OnNoteLongClickListener longClickListener;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public static final DiffUtil.ItemCallback<NoteEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<NoteEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull NoteEntity oldItem, @NonNull NoteEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull NoteEntity oldItem, @NonNull NoteEntity newItem) {
            return java.util.Objects.equals(oldItem.getTitle(), newItem.getTitle())
                    && java.util.Objects.equals(oldItem.getContent(), newItem.getContent())
                    && oldItem.isPinned() == newItem.isPinned()
                    && oldItem.getColorTag() == newItem.getColorTag()
                    && oldItem.getUpdatedAt() == newItem.getUpdatedAt();
        }
    };

    public NoteAdapter(OnNoteClickListener clickListener, OnNoteLongClickListener longClickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GridNotesBinding binding = GridNotesBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new NoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteEntity note = getItem(position);
        holder.bind(note, clickListener, longClickListener);
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final GridNotesBinding binding;

        public NoteViewHolder(GridNotesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NoteEntity note, OnNoteClickListener clickListener, OnNoteLongClickListener longClickListener) {
            Context context = itemView.getContext();

            // Set Title (fallback to first line or "Untitled Note" if blank)
            String title = note.getTitle();
            if (title == null || title.trim().isEmpty()) {
                if (note.getContent() != null && !note.getContent().trim().isEmpty()) {
                    String[] lines = note.getContent().split("\n");
                    title = lines[0].trim();
                    if (title.length() > 30) {
                        title = title.substring(0, 30) + "...";
                    }
                } else {
                    title = "Untitled Note";
                }
            }
            binding.textViewTitle.setText(title);

            // Set Content
            binding.textViewContent.setText(note.getContent());

            // Set Timestamp
            long timeToFormat = note.getUpdatedAt() > 0 ? note.getUpdatedAt() : note.getCreatedAt();
            if (timeToFormat > 0) {
                binding.textViewDate.setText(DATE_FORMAT.format(new Date(timeToFormat)));
                binding.textViewDate.setVisibility(View.VISIBLE);
            } else {
                binding.textViewDate.setVisibility(View.GONE);
            }

            // Pinned indicator
            binding.pinIcon.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

            // Color category
            int tagColorRes;
            int cardBgRes;
            switch (note.getColorTag()) {
                case 1:
                    tagColorRes = R.color.note_tag_amber;
                    cardBgRes = R.color.note_bg_amber;
                    break;
                case 2:
                    tagColorRes = R.color.note_tag_green;
                    cardBgRes = R.color.note_bg_green;
                    break;
                case 3:
                    tagColorRes = R.color.note_tag_blue;
                    cardBgRes = R.color.note_bg_blue;
                    break;
                case 4:
                    tagColorRes = R.color.note_tag_purple;
                    cardBgRes = R.color.note_bg_purple;
                    break;
                default:
                    tagColorRes = R.color.note_tag_default;
                    cardBgRes = R.color.note_bg_default;
                    break;
            }

            binding.colorCategoryIndicator.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, tagColorRes))
            );
            binding.noteCard.setCardBackgroundColor(
                    ContextCompat.getColor(context, cardBgRes)
            );

            // Click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) clickListener.onNoteClick(note);
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onNoteLongClick(note, v);
                    return true;
                }
                return false;
            });
        }
    }
}
