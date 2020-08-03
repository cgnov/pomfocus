package com.example.pomfocus.fragments.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomfocus.databinding.FragmentProfileSnapshotBinding;

import org.jetbrains.annotations.NotNull;

public class ProfileSnapshotFragment extends Fragment {

    private static final String TAG = "ProfileSnapshotFragment";
    private FragmentProfileSnapshotBinding mBinding;
    private int mTotal;
    private int mStreak = -1;

    public ProfileSnapshotFragment(int total) {
        mTotal = total;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentProfileSnapshotBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.tvTotal.setText(String.valueOf(mTotal));
        if (mStreak != -1) {
            mBinding.tvStreak.setText(String.valueOf(mStreak));
        }
    }

    public void setStreak(int streak) {
        if (mBinding != null) {
            mBinding.tvStreak.setText(String.valueOf(streak));
        } else {
            mStreak = streak;
        }
    }
}