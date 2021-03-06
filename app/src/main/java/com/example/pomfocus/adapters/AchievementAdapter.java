package com.example.pomfocus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomfocus.R;
import com.example.pomfocus.databinding.ItemAchievementBinding;
import com.example.pomfocus.fragments.profile.MilestonesDialogFragment;
import com.example.pomfocus.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {

    private static final String TAG = "FocusUserAdapter";
    private final Context mContext;
    private final List<Achievement> mAchievements = new ArrayList<>();
    private FragmentManager mFragmentManager;

    public AchievementAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_achievement, parent, false);
        mFragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = mAchievements.get(position);
        holder.bind(achievement);
    }

    public void add(Achievement achievement) {
        mAchievements.add(achievement);
    }

    @Override
    public int getItemCount() {
        return mAchievements.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAchievementBinding mBind;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mBind = ItemAchievementBinding.bind(itemView);
        }

        public void bind(final Achievement achievement) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MilestonesDialogFragment milestonesDialogFragment = new MilestonesDialogFragment(achievement);
                    milestonesDialogFragment.show(mFragmentManager, TAG);
                }
            });
            mBind.tvTitle.setText(achievement.mTitle);
            mBind.tvDescription.setText(achievement.mDescription);
            setLevels(achievement.mProgress, achievement.mLimits, mBind.progressBar, mBind.tvLevel, mBind.tvProgress);
        }

        // Sets achievement ProgressBar and TextView values based on level limits
        private void setLevels(int progress, int[] limits, ProgressBar progressBar, TextView tvLevel, TextView tvProgress) {
            for (int level = 0; level < limits.length; level++) {
                if (progress < limits[level]) {
                    tvLevel.setText(String.valueOf(level+1));
                    progressBar.setMax(limits[level]);
                    break;
                }
            }
            progressBar.setProgress(progress);
            String fraction = "(" + progress + "/" + progressBar.getMax() + ")";
            tvProgress.setText(fraction);
        }
    }
}
