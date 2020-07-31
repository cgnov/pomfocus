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

import com.example.pomfocus.FocusUser;
import com.example.pomfocus.FriendAdapter;
import com.example.pomfocus.databinding.FragmentFriendsBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        final FriendAdapter adapter = new FriendAdapter(getContext(), new ArrayList<ParseObject>());
        mBinding.rvFriends.setAdapter(adapter);

        ParseQuery<ParseObject> query = ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS).getQuery();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> friends, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error accessing friends");
                } else {
                    adapter.addAll(friends);
                }
            }
        });
    }
}