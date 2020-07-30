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

import com.example.pomfocus.FocusUser;
import com.example.pomfocus.FocusUserAdapter;
import com.example.pomfocus.databinding.FragmentLeaderboardBinding;
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
    private FragmentLeaderboardBinding mBinding;
    private boolean mFriendsOnly;

    public LeaderboardFragment(boolean friendsOnly) {
        mFriendsOnly = friendsOnly;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up view binding
        mBinding = FragmentLeaderboardBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView
        mBinding.rvWorldwide.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new FocusUserAdapter(getContext(), new ArrayList<ParseUser>());
        mFriendsOnlyAdapter = new FocusUserAdapter(getContext(), new ArrayList<ParseObject>(), true);
        mBinding.rvWorldwide.setAdapter(mAdapter);

        mBinding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                mFriendsOnlyAdapter.clear();
                queryUsers();
            }
        });

        queryUsers();

        mBinding.btnFriendsOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFriendsOnly = !mFriendsOnly;
                mAdapter.clear();
                mFriendsOnlyAdapter.clear();
                queryUsers();
            }
        });
    }

    private void queryUsers() {
        mBinding.pbLeaderboard.setVisibility(View.VISIBLE);
        if (mFriendsOnly) {
            if (!mFriendsOnlyAdapter.equals(mBinding.rvWorldwide.getAdapter())) {
                mBinding.rvWorldwide.setAdapter(mFriendsOnlyAdapter);
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
        } else {
            if (!mAdapter.equals(mBinding.rvWorldwide.getAdapter())) {
                Log.i(TAG, "adapters not the same");
                mBinding.rvWorldwide.setAdapter(mAdapter);
            } else {
                Log.i(TAG, "adapter is good");
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
}