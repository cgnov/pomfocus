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

import com.example.pomfocus.Focus;
import com.example.pomfocus.HistoryAdapter;
import com.example.pomfocus.databinding.FragmentHistoryBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private static final int NUM_REQUEST = 25;
    FragmentHistoryBinding mBinding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mBinding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Focus> focuses = new ArrayList<>();
        final HistoryAdapter adapter = new HistoryAdapter(getContext(), focuses);
        final ParseQuery<Focus> query = ParseQuery.getQuery(Focus.class);
        query.addDescendingOrder(Focus.KEY_CREATED);
        // TODO: replace limit with request for most recent week
        query.setLimit(NUM_REQUEST);
        query.findInBackground(new FindCallback<Focus>() {
            @Override
            public void done(List<Focus> focusList, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                } else {
                    // Posts have been successfully queried, clear out old posts and replace
                    adapter.addAll(focusList);
                    mBinding.pbHistory.setVisibility(View.GONE);
                }
            }
        });
        mBinding.rvHistory.setAdapter(adapter);
    }
}