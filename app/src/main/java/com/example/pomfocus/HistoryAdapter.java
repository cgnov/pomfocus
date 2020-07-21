package com.example.pomfocus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomfocus.databinding.ItemFocusBinding;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private static final String TAG = "HistoryAdapter";
    private Context mContext;
    private List<Focus> mFocuses;

    public HistoryAdapter(Context context, List<Focus> focuses) {
        mContext = context;
        mFocuses = focuses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_focus, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Focus focus = mFocuses.get(position);
        holder.bind(focus);
    }

    @Override
    public int getItemCount() {
        return mFocuses.size();
    }

    // Clean all elements of the recycler (used for SwipeRefresh)
    public void clear() {
        mFocuses.clear();
        notifyDataSetChanged();
    }

    // Add list of posts (used for SwipeRefresh)
    public void addAll(List<Focus> list) {
        mFocuses.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemFocusBinding mBind;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mBind = ItemFocusBinding.bind(itemView);
        }

        public void bind(final Focus focus) {
            if(focus != null) {
                mBind.tvLength.setText(String.valueOf(focus.getInt(Focus.KEY_LENGTH)));
                mBind.tvDateTime.setText(focus.getCreatedAt().toString());
            }
        }
    }
}
