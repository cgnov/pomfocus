package com.example.pomfocus.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomfocus.FocusTimer;
import com.example.pomfocus.MainActivity;
import com.example.pomfocus.databinding.FragmentTimerBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class TimerFragment extends Fragment {

    private static final String TAG = "TimerFragment";
    public static boolean sBreakIsNext = false;
    public static boolean sCurrentlyWorking = false;
    public static int sPomodoroStage = 0;
    private FragmentTimerBinding mBinding;
    public FocusTimer mTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Implement view binding
        mBinding = FragmentTimerBinding.inflate(getLayoutInflater(), container, false);
        if(mTimer != null) {
            mTimer.mBinding = mBinding;
        }
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(sCurrentlyWorking) {
            mBinding.btnStart.setVisibility(View.GONE);
            mBinding.tvTimeLeft.setText("");
        } else {
            mBinding.tvTimeLeft.setText(getNextFull());
        }

        setStartButtonOnClickListener();
        setGestureDetectors();
    }

    private void setStartButtonOnClickListener() {
        mBinding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.btnStart.setVisibility(View.GONE);
                mTimer = new FocusTimer(getNextLength()*FocusTimer.MILLIS_PER_MINUTE, FocusTimer.MILLIS_PER_SECOND, getContext(), mBinding);
                mTimer.start();
                sCurrentlyWorking = true;
            }
        });
    }

    private void setGestureDetectors() {
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return !sCurrentlyWorking;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if(!sCurrentlyWorking) {
                    mBinding.tvTimeLeft.performClick();
                }
            }
        });

        mBinding.tvTimeLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(gestureDetector.onTouchEvent(motionEvent)) {
                    mBinding.tvTimeLeft.performClick();
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!sCurrentlyWorking) {
            mBinding.tvTimeLeft.setText(String.format(Locale.getDefault(), "%d:00", getNextLength()));
        }
    }

    public static int getNextLength() {
        if(!sBreakIsNext) {
            return FocusTimer.MINUTES_PER_POMODORO;
        } else if(sPomodoroStage == FocusTimer.SHORT_BREAKS_PER_LONG_BREAK){
            return FocusTimer.MINUTES_PER_LONG_BREAK;
        } else {
            return FocusTimer.MINUTES_PER_BREAK;
        }
    }

    public static String getNextFull() {
        return String.format(Locale.getDefault(), "%d:00", getNextLength());
    }

    public static void resetValues() {
        sCurrentlyWorking = false;
        sBreakIsNext = false;
        sPomodoroStage = 0;
        MainActivity.sTimerFragment = new TimerFragment();
    }

    public void refresh() {
        mBinding.tvTimeLeft.setText(getNextFull());
    }
}