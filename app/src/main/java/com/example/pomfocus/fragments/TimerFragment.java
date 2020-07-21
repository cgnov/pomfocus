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
import android.widget.Toast;

import com.example.pomfocus.FocusTimer;
import com.example.pomfocus.databinding.FragmentTimerBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class TimerFragment extends Fragment implements GestureDetector.OnDoubleTapListener {

    private static final String TAG = "TimerFragment";
    public static boolean breakIsNext = false;
    public static boolean currentlyWorking = false;
    public static int pomodoroStage = 1;
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

        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                mBind.tvTimeLeft.performClick();
            }
        });

        mBind.tvTimeLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(gestureDetector.onTouchEvent(motionEvent)) {
                    mBind.tvTimeLeft.performClick();
                }
                return true;
            }
        });
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
        if(!breakIsNext) {
            return FocusTimer.MINUTES_PER_POMODORO;
        } else if(pomodoroStage == 4){
            return FocusTimer.MINUTES_PER_LONG_BREAK;
        } else {
            return FocusTimer.MINUTES_PER_BREAK;
        }
    }

    public static String getNextFull() {
        return String.format(Locale.getDefault(), "%d:00", getNextLength());
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        Toast.makeText(getContext(), "singleTapConfirmed", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        Toast.makeText(getContext(), "doubleTap", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        Toast.makeText(getContext(), "doubleTapEvent", Toast.LENGTH_SHORT).show();
        return true;
    }
}