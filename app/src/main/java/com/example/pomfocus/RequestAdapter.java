package com.example.pomfocus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomfocus.databinding.ItemRequestBinding;
import com.example.pomfocus.fragments.ProfileFragment;
import com.parse.ParseUser;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private static final String TAG = "HistoryAdapter";
    private final Context mContext;
    private final List<FriendRequest> mRequests;

    public RequestAdapter(Context context, List<FriendRequest> requests) {
        mContext = context;
        mRequests = requests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mRequests.get(position));
    }

    @Override
    public int getItemCount() {
        return mRequests.size();
    }

    public void addAll(List<FriendRequest> requests) {
        mRequests.addAll(requests);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRequestBinding mBind;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mBind = ItemRequestBinding.bind(itemView);
        }

        public void bind(final FriendRequest request) {
            if(request != null) {
                ParseUser sender = request.getParseUser("fromUser");
                assert sender != null;
                mBind.tvName.setText(sender.getUsername());
                ProfileFragment.displayAvatar(mBind.ivAvatar, sender.getParseFile(FocusUser.KEY_AVATAR));
            }
        }
    }
}
