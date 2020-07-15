package com.example.pomfocus.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomfocus.FocusTimer;
import com.example.pomfocus.databinding.FragmentTimerBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class TimerFragment extends Fragment {

    private static final String TAG = "TimerFragment";
    public static boolean breakIsNext = false;
    public static boolean currentlyWorking = false;
    private FragmentTimerBinding mBind;
    private FocusTimer mTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Implement view binding
        mBind = FragmentTimerBinding.inflate(getLayoutInflater(), container, false);
        if(mTimer != null) {
            mTimer.mBinding = mBind;
        }
        return mBind.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(currentlyWorking) {
            mBind.btnStart.setVisibility(View.GONE);
            mBind.tvTimeLeft.setText("");
        } else {
            mBind.tvTimeLeft.setText(getNextFull());
        }

        setStartButtonOnClickListener();
    }

    public void setStartButtonOnClickListener() {
        mBind.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBind.btnStart.setVisibility(View.GONE);
                mTimer = new FocusTimer(getNextLength()*FocusTimer.MILLIS_PER_MINUTE, FocusTimer.MILLIS_PER_SECOND, getContext(), mBind);
                mTimer.start();
                currentlyWorking = true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!currentlyWorking) {
            mBind.tvTimeLeft.setText(String.format(Locale.getDefault(), "%d:00", getNextLength()));
        }
    }

    public static int getNextLength() {
        return (breakIsNext) ? FocusTimer.MINUTES_PER_BREAK : FocusTimer.MINUTES_PER_POMODORO;
    }

    public static String getNextFull() {
        return String.format(Locale.getDefault(), "%d:00", getNextLength());
    }
}