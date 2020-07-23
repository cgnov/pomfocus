package com.example.pomfocus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomfocus.databinding.ItemFocusBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private static final String TAG = "HistoryAdapter";
    private final Context mContext;
    private final List<Focus> mFocuses;

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

    public void addAll(List<Focus> focuses) {
        mFocuses.addAll(focuses);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFocusBinding mBind;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mBind = ItemFocusBinding.bind(itemView);
        }

        public void bind(final Focus focus) {
            if(focus != null) {
                mBind.tvLength.setText(String.valueOf(focus.getInt(Focus.KEY_LENGTH)));
                mBind.tvDateTime.setText(getSimpleDateTime(focus.getCreatedAt().toString()));
            }
        }
    }

    // Takes datetime string like "Mon Apr 01 21:16:23 +0000 2014" and returns  "Thu April 7 9:16 PM"
    public static String getSimpleDateTime(String oldFormatDate) {
        String oldFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        String newFormat = "EEE MMMM dd h:mm aa";
        SimpleDateFormat sf = new SimpleDateFormat(oldFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String simpleDate = "";
        try {
            Date date = sf.parse(oldFormatDate);
            sf.applyPattern(newFormat);
            assert date != null;
            simpleDate = sf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return simpleDate;
    }
}
