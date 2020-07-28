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

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.pomfocus.databinding.FragmentTimerBinding;
import com.example.pomfocus.fragments.TimerFragment;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Locale;


@SuppressWarnings("CanBeFinal")
public class FocusTimer extends CountDownTimer {

    private static final String TAG = "FocusTimer";
    public static final String CHANNEL_ID = "pomFocusNotif";
    public static final int MILLIS_PER_SECOND = 1000;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;
    public static final int SHORT_BREAKS_PER_LONG_BREAK = 3;
    public static int MINUTES_PER_POMODORO = 25; // Not final because user can change these values
    public static int MINUTES_PER_BREAK = 5;
    public static int MINUTES_PER_LONG_BREAK = 15;
    public static final int NOTIFICATION_ID = 492804;
    public final Context mContext;
    public FragmentTimerBinding mBinding; // Not final because change in TimerFragment

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
        float percentLeft = ((float) millisUntilFinished) / (MILLIS_PER_MINUTE * TimerFragment.getNextLength());
        mBinding.tvTimeLeft.setText(String.format(Locale.getDefault(), "%d:%02d", minLeft, secLeft));
        mBinding.ccTimerVisual.onChangeTime(-percentLeft);
    }

    @Override
    public void onFinish() {
        TimerFragment.sCurrentlyWorking = false;
        mBinding.btnStart.setVisibility(View.VISIBLE);
        if(!TimerFragment.sBreakIsNext) {
            createFocusObject();
        } else {
            TimerFragment.sPomodoroStage++;
            TimerFragment.sPomodoroStage %= 4;
        }
        TimerFragment.sBreakIsNext = !TimerFragment.sBreakIsNext;
        mBinding.tvTimeLeft.setText(TimerFragment.getNextFull());

        sendTimerCompleteNotification();
    }

    private void sendTimerCompleteNotification() {
        String nextUp = (TimerFragment.sBreakIsNext)
                ? "take a break"
                : "get to work";
        if(TimerFragment.sBreakIsNext && (TimerFragment.sPomodoroStage == 4)) {
            nextUp = "take a long break";
        }

        Intent i = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.timer_24)
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
        focus.put(Focus.KEY_LENGTH, FocusTimer.MINUTES_PER_POMODORO);
        focus.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving focus session", e);
                    Toast.makeText(mContext, "Unable to save focus session", Toast.LENGTH_SHORT).show();
                } else {
                    increaseTotalTime();
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
}