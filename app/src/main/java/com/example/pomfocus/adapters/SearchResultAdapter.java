package com.example.pomfocus.adapters;

import android.content.Context;
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
    private static FragmentManager mFragmentManager;

    public SearchResultAdapter(Context context, List<ParseUser> results) {
        mContext = context;
        mResults = results;
    }

    @NonNull
    @Override
    public SearchResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_friend, parent, false);
        mFragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultAdapter.ViewHolder holder, int position) {
        holder.bind(mResults.get(position));
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
                    // User clicked on user in search results, slide to relevant profile view
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    ParseApp.addSlideTransition(fragmentTransaction);
                    fragmentTransaction.replace(R.id.flContainer, new ProfileFragment(result))
                            .addToBackStack(TAG)
                            .commit();
                }
            });
            mBind.tvName.setText(result.getString(FocusUser.KEY_NAME));
            ProfilePublicInfoFragment.displayHandle(mBind.tvHandle, result.getUsername());
            ProfilePublicInfoFragment.displayAvatar(mBind.ivAvatar, result.getParseFile(FocusUser.KEY_AVATAR));
        }
    }
}
