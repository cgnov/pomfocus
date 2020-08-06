package com.example.pomfocus.fragments.profile.friends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pomfocus.ParseApp;
import com.example.pomfocus.fragments.profile.blocks.ProfileRequestFragment;
import com.example.pomfocus.parse.FocusUser;
import com.example.pomfocus.adapters.FriendAdapter;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentFriendsBinding;
import com.example.pomfocus.parse.FriendRequest;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";
    private FragmentFriendsBinding mBinding;
    private FriendAdapter mAdapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentFriendsBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new FriendAdapter(getContext());
        mBinding.rvFriends.setAdapter(mAdapter);

        checkUnprocessedFriends();

        mBinding.btnCheckPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                ParseApp.addSlideTransition(fragmentTransaction);
                fragmentTransaction.replace(R.id.flContainer, new FriendRequestsFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });

        mBinding.btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                ParseApp.addSlideTransition(fragmentTransaction);
                fragmentTransaction.replace(R.id.flContainer, new SearchFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });
    }

    private void checkUnprocessedFriends() {
        ParseQuery<FriendRequest> acceptedRequest = ParseQuery.getQuery(FriendRequest.class);
        acceptedRequest.whereEqualTo(FriendRequest.KEY_FROM, ParseUser.getCurrentUser());
        acceptedRequest.whereEqualTo(FriendRequest.KEY_STATUS, FriendRequest.ACCEPTED);
        acceptedRequest.include(FriendRequest.KEY_FROM);
        acceptedRequest.include(FriendRequest.KEY_TO);
        acceptedRequest.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> friendRequests, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting unprocessed accepted requests", e);
                } else {
                    saveUnprocessedRequests(friendRequests);
                }
            }
        });
    }

    private void saveUnprocessedRequests(List<FriendRequest> friendRequests) {
        for (FriendRequest friendRequest : friendRequests) {
            processAcceptance(friendRequest);
        }
        displayFriends();
    }

    private void processAcceptance(FriendRequest friendRequest) {
        Log.i(TAG, "Processing accepted request from current user to " + friendRequest.getToUsername());
        ParseRelation<ParseUser> friends = ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS);
        friends.add(FriendRequest.makePointer(friendRequest.getToUser()));
        ParseUser.getCurrentUser().saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving new accepted friend"));

        // Update friend request to have processed and accepted
        friendRequest.setStatus(FriendRequest.PROCESSED);
        friendRequest.saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving processed friend request"));
    }

    private void displayFriends() {
        ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS);
        ParseQuery<ParseUser> query = relation.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                mBinding.pbFriends.setVisibility(View.GONE);
                if (e != null) {
                    Log.e(TAG, "Error accessing friends");
                    Toast.makeText(getContext(), "Problem accessing friends", Toast.LENGTH_SHORT).show();
                } else {
                    mAdapter.addAll(friends);
                }
            }
        });
    }
}