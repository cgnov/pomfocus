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
    private Context mContext;
    private List<ParseUser> mFocusers;
    private AppCompatActivity mActivity;

    public FocuserAdapter(Context context, List<ParseUser> focusers) {
        mContext = context;
        mFocusers = focusers;
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
        ParseUser focuser = mFocusers.get(position);
        focuser.put(FocusUser.KEY_RANK, position);
        holder.bind(focuser);
    }

    @Override
    public int getItemCount() {
        return mFocusers.size();
    }

    // Clean all elements of the recycler (used for SwipeRefresh)
    public void clear() {
        mFocusers.clear();
        notifyDataSetChanged();
    }

    // Add list of posts (used for SwipeRefresh)
    public void addAll(List<ParseUser> list) {
        mFocusers.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemFocuserBinding mBind;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mBind = ItemFocuserBinding.bind(itemView);
        }

        public void bind(final ParseUser focuser) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // User clicked on user in leaderboard, slide to relevant profile view
                    Fragment profileFragment = new ProfileFragment(focuser);
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
            mBind.tvName.setText(focuser.getString(FocusUser.KEY_NAME));
            mBind.tvTotal.setText(String.valueOf(focuser.getLong(FocusUser.KEY_TOTAL)));
            mBind.tvRank.setText(String.valueOf(focuser.getInt(FocusUser.KEY_RANK)+1));
            if(focuser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorAccent));
            }
        }
    }
}
