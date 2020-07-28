package com.example.pomfocus.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomfocus.Focus;
import com.example.pomfocus.HistoryAdapter;
import com.example.pomfocus.ParseApplication;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentHistoryBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private static final int DAYS_IN_WEEK = 7;
    public static final int NUM_REQUEST = 25;
    private FragmentHistoryBinding mBinding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mBinding.bcThisWeek.setNoDataTextColor(ParseApplication.getAttrColor(getContext(), R.attr.colorPrimary));
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

        requestThisWeek(cal.getTime());
        requestAll(adapter);
    }

    private void requestThisWeek(Date limit) {
        final ParseQuery<Focus> thisWeekQuery = ParseQuery.getQuery(Focus.class);
        thisWeekQuery.addDescendingOrder(Focus.KEY_CREATED_AT);
        thisWeekQuery.whereEqualTo(Focus.KEY_CREATOR, ParseUser.getCurrentUser());
        thisWeekQuery.whereGreaterThanOrEqualTo(Focus.KEY_CREATED_AT, limit);
        thisWeekQuery.findInBackground(new FindCallback<Focus>() {
            @Override
            public void done(List<Focus> thisWeekFocuses, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting this week focus history", e);
                } else {
                    displayBarChart(thisWeekFocuses);
                }
            }
        });
    }

    private void requestAll(final HistoryAdapter adapter) {
        final ParseQuery<Focus> olderQuery = ParseQuery.getQuery(Focus.class);
        olderQuery.addDescendingOrder(Focus.KEY_CREATED_AT);
        olderQuery.whereEqualTo(Focus.KEY_CREATOR, ParseUser.getCurrentUser());
        olderQuery.findInBackground(new FindCallback<Focus>() {
            @Override
            public void done(List<Focus> olderFocuses, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Issue with getting older focus history", e);
                } else {
                    adapter.addAll(olderFocuses);
                    mBinding.pbHistory.setVisibility(View.GONE);
                }
            }
        });
    }

    private void displayBarChart(List<Focus> thisWeekFocuses) {
        Calendar focusDate = Calendar.getInstance();
        Calendar matchDate = Calendar.getInstance();
        matchDate.setTime(new Date());

        List<BarEntry> points = new ArrayList<>();
        int sum = 0;
        String[] values = new String[DAYS_IN_WEEK];
        int pos = values.length -1;
        for(int i = 0; i<thisWeekFocuses.size(); i++) {
            focusDate.setTime(thisWeekFocuses.get(i).getCreatedAt());

            // No more focuses on given date, add info to bar chart and prep for previous day
            while(matchDate.get(Calendar.DAY_OF_YEAR) != focusDate.get(Calendar.DAY_OF_YEAR)) {
                points.add(new BarEntry(pos, sum));
                values[pos] = MONTHS[matchDate.get(Calendar.MONTH)] + " " + matchDate.get(Calendar.DAY_OF_MONTH);
                pos--;
                sum = 0;
                matchDate.add(Calendar.DAY_OF_YEAR, -1);
            }

            sum += thisWeekFocuses.get(i).getInt(Focus.KEY_LENGTH);
        }
        // Add info involving oldest focus
        points.add(new BarEntry(pos, sum));
        values[pos] = MONTHS[matchDate.get(Calendar.MONTH)] + " " + matchDate.get(Calendar.DAY_OF_MONTH);

        addDataToBarChart(points, values);
        styleBarChart();
    }

    private void addDataToBarChart(List<BarEntry> points, String[] values) {
        // Add points to chart
        BarDataSet set = new BarDataSet(points, "Minutes Spent Focusing");
        set.setColor(ParseApplication.getAttrColor(getContext(), R.attr.colorPrimary));
        set.setDrawValues(false);
        BarData data = new BarData(set);
        mBinding.bcThisWeek.setData(data);

        // Set x-axis labels to date strings
        IndexAxisValueFormatter valueFormatter = new IndexAxisValueFormatter();
        valueFormatter.setValues(values);
        mBinding.bcThisWeek.getXAxis().setValueFormatter(valueFormatter);
    }

    private void styleBarChart() {
        mBinding.bcThisWeek.setFitBars(true); // Automates bar width to fit screen
        mBinding.bcThisWeek.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE); // Moves labels tighter to chart
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

        int textColor = ParseApplication.getAttrColor(getContext(), R.attr.backgroundColor);
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