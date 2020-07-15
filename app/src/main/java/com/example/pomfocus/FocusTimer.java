package com.example.pomfocus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.pomfocus.databinding.FragmentTimerBinding;
import com.example.pomfocus.fragments.TimerFragment;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Locale;


public class FocusTimer extends CountDownTimer {

    private static final String TAG = "FocusTimer";
    public static final String CHANNEL_ID = "pomFocusNotif";
    public static final int MILLIS_PER_SECOND = 1000;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;
    public static final int MINUTES_PER_POMODORO = 25;
    public static final int MINUTES_PER_BREAK = 5;
    public static final int NOTIFICATION_ID = 492804;
    public final Context mContext;
    public FragmentTimerBinding mBinding;

    public FocusTimer(long millisInFuture, long countDownInterval, Context context, FragmentTimerBinding binding) {
        super(millisInFuture, countDownInterval);
        mContext = context;
        mBinding = binding;
        createNotificationChannel();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        long seconds = millisUntilFinished/MILLIS_PER_SECOND;
        long minLeft = seconds/SECONDS_PER_MINUTE;
        long secLeft = seconds%SECONDS_PER_MINUTE;
        mBinding.tvTimeLeft.setText(String.format(Locale.getDefault(), "%d:%02d", minLeft, secLeft));
    }

    @Override
    public void onFinish() {
        TimerFragment.currentlyWorking = false;
        mBinding.btnStart.setVisibility(View.VISIBLE);
        if(!TimerFragment.breakIsNext) {
            createFocusObject();
        }
        TimerFragment.breakIsNext = !TimerFragment.breakIsNext;
        mBinding.tvTimeLeft.setText(TimerFragment.getNextFull());

        String nextUp = (TimerFragment.breakIsNext) ? "take a break" : "get to work";

        Intent i = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(String.format("Time to %s!", nextUp))
                .setContentText("Keep it going by tapping here")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "testName";
            String description = "test description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void createFocusObject() {
        final Focus focus = new Focus();
        focus.setCreator(ParseUser.getCurrentUser());
        focus.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving focus session", e);
                    Toast.makeText(mContext, "Unable to save focus session", Toast.LENGTH_SHORT).show();
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
        Log.i(TAG, "checking streak");
        // Get last focus session to possibly update streak
        final ParseQuery<Focus> query = ParseQuery.getQuery(Focus.class);
        query.include(Focus.KEY_CREATOR);
        query.setSkip(1);
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
                    if(newFocus.get(Calendar.DAY_OF_YEAR) - 1 == oldFocus.get(Calendar.DAY_OF_YEAR)) {
                        increaseStreak();
                    }
                }
            }
        });
    }

    public void increaseStreak() {
        Log.i(TAG, "increasing streak");
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