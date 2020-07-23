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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private static final int DAYS_IN_WEEK = 7;
    private FragmentHistoryBinding mBinding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mBinding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Focus> savedFocuses = new ArrayList<>();
        final HistoryAdapter adapter = new HistoryAdapter(getContext(), savedFocuses);
        final ParseQuery<Focus> query = ParseQuery.getQuery(Focus.class);
        query.addDescendingOrder(Focus.KEY_CREATED);

        // Request only focus sessions from last week
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -DAYS_IN_WEEK + 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        query.whereGreaterThanOrEqualTo(Focus.KEY_CREATED, cal.getTime());

        query.findInBackground(new FindCallback<Focus>() {
            @Override
            public void done(List<Focus> thisWeekFocuses, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting focus history", e);
                } else {
                    adapter.addAll(thisWeekFocuses);
                    displayBarChart(thisWeekFocuses);
                }
            }
        });
        mBinding.rvHistory.setAdapter(adapter);
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

        // Add data to chart
        BarDataSet set = new BarDataSet(points, "Minutes Spent Focusing");
        set.setColor(getResources().getColor(R.color.red7));
        set.setDrawValues(false);
        BarData data = new BarData(set);
        mBinding.bcThisWeek.setData(data);

        // Set x-axis labels to date strings
        IndexAxisValueFormatter valueFormatter = new IndexAxisValueFormatter();
        valueFormatter.setValues(values);
        mBinding.bcThisWeek.getXAxis().setValueFormatter(valueFormatter);

        styleBarChart();

        mBinding.pbHistory.setVisibility(View.GONE);
    }

    private void styleBarChart() {
        mBinding.bcThisWeek.setFitBars(true); // Automates bar width to fit screen
        mBinding.bcThisWeek.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE); // Moves labels tighter to chart
        mBinding.bcThisWeek.setDoubleTapToZoomEnabled(false);
        mBinding.bcThisWeek.setScaleXEnabled(false);

        // Display dates only once no matter how much user zooms in
        mBinding.bcThisWeek.getXAxis().setGranularity(1);
        mBinding.bcThisWeek.getXAxis().setGranularityEnabled(true);

        // Hide lines and description
        mBinding.bcThisWeek.getXAxis().setDrawGridLines(false);
        mBinding.bcThisWeek.getAxisRight().setDrawGridLines(false);
        mBinding.bcThisWeek.getAxisLeft().setDrawGridLines(false);
        mBinding.bcThisWeek.getXAxis().setDrawAxisLine(false);
        mBinding.bcThisWeek.getAxisRight().setDrawAxisLine(false);
        mBinding.bcThisWeek.getAxisLeft().setDrawAxisLine(false);
        mBinding.bcThisWeek.setDescription(null);

        mBinding.bcThisWeek.invalidate();
    }
}