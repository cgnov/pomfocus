package com.example.pomfocus.fragments.profile.blocks;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomfocus.ParseApp;
import com.example.pomfocus.adapters.AchievementAdapter;
import com.example.pomfocus.databinding.FragmentProfileAchievementsBinding;
import com.example.pomfocus.fragments.profile.ProfileFragment;
import com.example.pomfocus.Achievement;
import com.example.pomfocus.parse.Focus;
import com.example.pomfocus.parse.FocusUser;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;


public class ProfileAchievementsFragment extends Fragment {

    private static final String TAG = "ProfileAchievementsFrag";
    private FragmentProfileAchievementsBinding mBinding;
    private AchievementAdapter mAdapter;
    private static final int[] STREAK_LIMITS = {3, 7, 14, 30, 75, 150, 365};
    private static final int[] NUM_STREAK_LIMITS = {1, 2, 3, 5, 7, 10, 15};
    private static final int[] MINUTE_LIMITS = {25, 50, 100, 250, 500, 750, 1000};
    private static final int MIN_STREAK_LENGTH = 2;
    public int mWorkweekStreak = 0;
    public int mTotal = 0, mWeekendTotal = 0, mWorkweekTotal = 0, mEarlyBirdTotal = 0, mNightOwlTotal = 0;
    public int mFourHourDays = 0, mMaxOneDay = 0, mSixtyHourMonths = 0, mMaxOneMonth = 0;
    public int mNumStreaks = 0, mMaxStreak;
    private boolean mDataAvailable = false;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentProfileAchievementsBinding.inflate(getLayoutInflater(), container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(TAG, "onViewCreated, mDataAvailable: " + mDataAvailable);

        mAdapter = new AchievementAdapter(getContext());
        mBinding.rvAchievements.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mBinding.rvAchievements.setLayoutManager(layoutManager);
        mBinding.rvAchievements.addItemDecoration(new DividerItemDecoration(mBinding.rvAchievements.getContext(), layoutManager.getOrientation()));

        if (mDataAvailable) {
            onDataAvailable();
        }
    }

    public void onDataAvailable() {
        Log.i(TAG, "onDataAvailable, mDataAvailable: " + mDataAvailable);
        if (mAdapter != null) {
            mAdapter.add(new Achievement("Weekend Warrior", "Minutes focused on weekends", mWeekendTotal, MINUTE_LIMITS));
            mAdapter.add(new Achievement("Doesn't Give Up", "Streaks started", mNumStreaks, NUM_STREAK_LIMITS));
            mAdapter.add(new Achievement("Workweek Streak", "Streak excluding weekends", mWorkweekStreak, STREAK_LIMITS));
            mAdapter.add(new Achievement("Early Bird", "Minutes focused between 4am and 7am", mEarlyBirdTotal, MINUTE_LIMITS));
            mAdapter.add(new Achievement("Night Owl", "Minutes focused between 10pm and 3am", mNightOwlTotal, MINUTE_LIMITS));
            mAdapter.add(new Achievement("Consistent", "Longest streak length", mMaxStreak, STREAK_LIMITS));
            mAdapter.add(new Achievement("Doing the Most", "Days with over 4 hours focused", mFourHourDays, NUM_STREAK_LIMITS));
            mAdapter.add(new Achievement("Monthly Max", "Most minutes focused in one month", mMaxOneMonth, MINUTE_LIMITS));
            mAdapter.add(new Achievement("Daily Max", "Most minutes focused in one day", mMaxOneDay, MINUTE_LIMITS));
            mAdapter.add(new Achievement("Intense Months", "Months with over sixty hours focused", mSixtyHourMonths, NUM_STREAK_LIMITS));

            mAdapter.notifyDataSetChanged();
            mBinding.pbAchievements.setVisibility(View.GONE);
        } else {
            mDataAvailable = true;
        }
    }

    public void countTotals(List<Focus> focuses, boolean personal) {
        Log.i(TAG, "countTotals");
        final Calendar focusTime = Calendar.getInstance();
        final Calendar toCheck = Calendar.getInstance();
        final Calendar matchDate = Calendar.getInstance();

        int daySum = 0;
        int monthSum = 0;
        int fullStreak = 0;

        for (Focus focus : focuses) {
            focusTime.setTime(focus.getCreatedAt());
            int length = focus.getInt(Focus.KEY_LENGTH);
            increaseLengths(length, focusTime);

            if(matchDate.get(Calendar.DAY_OF_YEAR) != focusTime.get(Calendar.DAY_OF_YEAR)) {
                if (daySum > 240) {
                    mFourHourDays++;
                }
                mMaxOneDay = Math.max(mMaxOneDay, daySum);
                daySum = 0;
            }
            if(matchDate.get(Calendar.MONTH) != focusTime.get(Calendar.MONTH)) {
                if (monthSum > 3600) {
                    mSixtyHourMonths++;
                }
                mMaxOneMonth = Math.max(mMaxOneMonth, monthSum);
                monthSum = 0;
            }
            matchDate.setTime(focusTime.getTime());
            daySum += length;
            monthSum += length;

            // Increase/save streaks
            if (focusTime.get(Calendar.DAY_OF_YEAR) == toCheck.get(Calendar.DAY_OF_YEAR)) {
                fullStreak++;
                toCheck.add(Calendar.DAY_OF_YEAR, -1);
            } else if (focusTime.compareTo(toCheck) < 0) {
                if(fullStreak >= MIN_STREAK_LENGTH) {
                    mNumStreaks++;
                    mMaxStreak = Math.max(mMaxStreak, fullStreak);
                }
                fullStreak = 1;
                toCheck.setTime(focusTime.getTime());
                toCheck.add(Calendar.DAY_OF_YEAR, -1);
            }
        }
        if(fullStreak >= MIN_STREAK_LENGTH) {
            mNumStreaks++;
        }
        if (daySum > 240) {
            mFourHourDays++;
        }
        if (monthSum > 3600) {
            mSixtyHourMonths++;
        }
        mMaxOneDay = Math.max(mMaxOneDay, daySum);
        mMaxOneMonth = Math.max(mMaxOneMonth, monthSum);

        if (personal) {
            confirmTotalTime();
        }
        onDataAvailable();
    }

    private void increaseLengths(int length, Calendar focusTime) {
        mTotal += length;

        if (ProfileFragment.isWeekend(focusTime)) {
            mWeekendTotal += length;
        } else {
            mWorkweekTotal += length;
        }

        int hour = focusTime.get(Calendar.HOUR_OF_DAY);
        if (hour < 7 && hour >= 4) {
            mEarlyBirdTotal += length;
        } else if (hour > 22 || hour <= 2) {
            mNightOwlTotal += length;
        }
    }

    // Saves manually calculated total time if not same as automatically incremented value
    private void confirmTotalTime() {
        int savedTotal = ParseUser.getCurrentUser().getInt(FocusUser.KEY_TOTAL);
        if (savedTotal != mTotal) {
            ParseUser.getCurrentUser().put(FocusUser.KEY_TOTAL, mTotal);
            ParseUser.getCurrentUser().saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving accurate total time"));
        }
    }
}