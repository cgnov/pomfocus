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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pomfocus.FocusUser;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentProfileBinding;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    private ParseUser mUser;
    private FragmentProfileBinding mBind;
    private View mView;
    private File mPhotoFile;

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
        mView = view;

        mBind.tvFirstName.setText(mUser.getString(FocusUser.KEY_NAME));
        mBind.tvHandle.setText(String.format("@%s", mUser.getUsername()));

        // If user has uploaded a picture, display that. Otherwise, display generic profile vector asset
        ParseFile avatar = mUser.getParseFile(FocusUser.KEY_AVATAR);
        if (avatar != null) {
            Glide.with(view).load(avatar.getUrl()).into(mBind.ivAvatar);
        } else {
            mBind.ivAvatar.setImageResource(R.drawable.profile_24);
        }

        mBind.btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        mBind.tvStreak.setText(String.valueOf(mUser.getInt(FocusUser.KEY_STREAK)));
        mBind.tvTotal.setText(String.valueOf(mUser.getLong(FocusUser.KEY_TOTAL)));
    }

    private void launchCamera() {
        // Create a File reference for future access
        String mPhotoFileName = "photo.jpg";
        mPhotoFile = getPhotoFileUri(mPhotoFileName);

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
    public File getPhotoFileUri(String fileName) {
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
                            Glide.with(mView).load(newAvatar.getUrl()).into(mBind.ivAvatar);
                        }
                    }
                });
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}