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

import com.example.pomfocus.MainActivity;
import com.example.pomfocus.ParseApp;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.ItemRequestBinding;
import com.example.pomfocus.fragments.profile.ProfileFragment;
import com.example.pomfocus.fragments.profile.blocks.ProfilePublicInfoFragment;
import com.example.pomfocus.parse.FocusUser;
import com.example.pomfocus.parse.FriendRequest;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private static final String TAG = "HistoryAdapter";
    private final Context mContext;
    private final List<FriendRequest> mRequests = new ArrayList<>();
    private FragmentManager mFragmentManager;

    public RequestAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_request, parent, false);
        mFragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
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

    public void remove(int position) {
        mRequests.remove(position);
        notifyItemRemoved(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRequestBinding mBind;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mBind = ItemRequestBinding.bind(itemView);
        }

        public void bind(final FriendRequest request) {
            if(request != null) {
                ParseUser sender = request.getParseUser("fromUser");
                assert sender != null;
                mBind.tvName.setText(sender.getString(FocusUser.KEY_NAME));
                ProfilePublicInfoFragment.displayAvatar(mBind.ivAvatar, sender.getParseFile(FocusUser.KEY_AVATAR));
                setUpItemClickListener(sender);
                setUpResponseClickListeners(request, sender);
            }
        }

        private void setUpItemClickListener(final ParseUser sender) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment profileFragment = new ProfileFragment(sender);
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    ParseApp.addSlideTransition(fragmentTransaction);
                    fragmentTransaction.replace(R.id.flContainer, profileFragment)
                            .addToBackStack(TAG)
                            .commit();
                }
            });
        }

        private void setUpResponseClickListeners(final FriendRequest request, final ParseUser sender) {
            mBind.btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    request.setStatus(FriendRequest.ACCEPTED);
                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error accepting friend request");
                            } else {
                                RequestAdapter.this.remove(getAdapterPosition());
                            }
                        }
                    });

                    ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS).add(FriendRequest.makePointer(sender));
                    ParseUser.getCurrentUser().saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving accepted friend request"));
                }
            });

            mBind.btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    request.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error declining/deleting friend request", e);
                            } else {
                                RequestAdapter.this.remove(getAdapterPosition());
                            }
                        }
                    });
                }
            });
        }
    }
}
