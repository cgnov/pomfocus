package com.example.pomfocus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomfocus.ParseApp;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.ItemFocusUserBinding;
import com.example.pomfocus.fragments.profile.ProfileFragment;
import com.example.pomfocus.fragments.profile.ProfilePublicInfoFragment;
import com.example.pomfocus.parse.FocusUser;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FocusUserAdapter extends RecyclerView.Adapter<FocusUserAdapter.ViewHolder> {

    private static final String TAG = "FocusUserAdapter";
    private final Context mContext;
    private final List<ParseUser> mFocusUsers = new ArrayList<>();
    private AppCompatActivity mActivity;

    public FocusUserAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_focus_user, parent, false);
        mActivity = (AppCompatActivity) view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser focusUser;
        focusUser = mFocusUsers.get(position);
        focusUser.put(FocusUser.KEY_RANK, position);
        holder.bind(focusUser);
    }

    @Override
    public int getItemCount() {
        return mFocusUsers.size();
    }

    // Clean all elements of the recycler (used for SwipeRefresh)
    public void clear() {
        mFocusUsers.clear();
        notifyDataSetChanged();
    }

    // Add list of posts (used for SwipeRefresh)
    public void addAll(List<ParseUser> list) {
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
                    Fragment profileFragment;
                    profileFragment = new ProfileFragment(focusUser);
                    FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                    ParseApp.addSlideTransition(fragmentTransaction);
                    fragmentTransaction.replace(R.id.flContainer, profileFragment)
                            .addToBackStack(TAG)
                            .commit();
                }
            });
            mBind.tvName.setText(focusUser.getString(FocusUser.KEY_NAME));
            ProfilePublicInfoFragment.displayAvatar(mBind.ivAvatar, focusUser.getParseFile(FocusUser.KEY_AVATAR));
            mBind.tvTotal.setText(String.valueOf(focusUser.getLong(FocusUser.KEY_TOTAL)));
            mBind.tvRank.setText(String.valueOf(focusUser.getInt(FocusUser.KEY_RANK)+1));
            if(focusUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                itemView.setBackgroundColor(ParseApp.getAttrColor(mContext, R.attr.backgroundColor));
            }
        }
    }
}
