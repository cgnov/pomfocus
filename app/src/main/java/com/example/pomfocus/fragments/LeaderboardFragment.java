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
import com.example.pomfocus.FocuserAdapter;
import com.example.pomfocus.databinding.FragmentLeaderboardBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {
    private static final String TAG = "LeaderboardFragment";
    private static final int NUM_REQUEST = 15;
    private FocuserAdapter mAdapter;
    private FragmentLeaderboardBinding mBind;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up view binding
        mBind = FragmentLeaderboardBinding.inflate(getLayoutInflater(), container, false);
        return mBind.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView
        mBind.rvWorldwide.setLayoutManager(new LinearLayoutManager(getContext()));
        List<ParseUser> focusers = new ArrayList<>();
        mAdapter = new FocuserAdapter(getContext(), focusers);
        mBind.rvWorldwide.setAdapter(mAdapter);

        mBind.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                queryFocusers();
            }
        });

        queryFocusers();
    }

    private void queryFocusers() {
        final ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.addDescendingOrder(FocusUser.KEY_TOTAL);
        query.setLimit(NUM_REQUEST);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> focusers, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                } else {
                    // Posts have been successfully queried, clear out old posts and replace
                    mAdapter.addAll(focusers);
                    mBind.swipeContainer.setRefreshing(false);
                }
            }
        });
    }
}