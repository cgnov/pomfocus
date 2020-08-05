package com.example.pomfocus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.pomfocus.databinding.ActivityMainBinding;
import com.example.pomfocus.fragments.LeaderboardFragment;
import com.example.pomfocus.fragments.profile.ProfileFragment;
import com.example.pomfocus.fragments.TimerFragment;
import com.example.pomfocus.parse.FocusUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static TimerFragment sTimerFragment = new TimerFragment();
    public BottomNavigationView mNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if(getIntent() != null && getIntent().getBooleanExtra("fromNotification", false)) {
            sTimerFragment = new TimerFragment();
        }
        mNavigation = binding.bNavigation;
        setUpNavigationSelectedListeners(mNavigation);
        displayTimerFragment();
    }

    // Only called on app close and opening notif from app open, not sleep mode, but NOT when other app is opened from home page notif
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if ((sTimerFragment.mTimer != null) && ParseUser.getCurrentUser().getBoolean(FocusUser.KEY_FOCUS)) {
            Context context = sTimerFragment.mTimer.mContext;
            Intent i = new Intent(context, MainActivity.class);
            i.putExtra("fromNotification", true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FocusTimer.CHANNEL_ID)
                    .setSmallIcon(R.drawable.timer_24)
                    .setContentTitle("Oh no, you closed the app during focus mode!")
                    .setContentText("Restart your timer by tapping here")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            MainActivity.sTimerFragment.mNotificationManager.notify(FocusTimer.NOTIFICATION_ID, builder.build());
            sTimerFragment.cancelTimer();
        }
    }

    private void setUpNavigationSelectedListeners(final BottomNavigationView navigation) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        navigation.setItemIconTintList(getResources().getColorStateList(R.color.navigation_coloring));
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Check which item was clicked, start relevant fragment
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.action_timer:
                        fragment = sTimerFragment;
                        break;
                    case R.id.action_leaderboard:
                        fragment = new LeaderboardFragment(false);
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment(ParseUser.getCurrentUser());
                        break;
                    default:
                        Log.e(TAG, "Did not recognize item clicked");
                        break;
                }
                if(fragment != null) {
                    fragmentManager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.flContainer, fragment)
                            .commit();
                }
                return true;
            }
        });
    }

    public void displayTimerFragment() {
        sTimerFragment = new TimerFragment();
        mNavigation.setSelectedItemId(R.id.action_timer);
    }
}