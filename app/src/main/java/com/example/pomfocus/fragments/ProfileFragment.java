package com.example.pomfocus.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.pomfocus.FocusUser;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentProfileBinding;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private ParseUser mUser;
    private FragmentProfileBinding mBind;

    public ProfileFragment(ParseUser user) {
        this.mUser = user;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Implement view binding
        mBind = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        return mBind.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBind.tvFirstName.setText(mUser.getString(FocusUser.KEY_NAME));
        mBind.tvHandle.setText(String.format("@%s", mUser.getUsername()));

        // If user has uploaded a picture, display that. Otherwise, display generic profile vector asset
        ParseFile avatar = mUser.getParseFile(FocusUser.KEY_AVATAR);
        if (avatar != null) {
            Glide.with(view).load(avatar.getUrl()).into(mBind.ivAvatar);
        } else {
            mBind.ivAvatar.setImageResource(R.drawable.profile_24);
        }

        mBind.tvStreak.setText(String.valueOf(mUser.getInt(FocusUser.KEY_STREAK)));
        mBind.tvTotal.setText(String.valueOf(mUser.getLong(FocusUser.KEY_TOTAL)));
    }
}