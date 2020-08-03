package com.example.pomfocus.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomfocus.adapters.RequestAdapter;
import com.example.pomfocus.databinding.FragmentFriendRequestsBinding;
import com.example.pomfocus.parse.FriendRequest;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FriendRequestsFragment extends Fragment {

    private static final String TAG = "FriendRequestsFragment";
    private FragmentFriendRequestsBinding mBinding;
    private RequestAdapter mAdapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentFriendRequestsBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView with friend requests
        mBinding.rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new RequestAdapter(getContext());
        queryRequests();
        mBinding.rvRequests.setAdapter(mAdapter);
    }

    private void queryRequests() {
        ParseQuery<FriendRequest> query = ParseQuery.getQuery(FriendRequest.class);
        query.whereEqualTo(FriendRequest.KEY_TO, FriendRequest.makePointer(ParseUser.getCurrentUser()));
        query.whereEqualTo(FriendRequest.KEY_STATUS, -1);
        query.include(FriendRequest.KEY_FROM);
        query.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> requests, ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Error getting requests", e);
                } else {
                    if (requests.size() != 0) {
                        mBinding.rvRequests.setVisibility(View.VISIBLE);
                        mAdapter.addAll(requests);
                    } else {
                        mBinding.tvNoRequestsPending.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
}