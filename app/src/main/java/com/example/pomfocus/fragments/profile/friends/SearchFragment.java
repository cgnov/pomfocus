package com.example.pomfocus.fragments.profile.friends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.pomfocus.parse.FocusUser;
import com.example.pomfocus.adapters.SearchResultAdapter;
import com.example.pomfocus.databinding.FragmentSearchBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    FragmentSearchBinding mBinding;
    SearchResultAdapter mAdapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSearchBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new SearchResultAdapter(getContext(), new ArrayList<ParseUser>());
        mBinding.rvSearchResults.setAdapter(mAdapter);

        mBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchResults(s.toLowerCase());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchResults(s.toLowerCase());
                return true;
            }
        });
    }

    private void searchResults(String searchString) {
        ParseQuery<ParseUser> queryName = ParseQuery.getQuery(ParseUser.class);
        queryName.whereStartsWith(FocusUser.KEY_NAME_LOWERCASE, searchString);

        ParseQuery<ParseUser> queryHandle = ParseQuery.getQuery(ParseUser.class);
        queryHandle.whereStartsWith(FocusUser.KEY_HANDLE, searchString);

        List<ParseQuery<ParseUser>> queries = new ArrayList<>();
        queries.add(queryName);
        queries.add(queryHandle);

        ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
        mainQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> searchResults, ParseException e) {
                mAdapter.addAll(searchResults);
            }
        });
    }
}