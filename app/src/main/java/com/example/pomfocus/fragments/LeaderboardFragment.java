package com.example.pomfocus.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pomfocus.Focus;
import com.example.pomfocus.FocusUser;
import com.example.pomfocus.FocusUserAdapter;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentLeaderboardBinding;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {
    
    private static final String TAG = "LeaderboardFragment";
    private static final int NUM_REQUEST = 10;
    private FocusUserAdapter mAdapter;
    private FocusUserAdapter mFriendsOnlyAdapter;
    private boolean mFriendsOnly;
    private FragmentLeaderboardBinding mBinding;

    public LeaderboardFragment(boolean friendsOnly) {
        mFriendsOnly = friendsOnly;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout. Can't use ViewBinding because TabLayout causes id error (Google issue 152606440)
        mBinding = FragmentLeaderboardBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView
        mBinding.rvLeaderboards.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new FocusUserAdapter(getContext(), new ArrayList<ParseUser>());
        mFriendsOnlyAdapter = new FocusUserAdapter(getContext(), new ArrayList<ParseObject>(), true);
        mBinding.rvLeaderboards.setAdapter(mAdapter);

        mBinding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                mFriendsOnlyAdapter.clear();
                queryUsers();
            }
        });

        queryUsers();

        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                assert tab.getText() != null;
                if (getString(R.string.all).equals(tab.getText().toString())) {
                    mFriendsOnly = false;
                    mFriendsOnlyAdapter.clear();
                } else {
                    mFriendsOnly = true;
                    mAdapter.clear();
                }
                queryUsers();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void queryUsers() {
        mBinding.pbLeaderboard.setVisibility(View.VISIBLE);
        if (mFriendsOnly) {
            queryFriends();
        } else {
            queryAll();
        }
    }

    private void queryFriends() {
        if (!mFriendsOnlyAdapter.equals(mBinding.rvLeaderboards.getAdapter())) {
            mBinding.rvLeaderboards.setAdapter(mFriendsOnlyAdapter);
        }
        ParseQuery<ParseObject> query = ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS).getQuery();
        query.addDescendingOrder(FocusUser.KEY_TOTAL);
        query.setLimit(NUM_REQUEST);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> topFriends, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                } else {
                    // Posts have been successfully queried, clear out old posts and replace
                    mFriendsOnlyAdapter.addAll(topFriends, true);
                    mBinding.swipeContainer.setRefreshing(false);
                    mBinding.pbLeaderboard.setVisibility(View.GONE);
                }
            }
        });
    }

    private void queryAll() {
        if (!mAdapter.equals(mBinding.rvLeaderboards.getAdapter())) {
            mBinding.rvLeaderboards.setAdapter(mAdapter);
        }
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.addDescendingOrder(FocusUser.KEY_TOTAL);
        query.setLimit(NUM_REQUEST);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> topFocusUsers, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                } else {
                    // Posts have been successfully queried, clear out old posts and replace
                    mAdapter.addAll(topFocusUsers);
                    mBinding.swipeContainer.setRefreshing(false);
                    mBinding.pbLeaderboard.setVisibility(View.GONE);
                }
            }
        });
    }
}