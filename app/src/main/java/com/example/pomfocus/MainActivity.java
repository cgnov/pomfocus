package com.example.pomfocus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.pomfocus.fragments.LeaderboardFragment;
import com.example.pomfocus.fragments.ProfileFragment;
import com.example.pomfocus.fragments.TimerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public TimerFragment timerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerFragment = new TimerFragment();

        BottomNavigationView navigation = findViewById(R.id.bNavigation);
        setUpNavigationSelectedListeners(navigation);
        navigation.setSelectedItemId(R.id.action_timer);
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
                        fragment = timerFragment;
                        break;
                    case R.id.action_leaderboard:
                        fragment = new LeaderboardFragment();
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment(ParseUser.getCurrentUser());
                        break;
                    default:
                        Log.e(TAG, "Did not recognize item clicked");
                        break;
                }
                if(fragment!=null) {
                    fragmentManager.beginTransaction().replace(R.id.flContainer, fragment, "current").commit();
                }
                return true;
            }
        });
    }

    public void logOutUser(View view) {
        ParseUser.logOut();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}