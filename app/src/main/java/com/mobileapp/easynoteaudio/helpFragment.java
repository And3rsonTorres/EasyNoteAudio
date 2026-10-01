package com.mobileapp.easynoteaudio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobileapp.easynoteaudio.databinding.HelpFragmentBinding;

public class helpFragment extends Fragment {
    private HelpFragmentBinding mbinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mbinding = HelpFragmentBinding.inflate(inflater, container, false);
        return mbinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mbinding = null;
    }
}
