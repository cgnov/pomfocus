package com.example.pomfocus.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomfocus.ParseApp;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.ItemFocusUserBinding;
import com.example.pomfocus.fragments.profile.ProfileFragment;
import com.example.pomfocus.fragments.profile.blocks.ProfilePublicInfoFragment;
import com.example.pomfocus.parse.FocusUser;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FocusUserAdapter extends RecyclerView.Adapter<FocusUserAdapter.ViewHolder> {

    private static final String TAG = "FocusUserAdapter";
    private final Context mContext;
    private final List<ParseUser> mFocusUsers = new ArrayList<>();
    private FragmentManager mFragmentManager;
    private static final int TIED_DEFAULT = 1, RANK_DEFAULT = 0;
    private int mNextRank = RANK_DEFAULT, mLastMax = Integer.MAX_VALUE, mTied = TIED_DEFAULT;

    public FocusUserAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_focus_user, parent, false);
        mFragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mFocusUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mFocusUsers.size();
    }

    // Clean all elements of the recycler (used for SwipeRefresh)
    public void clear() {
        mNextRank = RANK_DEFAULT;
        mTied = TIED_DEFAULT;
        mLastMax = Integer.MAX_VALUE;
        mFocusUsers.clear();
        notifyDataSetChanged();
    }

    // Add list of posts (used for SwipeRefresh)
    public void addAll(List<ParseUser> list) {
        for (ParseUser user : list) {
            if (user.getInt(FocusUser.KEY_TOTAL) < mLastMax) {
                mNextRank += mTied;
                mTied = TIED_DEFAULT;
                mLastMax = user.getInt(FocusUser.KEY_TOTAL);
            } else {
                mTied++;
            }
            user.put(FocusUser.KEY_RANK, mNextRank);
        }
        mFocusUsers.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFocusUserBinding mBind;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mBind = ItemFocusUserBinding.bind(itemView);
        }

        public void bind(final ParseUser focusUser) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // User clicked on user in leaderboard, slide to relevant profile view
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    ParseApp.addSlideTransition(fragmentTransaction);
                    fragmentTransaction.replace(R.id.flContainer, new ProfileFragment(focusUser))
                            .addToBackStack(TAG)
                            .commit();
                }
            });
            mBind.tvRank.setText(String.valueOf(focusUser.getInt(FocusUser.KEY_RANK)));
            ProfilePublicInfoFragment.displayAvatar(mBind.ivAvatar, focusUser.getParseFile(FocusUser.KEY_AVATAR));
            mBind.tvName.setText(focusUser.getString(FocusUser.KEY_NAME));
            mBind.tvTotal.setText(String.format(Locale.getDefault(), "%d min", focusUser.getLong(FocusUser.KEY_TOTAL)));
            if (focusUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                itemView.setBackgroundColor(ParseApp.getAttrColor(mContext, R.attr.backgroundColor));
            }
        }
    }
}
