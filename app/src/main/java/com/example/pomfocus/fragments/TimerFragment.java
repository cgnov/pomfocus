package com.example.pomfocus.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pomfocus.Focus;
import com.example.pomfocus.FocusService;
import com.example.pomfocus.FocusTimer;
import com.example.pomfocus.FocusUser;
import com.example.pomfocus.databinding.FragmentTimerBinding;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class TimerFragment extends Fragment {

    private static final String TAG = "TimerFragment";
    public boolean breakIsNext = false;
    public boolean currentlyWorking = false;
    private FragmentTimerBinding mBind;
    private final Intent mIntent;

    public TimerFragment(Intent i) {
        mIntent = i;
    }

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
        IntentFilter filter = new IntentFilter(FocusService.ACTION);
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).registerReceiver(br, filter);

        if(currentlyWorking) {
            mBind.btnStart.setVisibility(View.GONE);
            mBind.tvTimeLeft.setText("");
        } else {
            mBind.tvTimeLeft.setText(String.format(Locale.getDefault(), "%d:00", getNextLength()));
        }

        setStartButtonOnClickListener();
    }

    public void setStartButtonOnClickListener() {
        mBind.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBind.btnStart.setVisibility(View.GONE);
                if (mIntent.hasExtra("length")) {
                    mIntent.removeExtra("length");
                }
                mIntent.putExtra("length", getNextLength());
                Objects.requireNonNull(getActivity()).startService(mIntent);
                currentlyWorking = true;
            }
        });
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean timerComplete = intent.getBooleanExtra("timerComplete", false);
            if(timerComplete && currentlyWorking) {
                currentlyWorking = false;
                mBind.btnStart.setVisibility(View.VISIBLE);
                if(!breakIsNext) {
                    createFocusObject();
                }
                Log.i(TAG, "Pre: " + breakIsNext);
                breakIsNext = !breakIsNext;
                Log.i(TAG, "Post: " + breakIsNext);
                mBind.tvTimeLeft.setText(String.format(Locale.getDefault(), "%d:00", getNextLength()));
            } else {
                long totalSecondsLeft = intent.getLongExtra("secondsLeft", -1);
                if(totalSecondsLeft != -1) {
                    long minLeft = totalSecondsLeft/60;
                    long secLeft = totalSecondsLeft%60;
                    mBind.tvTimeLeft.setText(String.format(Locale.getDefault(), "%d:%02d", minLeft, secLeft));
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(FocusService.ACTION);
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).registerReceiver(br, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).unregisterReceiver(br);
    }

    public void createFocusObject() {
        final Focus focus = new Focus();
        focus.setCreator(ParseUser.getCurrentUser());
        focus.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving focus session", e);
                    Toast.makeText(getActivity(), "Unable to save focus session", Toast.LENGTH_SHORT).show();
                } else {
                    increaseTotalTime();
                    checkStreak(focus);
                }
            }
        });
    }

    public void increaseTotalTime() {
        ParseUser.getCurrentUser().increment(FocusUser.KEY_TOTAL, FocusTimer.MINUTES_PER_POMODORO);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "problem saving total time", e);
                }
            }
        });
    }

    public void checkStreak(final Focus focus) {
        // Get last focus session to possibly update streak
        final ParseQuery<Focus> query = ParseQuery.getQuery(Focus.class);
        query.include(Focus.KEY_CREATOR);
        query.whereEqualTo(Focus.KEY_CREATOR, ParseUser.getCurrentUser());
        query.addDescendingOrder(Focus.KEY_CREATED);
        query.getFirstInBackground(new GetCallback<Focus>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void done(Focus lastFocus, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error retrieving last focus, streak may not be accurately updated", e);
                    Toast.makeText(getActivity(), "Error updating streak", Toast.LENGTH_SHORT).show();
                } else {
                    // Compare new focus session date with most recent focus session date and update streak accordingly
                    Calendar newFocus = Calendar.getInstance();
                    Calendar oldFocus = Calendar.getInstance();
                    oldFocus.setTime(lastFocus.getCreatedAt());
                    newFocus.setTime(focus.getCreatedAt());
                    Log.i(TAG, newFocus.get(Calendar.DAY_OF_YEAR) + " " + oldFocus.get(Calendar.DAY_OF_YEAR));
                    if(newFocus.get(Calendar.DAY_OF_YEAR) - 1 == oldFocus.get(Calendar.DAY_OF_YEAR)) {
                        increaseStreak();
                    }
                }
            }
        });
    }

    public void increaseStreak() {
        ParseUser updatedUser = ParseUser.getCurrentUser();
        updatedUser.increment("streak");
        updatedUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error increasing streak", e);
                    Toast.makeText(getActivity(), "Problem increasing streak", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public int getNextLength() {
        return (breakIsNext) ? FocusTimer.MINUTES_PER_BREAK : FocusTimer.MINUTES_PER_POMODORO;
    }
}