package com.example.pomfocus.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.pomfocus.FocusTimer;
import com.example.pomfocus.R;
import com.example.pomfocus.TimerTextView;
import com.example.pomfocus.parse.FocusUser;
import com.example.pomfocus.MainActivity;
import com.example.pomfocus.databinding.FragmentTimerBinding;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class TimerFragment extends Fragment {

    private static final String TAG = "TimerFragment";
    public static boolean sBreakIsNext = false;
    public static boolean sCurrentlyWorking = false;
    public static int sPomodoroStage = 0;
    public static final int LONG_BREAK_STAGE = 3;
    private FragmentTimerBinding mBinding;
    public FocusTimer mTimer;
    public NotificationManagerCompat mNotificationManager;

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
        mNotificationManager = NotificationManagerCompat.from(requireContext());
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (sCurrentlyWorking) {
            displayTimerRunningButtons();
            mBinding.tvTimeLeft.setText("");
        } else {
            mBinding.tvTimeLeft.setText(getNextFull());
            mBinding.ccTimerVisual.onChangeTime(100);
            setStartButtonText(requireContext(), mBinding);
            if (sBreakIsNext && !ParseUser.getCurrentUser().getBoolean(FocusUser.KEY_HIDE_SKIP_BREAK)) {
                mBinding.btnSkipBreak.setVisibility(View.VISIBLE);
            }
        }

        setSkipButtonOnClickListener();
        setStartButtonOnClickListener();
        setCancelButtonOnClickListener();
        setGestureDetectors();
    }

    private void displayTimerRunningButtons() {
        mBinding.btnStart.setVisibility(View.GONE);
        mBinding.btnSkipBreak.setVisibility(View.GONE);
        mBinding.btnCancel.setVisibility(View.VISIBLE);
    }

    public static void setStartButtonText(Context context, FragmentTimerBinding binding) {
        String startButtonText = context.getString(R.string.start);
        if (!sBreakIsNext) {
            startButtonText += " " + context.getString(R.string.focus);
        } else if (sPomodoroStage == LONG_BREAK_STAGE) {
            startButtonText += " " + context.getString(R.string.long_break);
        } else {
            startButtonText += " " + context.getString(R.string.short_break);
        }
        binding.btnStart.setText(startButtonText);
    }

    private void setSkipButtonOnClickListener() {
        mBinding.btnSkipBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set up changing bounds
                ChangeBounds changeBounds = new ChangeBounds();
                changeBounds.addTarget(mBinding.btnStart);

                // Set up fade-out
                Fade fadeOut = new Fade();
                fadeOut.setMode(Fade.OUT);
                fadeOut.addTarget(mBinding.btnSkipBreak);
                fadeOut.setDuration(300);

                // Add transitions and begin
                Transition transition = new TransitionSet().addTransition(changeBounds).addTransition(fadeOut);
                TransitionManager.beginDelayedTransition((ViewGroup) mBinding.btnStart.getParent(), transition);

                TimerFragment.sPomodoroStage++;
                TimerFragment.sBreakIsNext = !TimerFragment.sBreakIsNext;
                mBinding.tvTimeLeft.setText(TimerFragment.getNextFull());
                TimerFragment.setStartButtonText(requireContext(), mBinding);
                mBinding.btnSkipBreak.setVisibility(View.GONE);
            }
        });
    }

    private void setStartButtonOnClickListener() {
        mBinding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayTimerRunningButtons();
                if (ParseUser.getCurrentUser().getBoolean(FocusUser.KEY_SCREEN) && (getActivity() != null)) {
                    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                mTimer = new FocusTimer(getNextLength()*FocusTimer.MILLIS_PER_MINUTE, FocusTimer.MILLIS_PER_SECOND, getContext(), mBinding);
                mTimer.start();
                sCurrentlyWorking = true;
            }
        });
    }

    private void setCancelButtonOnClickListener() {
        mBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelTimer();
            }
        });
    }

    private void setGestureDetectors() {
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (sCurrentlyWorking) {
                    Toast.makeText(getContext(), "Cannot edit timer lengths while timer is running", Toast.LENGTH_SHORT).show();
                }
                return !sCurrentlyWorking;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if(!sCurrentlyWorking) {
                    mBinding.tvTimeLeft.performClick();
                } else {
                    Toast.makeText(getContext(), "Cannot edit timer lengths while timer is running", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TimerTextView.setUpTouchListener(mBinding.tvTimeLeft, gestureDetector);
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
            return FocusTimer.MIN_PER_FOCUS;
        } else if(sPomodoroStage == FocusTimer.SHORT_BREAKS_PER_LONG_BREAK){
            return FocusTimer.MIN_PER_LONG_BREAK;
        } else {
            return FocusTimer.MIN_PER_BREAK;
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

    public void cancelTimer() {
        mTimer.cancel();
        sCurrentlyWorking = false;
        ((MainActivity) requireActivity()).displayTimerFragment();
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}