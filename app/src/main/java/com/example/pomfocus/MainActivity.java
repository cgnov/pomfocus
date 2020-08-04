package com.example.pomfocus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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