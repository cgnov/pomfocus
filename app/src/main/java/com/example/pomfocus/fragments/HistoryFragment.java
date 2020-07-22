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
import android.widget.Toast;

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
    private static final int NUM_REQUEST = 25;
    FragmentHistoryBinding mBinding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mBinding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Focus> focuses = new ArrayList<>();
        final HistoryAdapter adapter = new HistoryAdapter(getContext(), focuses);
        final ParseQuery<Focus> query = ParseQuery.getQuery(Focus.class);
        query.addDescendingOrder(Focus.KEY_CREATED);

        // Request only focus sessions from last week
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -6);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        query.whereGreaterThanOrEqualTo(Focus.KEY_CREATED, cal.getTime());

        query.findInBackground(new FindCallback<Focus>() {
            @Override
            public void done(List<Focus> focusList, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting focus history", e);
                } else {
                    adapter.addAll(focusList);

                    // TODO: modularize/generally clean up code

                    List<BarEntry> points = new ArrayList<>();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    Calendar focusDate = Calendar.getInstance();
                    focusDate.add(Calendar.DAY_OF_MONTH, -7);
                    int[] days = new int[7];
                    for(int i = 0; i<7; i++) {
                        focusDate.add(Calendar.DAY_OF_MONTH, 1);
                        days[i] = focusDate.get(Calendar.DAY_OF_YEAR);
                    }
                    int[] focusTotals = new int[7];
                    int pos = 6;

                    for(Focus focus : focusList) {
                        focusDate.setTime(focus.getCreatedAt());
                        while(cal.get(Calendar.DAY_OF_YEAR)!=focusDate.get(Calendar.DAY_OF_YEAR)) {
                            pos--;
                            cal.add(Calendar.DAY_OF_YEAR, -1);
                        }
                        if(pos>=0) {
                            focusTotals[pos] += focus.getInt(Focus.KEY_LENGTH);
                        } else {
                            break;
                        }
                    }

                    for(int i = 0; i<days.length; i++) {
                        points.add(new BarEntry(days[i], focusTotals[i]));
                    }

                    BarDataSet set = new BarDataSet(points, "Minutes Spent Focusing");
                    set.setColor(getResources().getColor(R.color.red7));

                    BarData data = new BarData(set);

                    mBinding.bcThisWeek.setData(data);

                    final String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

                    mBinding.bcThisWeek.setFitBars(true);
                    mBinding.bcThisWeek.getXAxis().setGranularity(1);
                    mBinding.bcThisWeek.getXAxis().setGranularityEnabled(true);
                    mBinding.bcThisWeek.getXAxis().setDrawGridLines(false);
                    mBinding.bcThisWeek.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
                    mBinding.bcThisWeek.setDescription(null);
                    mBinding.bcThisWeek.getXAxis().setDrawAxisLine(false);
                    mBinding.bcThisWeek.getAxisRight().setDrawGridLines(false);
                    mBinding.bcThisWeek.getAxisLeft().setDrawGridLines(false);
                    mBinding.bcThisWeek.getAxisRight().setDrawAxisLine(false);
                    mBinding.bcThisWeek.getAxisLeft().setDrawAxisLine(false);
                    mBinding.bcThisWeek.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.DAY_OF_YEAR, (int)value);
                            return months[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH);
                        }
                    });
                    mBinding.bcThisWeek.invalidate();

                    mBinding.pbHistory.setVisibility(View.GONE);
                }
            }
        });
        mBinding.rvHistory.setAdapter(adapter);
    }
}