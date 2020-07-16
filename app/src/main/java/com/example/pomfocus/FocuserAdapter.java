package com.example.pomfocus;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomfocus.databinding.ItemFocuserBinding;
import com.parse.ParseUser;

import java.util.List;

public class FocuserAdapter extends RecyclerView.Adapter<FocuserAdapter.ViewHolder> {

    private static final String TAG = "FocuserAdapter";
    private Context mContext;
    private List<ParseUser> mFocusers;

    public FocuserAdapter(Context context, List<ParseUser> focusers) {
        mContext = context;
        mFocusers = focusers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_focuser, parent, false);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mBind = ItemFocuserBinding.bind(itemView);
        }

        public void bind(final ParseUser focuser) {
            mBind.tvName.setText(focuser.getString(FocusUser.KEY_NAME));
            mBind.tvTotal.setText(String.valueOf(focuser.getLong(FocusUser.KEY_TOTAL)));
            mBind.tvRank.setText(String.valueOf(focuser.getInt(FocusUser.KEY_RANK)+1));
            if(focuser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorAccent));
            }
        }

    }
}
