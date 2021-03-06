package com.example.pomfocus.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pomfocus.FocusTimer;
import com.example.pomfocus.MainActivity;
import com.example.pomfocus.ParseApp;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentEditLengthsDialogBinding;
import com.example.pomfocus.fragments.profile.SettingsFragment;
import com.example.pomfocus.parse.FocusUser;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

public class EditLengthsDialogFragment extends DialogFragment {

    private static final String TAG = "EditLengthsFragment";
    private FragmentEditLengthsDialogBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up view binding
        mBinding = FragmentEditLengthsDialogBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.etPomLength.setText(String.valueOf(FocusTimer.MIN_PER_FOCUS));
        mBinding.etShortBreak.setText(String.valueOf(FocusTimer.MIN_PER_BREAK));
        mBinding.etLongBreak.setText(String.valueOf(FocusTimer.MIN_PER_LONG_BREAK));

        mBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newFocusLength = Integer.parseInt(mBinding.etPomLength.getText().toString());
                int newShortBreak = Integer.parseInt(mBinding.etShortBreak.getText().toString());
                int newLongBreak = Integer.parseInt(mBinding.etLongBreak.getText().toString());
                if ((newFocusLength == 0) || (newShortBreak == 0) || (newLongBreak == 0)) {
                    Toast.makeText(getContext(), "Timer lengths cannot be set to 0", Toast.LENGTH_SHORT).show();
                } else {
                    saveNewLengths(newFocusLength, newShortBreak, newLongBreak);
                    dismiss();
                }
            }
        });
    }

    private void saveNewLengths(int focus, int shortBreak, int longBreak) {
        FocusTimer.MIN_PER_FOCUS = focus;
        FocusTimer.MIN_PER_BREAK = shortBreak;
        FocusTimer.MIN_PER_LONG_BREAK = longBreak;
        if(!TimerFragment.sCurrentlyWorking) {
            MainActivity.sTimerFragment.refresh();
        }
        ParseUser user = ParseUser.getCurrentUser();
        user.put(FocusUser.KEY_FOCUS_LENGTH, FocusTimer.MIN_PER_FOCUS);
        user.put(FocusUser.KEY_SHORT_BREAK_LENGTH, FocusTimer.MIN_PER_BREAK);
        user.put(FocusUser.KEY_LONG_BREAK_LENGTH, FocusTimer.MIN_PER_LONG_BREAK);
        user.saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving length preferences"));
        Fragment fragment = ((AppCompatActivity)requireContext()).getSupportFragmentManager().findFragmentById(R.id.flContainer);
        assert fragment != null;
        fragment.onActivityResult(SettingsFragment.UPDATE_LENGTHS_REQUEST_CODE, 1, null);
    }
}