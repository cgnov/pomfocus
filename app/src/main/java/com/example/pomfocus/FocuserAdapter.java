package com.example.pomfocus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomfocus.databinding.ItemFocuserBinding;
import com.example.pomfocus.fragments.ProfileFragment;
import com.parse.ParseUser;

import java.util.List;

public class FocuserAdapter extends RecyclerView.Adapter<FocuserAdapter.ViewHolder> {

    private static final String TAG = "FocuserAdapter";
    private final Context mContext;
    private final List<ParseUser> mFocusUsers;
    private AppCompatActivity mActivity;

    public FocuserAdapter(Context context, List<ParseUser> focusers) {
        mContext = context;
        mFocusUsers = focusers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_focuser, parent, false);
        mActivity = (AppCompatActivity) view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser focusUser = mFocusUsers.get(position);
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
        private final ItemFocuserBinding mBind;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mBind = ItemFocuserBinding.bind(itemView);
        }

        public void bind(final ParseUser focusUser) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // User clicked on user in leaderboard, slide to relevant profile view
                    Fragment profileFragment = new ProfileFragment(focusUser);
                    FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_left_enter,
                            R.anim.fragment_slide_left_exit,
                            R.anim.fragment_slide_right_enter,
                            R.anim.fragment_slide_right_exit);
                    fragmentTransaction.replace(R.id.flContainer, profileFragment)
                            .addToBackStack(TAG)
                            .commit();
                }
            });
            mBind.tvName.setText(focusUser.getString(FocusUser.KEY_NAME));
            mBind.tvTotal.setText(String.valueOf(focusUser.getLong(FocusUser.KEY_TOTAL)));
            mBind.tvRank.setText(String.valueOf(focusUser.getInt(FocusUser.KEY_RANK)+1));
            if(focusUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                itemView.setBackgroundColor(itemView.getResources().getColor(R.color.red3));
            }
        }
    }
}
