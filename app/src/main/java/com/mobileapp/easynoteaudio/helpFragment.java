package com.mobileapp.easynoteaudio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.mobileapp.easynoteaudio.databinding.HelpFragmentBinding;

public class helpFragment extends Fragment {
    private HelpFragmentBinding mbinding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mbinding = HelpFragmentBinding.inflate(inflater);

        // Inflate the layout for this fragment
        return mbinding.getRoot();
    }
}
