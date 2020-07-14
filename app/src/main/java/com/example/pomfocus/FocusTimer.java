package com.example.pomfocus;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.pomfocus.databinding.FragmentTimerBinding;

public class FocusTimer extends CountDownTimer {

    private static final String TAG = "FocusTimer";
    public static final int MILLIS_PER_SECOND = 1000;
    public static final int MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    public static final int MINUTES_PER_POMODORO = 25;
    public static final int MINUTES_PER_BREAK = 5;
    public final FocusService mService;
    private Context mContext; // Used for sending broadcast

    public FocusTimer(long millisInFuture, long countDownInterval, FocusService service) {
        super(millisInFuture, countDownInterval);
        mService = service;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        long seconds = millisUntilFinished/MILLIS_PER_SECOND;
        Intent i = new Intent(FocusService.ACTION);
        i.putExtra("secondsLeft", seconds);
        i.putExtra("timerComplete", false);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    @Override
    public void onFinish() {
        Intent i = new Intent(FocusService.ACTION);
        i.putExtra("timerComplete", true);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }
}