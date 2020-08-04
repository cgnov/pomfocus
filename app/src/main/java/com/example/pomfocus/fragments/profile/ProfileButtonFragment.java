package com.example.pomfocus.fragments.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomfocus.ParseApp;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentProfileButtonBinding;
import com.example.pomfocus.fragments.friends.FriendsFragment;
import com.example.pomfocus.fragments.HistoryFragment;
import com.example.pomfocus.fragments.SettingsFragment;

import org.jetbrains.annotations.NotNull;


public class ProfileButtonFragment extends Fragment {

    private static final String TAG = "ProfileButtonFragment";
    private FragmentProfileButtonBinding mBinding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentProfileButtonBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                ParseApp.addSlideTransition(fragmentTransaction);
                fragmentTransaction.replace(R.id.flContainer, new SettingsFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });

        mBinding.btnSeeHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                ParseApp.addSlideTransition(fragmentTransaction);
                fragmentTransaction.replace(R.id.flContainer, new HistoryFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });

        mBinding.btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                ParseApp.addSlideTransition(fragmentTransaction);
                fragmentTransaction.replace(R.id.flContainer, new FriendsFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });
    }
}