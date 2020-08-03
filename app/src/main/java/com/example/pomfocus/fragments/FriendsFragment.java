package com.example.pomfocus.fragments;

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

import com.example.pomfocus.ParseApp;
import com.example.pomfocus.parse.FocusUser;
import com.example.pomfocus.adapters.FriendAdapter;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentFriendsBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";
    FragmentFriendsBinding mBinding;

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
        final FriendAdapter adapter = new FriendAdapter(getContext());
        mBinding.rvFriends.setAdapter(adapter);

        ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS);
        ParseQuery<ParseUser> query = relation.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error accessing friends");
                } else {
                    adapter.addAll(friends);
                }
            }
        });

        mBinding.btnCheckPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getFragmentManager() != null;
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ParseApp.addSlideTransition(fragmentTransaction);
                fragmentTransaction.replace(R.id.flContainer, new FriendRequestsFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });

        mBinding.btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getFragmentManager() != null;
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ParseApp.addSlideTransition(fragmentTransaction);
                fragmentTransaction.replace(R.id.flContainer, new SearchFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });
    }
}