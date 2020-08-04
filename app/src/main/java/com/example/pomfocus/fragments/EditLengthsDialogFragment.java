package com.example.pomfocus.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomfocus.FocusTimer;
import com.example.pomfocus.MainActivity;
import com.example.pomfocus.ParseApp;
import com.example.pomfocus.databinding.FragmentEditLengthsDialogBinding;
import com.example.pomfocus.parse.FocusUser;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

public class EditLengthsDialogFragment extends DialogFragment {

    private static final String TAG = "EditLengthsDialogFragment";
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
                FocusTimer.MIN_PER_FOCUS = Integer.parseInt(mBinding.etPomLength.getText().toString());
                FocusTimer.MIN_PER_BREAK = Integer.parseInt(mBinding.etShortBreak.getText().toString());
                FocusTimer.MIN_PER_LONG_BREAK = Integer.parseInt(mBinding.etLongBreak.getText().toString());
                dismiss();
                if(!TimerFragment.sCurrentlyWorking) {
                    MainActivity.sTimerFragment.refresh();
                }
                ParseUser user = ParseUser.getCurrentUser();
                user.put(FocusUser.KEY_FOCUS_LENGTH, FocusTimer.MIN_PER_FOCUS);
                user.put(FocusUser.KEY_SHORT_BREAK_LENGTH, FocusTimer.MIN_PER_BREAK);
                user.put(FocusUser.KEY_LONG_BREAK_LENGTH, FocusTimer.MIN_PER_LONG_BREAK);
                user.saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving length preferences"));
            }
        });
    }
}