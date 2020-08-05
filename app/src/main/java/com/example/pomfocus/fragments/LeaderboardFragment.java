package com.example.pomfocus.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pomfocus.parse.FocusUser;
import com.example.pomfocus.adapters.FocusUserAdapter;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentLeaderboardBinding;
import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {
    
    private static final String TAG = "LeaderboardFragment";
    private static final int NUM_REQUEST = 10;
    private static final int ALL_INDEX = 0, FRIENDS_INDEX = 1;
    private FocusUserAdapter mAllAdapter;
    private FocusUserAdapter mFriendsAdapter;
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
        mAllAdapter = new FocusUserAdapter(getContext());
        mFriendsAdapter = new FocusUserAdapter(getContext());
        mBinding.rvLeaderboards.setAdapter(mAllAdapter);

        mBinding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryUsers();
            }
        });

        addTabListener();
        int tabSelected = (mFriendsOnly) ? FRIENDS_INDEX : ALL_INDEX;
        if (ParseUser.getCurrentUser().getBoolean(FocusUser.KEY_HIDE_FROM_LEADERBOARD)) {
            tabSelected = FRIENDS_INDEX;
        }
        mBinding.tabLayout.selectTab(mBinding.tabLayout.getTabAt(tabSelected));
    }

    private void addTabListener() {
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i(TAG, "tabSelected: " + tab.getText());
                displayRanking(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.i(TAG, "tabUnselected: " + tab.getText());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.i(TAG, "tabReselected: " + tab.getText());
                displayRanking(tab);
            }

            private void displayRanking(TabLayout.Tab tab) {
                assert tab.getText() != null;
                mFriendsOnly = getString(R.string.friends).equals(tab.getText().toString());
                queryUsers();
            }
        });
    }

    private void queryUsers() {
        Log.i(TAG, "queryUsers, mFriendsOnly: " + mFriendsOnly);
        mAllAdapter.clear();
        mFriendsAdapter.clear();
        mBinding.pbLeaderboard.setVisibility(View.VISIBLE);
        if (mFriendsOnly) {
            queryFriends();
        } else {
            queryAll();
        }
    }

    private void queryFriends() {
        if (mAllAdapter.equals(mBinding.rvLeaderboards.getAdapter()) && mFriendsOnly) {
            mBinding.rvLeaderboards.setAdapter(mFriendsAdapter);
        }
        ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS);
        ParseQuery<ParseUser> queryFriends = relation.getQuery();

        ParseQuery<ParseUser> querySelf = ParseQuery.getQuery(ParseUser.class);
        querySelf.whereEqualTo(FocusUser.KEY_HANDLE, ParseUser.getCurrentUser().getUsername());

        List<ParseQuery<ParseUser>> queryList = new ArrayList<>();
        queryList.add(queryFriends);
        queryList.add(querySelf);

        ParseQuery<ParseUser> fullQuery = ParseQuery.or(queryList);
        fullQuery.addDescendingOrder(FocusUser.KEY_TOTAL);
        fullQuery.setLimit(NUM_REQUEST);
        fullQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> topFriends, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                } else {
                    mFriendsAdapter.addAll(topFriends);
                    mBinding.swipeContainer.setRefreshing(false);
                    mBinding.pbLeaderboard.setVisibility(View.GONE);
                }
            }
        });
    }

    private void queryAll() {
        if (mFriendsAdapter.equals(mBinding.rvLeaderboards.getAdapter()) && !mFriendsOnly) {
            mBinding.rvLeaderboards.setAdapter(mAllAdapter);
        }
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.whereEqualTo(FocusUser.KEY_HIDE_FROM_LEADERBOARD, false);
        query.addDescendingOrder(FocusUser.KEY_TOTAL);
        query.setLimit(NUM_REQUEST);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> topFocusUsers, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                } else {
                    mAllAdapter.addAll(topFocusUsers);
                    mBinding.swipeContainer.setRefreshing(false);
                    mBinding.pbLeaderboard.setVisibility(View.GONE);
                }
            }
        });
    }
}