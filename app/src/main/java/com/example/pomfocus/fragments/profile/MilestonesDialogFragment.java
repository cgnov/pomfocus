package com.example.pomfocus.fragments.profile;

import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.pomfocus.Milestone;
import com.example.pomfocus.adapters.MilestoneAdapter;
import com.example.pomfocus.databinding.FragmentMilestonesDialogBinding;
import com.example.pomfocus.Achievement;

public class MilestonesDialogFragment extends DialogFragment {

    private static final String TAG = "MilestonesDialogFragmen";
    private FragmentMilestonesDialogBinding mBinding;
    private final Achievement mAchievement;

    public MilestonesDialogFragment(Achievement achievement) {
        mAchievement = achievement;
    }

    @Override
    public void onResume() {
        super.onResume();

        Point size = new Point();
        Display display = requireContext().getDisplay();
        assert display != null;
        display.getRealSize(size);
        int width = size.x;
        int height = size.y;

        Window window = requireDialog().getWindow();
        assert window != null;
        window.setLayout((int) (width * 0.7), (int) (height * 0.6));
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentMilestonesDialogBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.tvTitle.setText(mAchievement.mTitle);
        mBinding.tvDesc.setText(mAchievement.mDescription);

        MilestoneAdapter adapter = new MilestoneAdapter(getContext());
        mBinding.rvMilestones.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvMilestones.setAdapter(adapter);
        for (int i = 0; i < mAchievement.mLimits.length; i++) {
            boolean currentLevel = mAchievement.mProgress < mAchievement.mLimits[i];
            if (i > 0) {
                currentLevel &= mAchievement.mProgress >= mAchievement.mLimits[i - 1];
            }
            adapter.add(new Milestone((i + 1), mAchievement.mLimits[i], mAchievement.mProgress, currentLevel));
        }
    }
}