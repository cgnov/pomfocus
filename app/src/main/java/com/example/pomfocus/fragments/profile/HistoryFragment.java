package com.example.pomfocus.fragments.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomfocus.FocusTimer;
import com.example.pomfocus.fragments.profile.blocks.ProfileAchievementsFragment;
import com.example.pomfocus.parse.Focus;
import com.example.pomfocus.adapters.HistoryAdapter;
import com.example.pomfocus.ParseApp;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentHistoryBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private static final int DAYS_IN_WEEK = 7, MILLIS_IN_DAY = FocusTimer.MILLIS_PER_MINUTE * 60 * 24;
    private FragmentHistoryBinding mBinding;
    private List<BarEntry> mPoints = new ArrayList<>();
    private ProfileFragment mProfileFragment;

    public HistoryFragment(ProfileFragment profileFragment) {
        mProfileFragment = profileFragment;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mBinding.bcThisWeek.setNoDataTextColor(ParseApp.getAttrColor(getContext(), R.attr.colorPrimary));
        mBinding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Focus> savedFocuses = new ArrayList<>();
        final HistoryAdapter adapter = new HistoryAdapter(getContext(), savedFocuses);
        mBinding.rvHistory.setAdapter(adapter);

        // Find week limit
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -DAYS_IN_WEEK + 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);

        displayBarChart();
        adapter.addAll(mProfileFragment.mFocuses);
        mBinding.pbHistory.setVisibility(View.GONE);
    }

    private void displayBarChart() {
        Calendar focusDate = Calendar.getInstance();
        Calendar matchDate = Calendar.getInstance();
        Calendar oneWeekAgo = Calendar.getInstance();
        oneWeekAgo.add(Calendar.DAY_OF_YEAR, 1 - DAYS_IN_WEEK);
        int sum = 0;
        int pointsAdded = 0;
        int i = 0;
        while ((matchDate.get(Calendar.DAY_OF_YEAR) >= oneWeekAgo.get(Calendar.DAY_OF_YEAR)) && (i < mProfileFragment.mFocuses.size())) {
            focusDate.setTime(mProfileFragment.mFocuses.get(i).getCreatedAt());

            // No more focuses on given date, add info to bar chart and prep for previous day
            if (matchDate.get(Calendar.DAY_OF_YEAR) != focusDate.get(Calendar.DAY_OF_YEAR)) {
                mPoints.add(new BarEntry(getDay(matchDate), sum));
                Log.i(TAG, "adding point");
                sum = 0;
                pointsAdded++;
                matchDate.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                sum += mProfileFragment.mFocuses.get(i).getInt(Focus.KEY_LENGTH);
                i++;
            }
        }
        // Finish out week
        while (pointsAdded < 7) {

            mPoints.add(new BarEntry(getDay(matchDate), sum));
            matchDate.add(Calendar.DAY_OF_YEAR, -1);
            pointsAdded++;
            sum = 0;
        }
        addDataToBarChart();
        styleBarChart();
    }

    private long getDay(Calendar calendar) {
        return calendar.getTimeInMillis() / MILLIS_IN_DAY;
    }

    private void addDataToBarChart() {
        // Add points to chart
        BarDataSet set = new BarDataSet(mPoints, "Minutes Spent Focusing");
        set.setColor(ParseApp.getAttrColor(getContext(), R.attr.colorPrimary));
//        set.setDrawValues(false);
        set.setValueTextColor(getResources().getColor(android.R.color.white));
        BarData data = new BarData(set);
        mBinding.bcThisWeek.setData(data);
        mBinding.bcThisWeek.getXAxis().setValueFormatter(new DateValueFormatter());
    }

    private static class DateValueFormatter extends IndexAxisValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.setTimeInMillis((long)(value) * MILLIS_IN_DAY);
            return MONTHS[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.DAY_OF_MONTH);
        }
    }

    private void styleBarChart() {
        mBinding.bcThisWeek.setFitBars(true); // Automates bar width to fit screen
        mBinding.bcThisWeek.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mBinding.bcThisWeek.setDoubleTapToZoomEnabled(false);
        mBinding.bcThisWeek.setScaleXEnabled(false);

        // Style y-axes to display discrete values starting at 0 on default
        mBinding.bcThisWeek.getAxisRight().setAxisMinimum(0);
        mBinding.bcThisWeek.getAxisLeft().setAxisMinimum(0);
        mBinding.bcThisWeek.getAxisRight().setGranularity(1);
        mBinding.bcThisWeek.getAxisLeft().setGranularity(1);

        // Display dates only once no matter how much user zooms in
        mBinding.bcThisWeek.getXAxis().setGranularity(1);
        mBinding.bcThisWeek.getXAxis().setGranularityEnabled(true);

        hideBarChartLines();

        int textColor = ParseApp.getAttrColor(getContext(), R.attr.colorSecondary);
        mBinding.bcThisWeek.getXAxis().setTextColor(textColor);
        mBinding.bcThisWeek.getAxisRight().setTextColor(textColor);
        mBinding.bcThisWeek.getAxisLeft().setTextColor(textColor);
        mBinding.bcThisWeek.getLegend().setTextColor(textColor);

        mBinding.bcThisWeek.invalidate();
    }

    private void hideBarChartLines() {
        mBinding.bcThisWeek.getXAxis().setDrawGridLines(false);
        mBinding.bcThisWeek.getAxisRight().setDrawGridLines(false);
        mBinding.bcThisWeek.getAxisLeft().setDrawGridLines(false);
        mBinding.bcThisWeek.getXAxis().setDrawAxisLine(false);
        mBinding.bcThisWeek.getAxisRight().setDrawAxisLine(false);
        mBinding.bcThisWeek.getAxisLeft().setDrawAxisLine(false);
        mBinding.bcThisWeek.setDescription(null);
    }
}