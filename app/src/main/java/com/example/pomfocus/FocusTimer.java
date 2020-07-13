package com.example.pomfocus;

import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.pomfocus.databinding.FragmentTimerBinding;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Locale;

public class FocusTimer extends CountDownTimer {

    private static final String TAG = "FocusTimer";
    public static final int MILLIS_PER_SECOND = 1000;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_POMODORO = 25;
    public static final int LENGTH = MILLIS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_POMODORO;
    private FragmentTimerBinding mBind;
    private Context mContext;

    public FocusTimer(long millisInFuture, long countDownInterval, FragmentTimerBinding binding, Context context) {
        super(millisInFuture, countDownInterval);
        mBind = binding;
        mContext = context;
        mBind.btnStart.setVisibility(View.GONE);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        long seconds = millisUntilFinished/MILLIS_PER_SECOND;
        long minLeft = seconds/SECONDS_PER_MINUTE;
        long secLeft = seconds%SECONDS_PER_MINUTE;
        mBind.tvTimeLeft.setText(String.format(Locale.getDefault(), "%d:%02d", minLeft, secLeft));
    }

    @Override
    public void onFinish() {
        mBind.btnStart.setVisibility(View.VISIBLE);

        final Focus focus = new Focus();
        focus.setCreator(ParseUser.getCurrentUser());
        focus.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving focus session", e);
                    Toast.makeText(mContext, "Unable to save focus session", Toast.LENGTH_SHORT).show();
                } else {
                    ParseUser.getCurrentUser().increment(FocusUser.KEY_TOTAL, MINUTES_PER_POMODORO);
                    ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "problem saving total time", e);
                            }
                        }
                    });

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
                                Toast.makeText(mContext, "Error updating streak", Toast.LENGTH_SHORT).show();
                            } else {
                                // Compare new focus session date with most recent focus session date and update streak accordingly
                                Calendar newFocus = Calendar.getInstance();
                                Calendar oldFocus = Calendar.getInstance();
                                oldFocus.setTime(lastFocus.getCreatedAt());
                                newFocus.setTime(focus.getCreatedAt());
                                Log.i(TAG, newFocus.get(Calendar.DAY_OF_YEAR) + " " + oldFocus.get(Calendar.DAY_OF_YEAR));
                                if(newFocus.get(Calendar.DAY_OF_YEAR) + 1 == oldFocus.get(Calendar.DAY_OF_YEAR)) {
                                    increaseStreak();
                                }
                            }
                        }
                    });
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
                    Toast.makeText(mContext, "Problem increasing streak", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}