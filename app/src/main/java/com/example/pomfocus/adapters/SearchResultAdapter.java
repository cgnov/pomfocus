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
import com.example.pomfocus.databinding.ItemFriendBinding;
import com.example.pomfocus.fragments.profile.ProfileFragment;
import com.example.pomfocus.fragments.profile.blocks.ProfilePublicInfoFragment;
import com.example.pomfocus.parse.FocusUser;
import com.parse.ParseUser;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private static final String TAG = "FriendAdapter";
    private final Context mContext;
    private final List<ParseUser> mResults;
    private static AppCompatActivity mActivity;

    public SearchResultAdapter(Context context, List<ParseUser> results) {
        mContext = context;
        mResults = results;
    }

    @NonNull
    @Override
    public SearchResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_friend, parent, false);
        mActivity = (AppCompatActivity) view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultAdapter.ViewHolder holder, int position) {
        ParseUser result = mResults.get(position);
        holder.bind(result);
    }

    public void addAll(List<ParseUser> results) {
        mResults.clear();
        mResults.addAll(results);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFriendBinding mBind;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mBind = ItemFriendBinding.bind(itemView);
        }

        public void bind(final ParseUser result) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // User clicked on user in leaderboard, slide to relevant profile view
                    Fragment profileFragment = new ProfileFragment(result);
                    FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                    ParseApp.addSlideTransition(fragmentTransaction);
                    fragmentTransaction.replace(R.id.flContainer, profileFragment)
                            .addToBackStack(TAG)
                            .commit();
                }
            });
            mBind.tvName.setText(result.getString(FocusUser.KEY_NAME));
            mBind.tvHandle.setText(String.format("@%s", result.getUsername()));
            ProfilePublicInfoFragment.displayAvatar(mBind.ivAvatar, result.getParseFile(FocusUser.KEY_AVATAR));
        }
    }
}
