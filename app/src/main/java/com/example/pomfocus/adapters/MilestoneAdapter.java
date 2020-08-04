package com.example.pomfocus.adapters;

import android.content.Context;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomfocus.Milestone;
import com.example.pomfocus.ParseApp;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.ItemMilestoneBinding;

import java.util.ArrayList;
import java.util.List;

public class MilestoneAdapter extends RecyclerView.Adapter<MilestoneAdapter.ViewHolder> {

    private static final String TAG = "FocusUserAdapter";
    private final Context mContext;
    private final List<Milestone> mMilestones = new ArrayList<>();

    public MilestoneAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_milestone, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Milestone milestone = mMilestones.get(position);
        holder.bind(milestone);
    }

    public void add(Milestone milestone) {
        mMilestones.add(milestone);
    }

    @Override
    public int getItemCount() {
        return mMilestones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMilestoneBinding mBind;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mBind = ItemMilestoneBinding.bind(itemView);
        }

        public void bind(final Milestone milestone) {
            String title = "Level " + milestone.mLevel + " (" + milestone.mProgress + "/" + milestone.mMax + ")";
            mBind.tvTitle.setText(title);
            if (milestone.mCurrentLevel) {
                mBind.tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                mBind.tvTitle.setTextColor(ParseApp.getAttrColor(itemView.getContext(), R.attr.backgroundColor));
                setColorFilter(mBind.progressBar.getProgressDrawable(), itemView);
            }
            mBind.progressBar.setMax(milestone.mMax);
            mBind.progressBar.setProgress(milestone.mProgress);
        }

        private void setColorFilter(Drawable drawable, View itemView) {
            int color = ParseApp.getAttrColor(itemView.getContext(), R.attr.backgroundColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
            } else {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }
}
