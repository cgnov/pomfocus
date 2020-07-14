package com.example.pomfocus;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

public class FocusService extends JobIntentService {

    private static final String TAG = "FocusService";
    public static final String ACTION = "com.example.pomfocus.FocusService";
    private FocusTimer mTimer;

    @Override
    protected void onHandleWork(@NonNull Intent intent) { }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if(intent != null) {
            int length = intent.getIntExtra("length", -1);
            mTimer = new FocusTimer(length*FocusTimer.MILLIS_PER_MINUTE, 1000, this);
            mTimer.start();
        }
        return START_STICKY;
    }

    // User has logged out, need to cancel timer
    @Override
    public void onDestroy() {
        mTimer.cancel();
    }
}
