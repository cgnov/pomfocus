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
import com.example.pomfocus.R;
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
    public static final int MIN_STREAK_LENGTH = 2, MINUTES_IN_FOUR_HOURS = 240, MINUTES_IN_SIXTY_HOURS = 3600;
    public int mDaySum, mMonthSum, mFullStreak, mWorkweekStreak, mNumStreaks, mMaxStreak, mCurrentStreak = -1;
    public int mTotal, mWeekendTotal, mWorkweekTotal, mEarlyBirdTotal, mNightOwlTotal;
    public int mFourHourDays, mMaxOneDay, mSixtyHourMonths, mMaxOneMonth;
    private boolean mDataAvailable = false;
    private final Calendar mFocusTime = Calendar.getInstance(), mToCheck = Calendar.getInstance(), mMatchDate = Calendar.getInstance();

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
        for (Focus focus : focuses) {
            mFocusTime.setTime(focus.getCreatedAt());
            int length = focus.getInt(Focus.KEY_LENGTH);
            increaseLengths(length);
            checkDayMonthTotals(length);
            checkStreaks();
        }
        checkFinalDataPoints();

        if (personal) {
            confirmTotalTime();
        }
        onDataAvailable();
    }

    private void increaseLengths(int length) {
        mTotal += length;

        if (ProfileFragment.isWeekend(mFocusTime)) {
            mWeekendTotal += length;
        } else {
            mWorkweekTotal += length;
        }

        int hour = mFocusTime.get(Calendar.HOUR_OF_DAY);
        if (hour < 7 && hour >= 4) {
            mEarlyBirdTotal += length;
        } else if (hour > 22 || hour <= 2) {
            mNightOwlTotal += length;
        }
    }

    private void checkDayMonthTotals(int length) {
        if (mMatchDate.get(Calendar.DAY_OF_YEAR) != mFocusTime.get(Calendar.DAY_OF_YEAR)) {
            if (mDaySum > MINUTES_IN_FOUR_HOURS) {
                mFourHourDays++;
            }
            mMaxOneDay = Math.max(mMaxOneDay, mDaySum);
            mDaySum = 0;
        }
        if (mMatchDate.get(Calendar.MONTH) != mFocusTime.get(Calendar.MONTH)) {
            if (mMonthSum > MINUTES_IN_SIXTY_HOURS) {
                mSixtyHourMonths++;
            }
            mMaxOneMonth = Math.max(mMaxOneMonth, mMonthSum);
            mMonthSum = 0;
        }
        mMatchDate.setTime(mFocusTime.getTime());
        mDaySum += length;
        mMonthSum += length;
    }

    private void checkStreaks() {
        // Increase/save streaks
        if (mFocusTime.get(Calendar.DAY_OF_YEAR) == mToCheck.get(Calendar.DAY_OF_YEAR)) {
            mFullStreak++;
            mToCheck.add(Calendar.DAY_OF_YEAR, -1);
        } else if (mFocusTime.compareTo(mToCheck) < 0) {
            if (mCurrentStreak == -1) {
                mCurrentStreak = mFullStreak;
            }
            if (mFullStreak >= MIN_STREAK_LENGTH) {
                mNumStreaks++;
                mMaxStreak = Math.max(mMaxStreak, mFullStreak);
            }
            mFullStreak = 1;
            mToCheck.setTime(mFocusTime.getTime());
            mToCheck.add(Calendar.DAY_OF_YEAR, -1);
        }
    }

    private void checkFinalDataPoints() {
        if(mFullStreak >= MIN_STREAK_LENGTH) {
            mNumStreaks++;
        }
        if (mDaySum > MINUTES_IN_FOUR_HOURS) {
            mFourHourDays++;
        }
        if (mMonthSum > MINUTES_IN_SIXTY_HOURS) {
            mSixtyHourMonths++;
        }
        mMaxOneDay = Math.max(mMaxOneDay, mDaySum);
        mMaxOneMonth = Math.max(mMaxOneMonth, mMonthSum);
    }

    // Saves manually calculated total time if not same as automatically incremented value
    private void confirmTotalTime() {
        int savedTotal = ParseUser.getCurrentUser().getInt(FocusUser.KEY_TOTAL);
        if (savedTotal != mTotal) {
            ParseUser.getCurrentUser().put(FocusUser.KEY_TOTAL, mTotal);
            ParseUser.getCurrentUser().saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving accurate total time"));
            assert getParentFragment() != null;
            ProfileSnapshotFragment profileSnapshotFragment = (ProfileSnapshotFragment) getParentFragment()
                    .getChildFragmentManager()
                    .findFragmentById(R.id.flSnapshot);
            assert profileSnapshotFragment != null;
            profileSnapshotFragment.setTotal(mTotal);
        }
    }
}