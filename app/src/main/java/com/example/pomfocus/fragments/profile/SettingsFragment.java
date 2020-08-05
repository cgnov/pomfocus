package com.example.pomfocus.fragments.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.pomfocus.TimerTextView;
import com.example.pomfocus.fragments.TimerFragment;
import com.example.pomfocus.fragments.profile.blocks.ProfilePublicInfoFragment;
import com.example.pomfocus.parse.Focus;
import com.example.pomfocus.parse.FocusUser;
import com.example.pomfocus.LoginActivity;
import com.example.pomfocus.ParseApp;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentSettingsBinding;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    private File mPhotoFile;
    private FragmentSettingsBinding mBinding;
    public static final String[] KEYS = {FocusUser.KEY_FOCUS, FocusUser.KEY_SCREEN, FocusUser.KEY_PRIVATE,
            FocusUser.KEY_HIDE_FROM_LEADERBOARD, FocusUser.KEY_HIDE_SKIP_BREAK};
    private Switch[] switches;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSettingsBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switches = new Switch[]{mBinding.switchFocusMode, mBinding.switchPrivate, mBinding.switchPrivate,
                mBinding.switchHideFromLeaderboard, mBinding.switchHideSkip};

        displayProfileInfo();
        setUpSwitches();
        setUpGestureRecognizers();

        mBinding.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        mBinding.btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.btnLogOut.setText(getString(R.string.logging_out));
                mBinding.btnLogOut.setEnabled(false);
                TimerFragment.resetValues();
                ParseUser.logOut();
                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);
                requireActivity().finish();
            }
        });
    }

    private void displayProfileInfo() {
        mBinding.tvName.setText(ParseUser.getCurrentUser().getString(FocusUser.KEY_NAME));
        mBinding.tvHandle.setText(String.format("@%s", ParseUser.getCurrentUser().getUsername()));

        // If user has uploaded a picture, display that. Otherwise, display generic profile vector asset
        ParseFile avatar = ParseUser.getCurrentUser().getParseFile(FocusUser.KEY_AVATAR);
        ProfilePublicInfoFragment.displayAvatar(mBinding.ivAvatar, avatar);

        mBinding.tvFocusLength.setText(String.valueOf(FocusUser.getFocusLength()));
        mBinding.tvShortBreakLength.setText(String.valueOf(FocusUser.getShortBreakLength()));
        mBinding.tvLongBreakLength.setText(String.valueOf(FocusUser.getLongBreakLength()));
    }

    private void setUpGestureRecognizers() {
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (TimerFragment.sCurrentlyWorking) {
                    Toast.makeText(getContext(), "Cannot edit timer lengths while timer is running", Toast.LENGTH_SHORT).show();
                }
                return !TimerFragment.sCurrentlyWorking;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if(!TimerFragment.sCurrentlyWorking) {
                    mBinding.tvFocusLength.performClick();
                } else {
                    Toast.makeText(getContext(), "Cannot edit timer lengths while timer is running", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TimerTextView.setUpTouchListener(mBinding.tvFocusLength, gestureDetector);
        TimerTextView.setUpTouchListener(mBinding.tvShortBreakLength, gestureDetector);
        TimerTextView.setUpTouchListener(mBinding.tvLongBreakLength, gestureDetector);
    }

    private void setUpSwitches() {
        for (int i = 0; i < KEYS.length; i++) {
            switches[i].setChecked(ParseUser.getCurrentUser().getBoolean(KEYS[i]));
            if (!KEYS[i].equals("keepScreenOn")) {
                setUpSimpleListener(i);
            }
        }

        mBinding.switchKeepScreenOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ParseUser user = ParseUser.getCurrentUser();
                user.put(FocusUser.KEY_SCREEN, b);
                user.saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving keepScreenOn preference"));
                if (b && TimerFragment.sCurrentlyWorking) {
                    requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else if (!b && !TimerFragment.sCurrentlyWorking) {
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });
    }

    private void setUpSimpleListener(final int i) {
        switches[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ParseUser user = ParseUser.getCurrentUser();
                user.put(KEYS[i], b);
                user.saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving " + KEYS[i] + " preference"));
            }
        });
    }

    private void launchCamera() {
        // Create a File reference for future access
        String photoFileName = "photo.jpg";
        mPhotoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider (required for API >= 24)
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(requireContext(), "com.codepath.fileprovider.pomfocus", mPhotoFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // Ensures that there is a valid camera on the phone (prevents crash from lack of camera)
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        File mediaStorageDir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Communicate to user that saving is in progress
                mBinding.pbAvatar.setVisibility(View.VISIBLE);
                mBinding.ivAvatar.setImageResource(R.color.grey2);

                // Save photo to Parse
                final ParseFile newAvatar = new ParseFile(mPhotoFile);
                ParseUser user = ParseUser.getCurrentUser();
                user.put(FocusUser.KEY_AVATAR, newAvatar);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error while saving new avatar", e);
                            Toast.makeText(getActivity(), "Unable to save profile picture", Toast.LENGTH_SHORT).show();
                        } else {
                            mBinding.pbAvatar.setVisibility(View.GONE);
                            ProfilePublicInfoFragment.displayAvatar(mBinding.ivAvatar, newAvatar);
                        }
                    }
                });
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}