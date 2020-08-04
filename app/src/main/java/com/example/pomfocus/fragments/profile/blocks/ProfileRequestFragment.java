package com.example.pomfocus.fragments.profile.blocks;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pomfocus.ParseApp;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentProfileRequestBinding;
import com.example.pomfocus.fragments.profile.ProfileFragment;
import com.example.pomfocus.fragments.profile.SettingsFragment;
import com.example.pomfocus.parse.FriendRequest;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

public class ProfileRequestFragment extends Fragment {

    private static final String TAG = "ProfileRequestFragment";
    private FragmentProfileRequestBinding mBinding;
    private ParseUser mUser;
    public static final int NONE = -2, SENT = 2;
    private int mStatus;
    private FriendRequest mRequest;

    public ProfileRequestFragment(ParseUser user, int status, @Nullable FriendRequest request) {
        mUser = user;
        mStatus = status;
        mRequest = request;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentProfileRequestBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(TAG, "checking which button, status: " + mStatus);
        if (mStatus == NONE) {
            setUpSendButton();
        } else if (mStatus == SENT) {
            setUpCancelButton();
        } else if (mStatus == FriendRequest.PENDING) {
            setUpRespondButton();
        } else {
            setUpFriendsButton();
        }
        Log.i(TAG, String.valueOf(mStatus));
    }

    private void setUpSendButton() {
        mBinding.btnFriendRequest.setText(getString(R.string.send_friend_request));
        mBinding.btnFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendRequest friendRequest = new FriendRequest();
                friendRequest.setStatus(FriendRequest.PENDING);
                friendRequest.setFrom(ParseUser.getCurrentUser());
                friendRequest.setTo(mUser);
                friendRequest.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error saving new friend request", e);
                            Toast.makeText(getContext(), "Unable to send friend request", Toast.LENGTH_SHORT).show();
                        } else {
                            setUpCancelButton();
                        }
                    }
                });
            }
        });
    }

    private void setUpCancelButton() {
        mBinding.btnFriendRequest.setText(getString(R.string.cancel_request));
        mBinding.btnFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRequest.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error deleting friend request");
                        } else {
                            setUpSendButton();
                        }
                    }
                });
            }
        });
    }

    private void setUpRespondButton() {
        mBinding.btnFriendRequest.setText(getString(R.string.respond_to_request));
        mBinding.btnFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                ParseApp.addSlideTransition(fragmentTransaction);
                fragmentTransaction.replace(R.id.flContainer, new SettingsFragment())
                        .addToBackStack(ProfileFragment.TAG)
                        .commit();
            }
        });
    }

    private void setUpFriendsButton() {
        mBinding.btnFriendRequest.setText(getString(R.string.friends));
        mBinding.btnFriendRequest.setEnabled(false);
    }
}