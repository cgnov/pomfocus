package com.example.pomfocus.fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pomfocus.Focus;
import com.example.pomfocus.FocusUser;
import com.example.pomfocus.LoginActivity;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentProfileBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    private final ParseUser mUser;
    private FragmentProfileBinding mBinding;
    private File mPhotoFile;
    private List<Focus> mFocuses;

    public ProfileFragment(ParseUser user) {
        this.mUser = user;
        findFullFocusHistory();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Implement view binding
        mBinding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.tvName.setText(mUser.getString(FocusUser.KEY_NAME));
        mBinding.tvHandle.setText(String.format("@%s", mUser.getUsername()));
        mBinding.tvStreak.setText("--");
        mBinding.tvTotal.setText(String.valueOf(mUser.getLong(FocusUser.KEY_TOTAL)));

        // If user has uploaded a picture, display that. Otherwise, display generic profile vector asset
        ParseFile avatar = mUser.getParseFile(FocusUser.KEY_AVATAR);
        displayAvatar(mBinding.ivAvatar, avatar);

        // Set up or hide personal buttons
        if(mUser.equals(ParseUser.getCurrentUser())) {
            setUpClickListeners();
        } else {
            hideButtons();
        }
    }

    private void hideButtons() {
        mBinding.btnTakePicture.setVisibility(View.GONE);
        mBinding.btnLogOut.setVisibility(View.GONE);
        mBinding.btnSeeHistory.setVisibility(View.GONE);
    }

    private void setUpClickListeners() {
        mBinding.btnTakePicture.setOnClickListener(new View.OnClickListener() {
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
                Objects.requireNonNull(getActivity()).finish();
            }
        });

        mBinding.btnSeeHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getFragmentManager() != null;
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flContainer, new HistoryFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });
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

    private void launchCamera() {
        // Create a File reference for future access
        String photoFileName = "photo.jpg";
        mPhotoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider (required for API >= 24)
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(Objects.requireNonNull(getContext()), "com.codepath.fileprovider.pomfocus", mPhotoFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // Ensures that there is a valid camera on the phone (prevents crash from lack of camera)
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        File mediaStorageDir = new File(Objects.requireNonNull(getContext()).getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

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
                mBinding.btnTakePicture.setText(getString(R.string.saving_picture));
                mBinding.btnTakePicture.setEnabled(false);
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
                            mBinding.btnTakePicture.setText(getString(R.string.take_picture));
                            mBinding.btnTakePicture.setEnabled(true);
                            displayAvatar(mBinding.ivAvatar, newAvatar);
                        }
                    }
                });
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void findFullFocusHistory() {
        Log.i(TAG, "Querying full focus history from Parse for user " + mUser.getUsername());
        ParseQuery<Focus> fullHistoryQuery = ParseQuery.getQuery(Focus.class);
        fullHistoryQuery.whereEqualTo(Focus.KEY_CREATOR, mUser);
        fullHistoryQuery.addDescendingOrder(Focus.KEY_CREATED_AT);

        fullHistoryQuery.findInBackground(new FindCallback<Focus>() {
            @Override
            public void done(List<Focus> focuses, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue getting focus history", e);
                } else {
                    mFocuses = focuses;
                    displayFocusInfo();
                }
            }
        });
    }

    private void displayFocusInfo() {
        mBinding.tvStreak.setText(String.valueOf(findCurrentStreak(false)));
        mBinding.pbNumStreaks.setMax(3);
        mBinding.pbNumStreaks.setProgress(checkNumStreaks(2));
    }

    private int findCurrentStreak(boolean workweekOnly) {
        Log.i(TAG, "Checking current streak for user " + mUser.getUsername());
        final Calendar toCheck = Calendar.getInstance();
        final Calendar focusTime = Calendar.getInstance();
        int streak = 0;

        for (Focus focus : mFocuses) {
            focusTime.setTime(focus.getCreatedAt());
            if(workweekOnly) {
                while((toCheck.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (toCheck.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                    toCheck.add(Calendar.DAY_OF_YEAR, -1);
                }
            }
            if (focusTime.get(Calendar.DAY_OF_YEAR) == toCheck.get(Calendar.DAY_OF_YEAR)) {
                streak++;
                toCheck.add(Calendar.DAY_OF_YEAR, -1);
            } else if (focusTime.compareTo(toCheck) < 0) {
                return streak;
            }
        }
        return streak;
    }

    private int checkNumStreaks(int minStreakLength) {
        Log.i(TAG, "Counting number of streaks");
        final List<Integer> streaks = new ArrayList<>();
        final Calendar toCheck = Calendar.getInstance();
        final Calendar focusTime = Calendar.getInstance();
        int streak = 0;

        for (Focus focus : mFocuses) {
            focusTime.setTime(focus.getCreatedAt());
            if (focusTime.get(Calendar.DAY_OF_YEAR) == toCheck.get(Calendar.DAY_OF_YEAR)) {
                streak++;
                toCheck.add(Calendar.DAY_OF_YEAR, -1);
            } else if (focusTime.compareTo(toCheck) < 0) {
                if(streak >= minStreakLength) {
                    streaks.add(streak);
                }
                streak = 1;
                toCheck.setTime(focusTime.getTime());
                toCheck.add(Calendar.DAY_OF_YEAR, -1);
            }
        }
        if(streak >= minStreakLength) {
            streaks.add(streak);
        }

        return streaks.size();
    }
}