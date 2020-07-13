package com.example.pomfocus.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.pomfocus.FocusTimer;
import com.example.pomfocus.databinding.FragmentTimerBinding;

import org.jetbrains.annotations.NotNull;

public class TimerFragment extends Fragment {

    private static final String TAG = "TimerFragment";
    private FragmentTimerBinding mBind;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Implement view binding
        mBind = FragmentTimerBinding.inflate(getLayoutInflater(), container, false);
        return mBind.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBind.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FocusTimer timer = new FocusTimer(FocusTimer.LENGTH, FocusTimer.MILLIS_PER_SECOND, mBind, getContext());
                timer.start();
            }
        });
    }
}