package com.example.pomfocus.fragments.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentProfilePublicInfoBinding;
import com.example.pomfocus.parse.FocusUser;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

public class ProfilePublicInfoFragment extends Fragment {

    private static final String TAG = "ProfilePublicInfoFragment";
    private FragmentProfilePublicInfoBinding mBinding;
    private ParseUser mUser;

    public ProfilePublicInfoFragment(ParseUser user) {
        mUser = user;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentProfilePublicInfoBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.tvHandle.setText(String.format("@%s", mUser.getUsername()));
        mBinding.tvName.setText(mUser.getString(FocusUser.KEY_NAME));

        // If user has uploaded a picture, display that. Otherwise, display generic profile vector asset
        ParseFile avatar = mUser.getParseFile(FocusUser.KEY_AVATAR);
        displayAvatar(mBinding.ivAvatar, avatar);
    }

    public static void displayAvatar(ImageView view, ParseFile avatar) {
        if (avatar != null) {
            Glide.with(view)
                    .load(avatar.getUrl())
                    .circleCrop()
                    .placeholder(R.color.grey2)
                    .into(view);
        } else {
            view.setImageResource(R.drawable.profile_24);
        }
    }
}