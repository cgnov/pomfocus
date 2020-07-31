package com.example.pomfocus.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.pomfocus.Achievement;
import com.example.pomfocus.AchievementAdapter;
import com.example.pomfocus.Focus;
import com.example.pomfocus.FocusUser;
import com.example.pomfocus.FriendRequest;
import com.example.pomfocus.ParseApp;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentProfileBinding;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private final ParseUser mUser;
    private FragmentProfileBinding mBinding;
    private List<Focus> mFocuses;
    private static final int[] STREAK_LIMITS = {3, 7, 14, 30, 75, 150, 365};
    private static final int[] NUM_STREAK_LIMITS = {1, 2, 3, 5, 7, 10, 15};
    private static final int[] MINUTE_LIMITS = {25, 50, 100, 250, 500, 750, 1000};
    private static final int MIN_STREAK_LENGTH = 2;
    private int mFullStreak = 0, mWorkweekStreak = 0;
    private int mTotal = 0, mWeekendTotal = 0, mWorkweekTotal = 0, mEarlyBirdTotal = 0, mNightOwlTotal = 0;
    private int mFourHourDays = 0, mMaxOneDay = 0, mSixtyHourMonths = 0, mMaxOneMonth = 0;
    private int mNumStreaks = 0, mMaxStreak;
    private AchievementAdapter mAdapter;
    private boolean mFriends = true, mSent = true, mReceived = true, mConfirmed = false;

    public ProfileFragment(ParseUser user) {
        this.mUser = user;
    }

    public ProfileFragment(ParseUser user, boolean friend) {
        this.mUser = user;
        mConfirmed = friend;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Implement view binding
        mBinding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpRecyclerView();
        displayProfileInfo();

        if(mFocuses == null) {
            findFullFocusHistory();
        } else {
            displayFocusInfo();
        }
    }

    private void displayProfileInfo() {
        mBinding.tvName.setText(mUser.getString(FocusUser.KEY_NAME));
        mBinding.tvHandle.setText(String.format("@%s", mUser.getUsername()));
        mBinding.tvTotal.setText(String.valueOf(mUser.getLong(FocusUser.KEY_TOTAL)));

        // If user has uploaded a picture, display that. Otherwise, display generic profile vector asset
        ParseFile avatar = mUser.getParseFile(FocusUser.KEY_AVATAR);
        displayAvatar(mBinding.ivAvatar, avatar);

        // Set up or hide personal buttons
        if(mUser.equals(ParseUser.getCurrentUser())) {
            setUpClickListeners();
            queryNewFriends();
        } else {
            hideButtons();
            checkFriendStatus();
        }
    }

    private void queryNewFriends() {
        ParseQuery<FriendRequest> query = ParseQuery.getQuery(FriendRequest.class);
        query.whereEqualTo(FriendRequest.KEY_FROM, FriendRequest.makePointer(ParseUser.getCurrentUser()));
        query.whereEqualTo(FriendRequest.KEY_ACCEPTED, true);
        query.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> acceptedRequests, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error retrieving new friends", e);
                } else {
                    if ((acceptedRequests != null) && (acceptedRequests.size() != 0)) {
                        processNewFriends(acceptedRequests);
                    }
                }
            }
        });
    }

    private void processNewFriends(List<FriendRequest> acceptedRequests) {
        List<ParseUser> newFriends = new ArrayList<>();
        for (FriendRequest acceptedRequest : acceptedRequests) {
            ParseUser newFriend = acceptedRequest.getParseUser(FriendRequest.KEY_TO);
            assert newFriend != null;
            newFriends.add(FriendRequest.makePointer(newFriend));
            acceptedRequest.deleteInBackground(new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error deleting accepted friend request");
                    }
                }
            });
        }
        for (ParseUser friendPointer : newFriends) {
            ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS).add(friendPointer);
        }
        ParseUser.getCurrentUser().saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving new friends"));
    }

    private void setUpRecyclerView() {
        List<Achievement> achievements = new ArrayList<>();
        mAdapter = new AchievementAdapter(getContext(), achievements);
        mBinding.rvAchievements.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mBinding.rvAchievements.setLayoutManager(layoutManager);
        mBinding.rvAchievements.addItemDecoration(new DividerItemDecoration(mBinding.rvAchievements.getContext(), layoutManager.getOrientation()));
    }

    private void hideButtons() {
        mBinding.btnSettings.setVisibility(View.GONE);
        mBinding.btnSeeHistory.setVisibility(View.GONE);
        mBinding.btnFriends.setVisibility(View.GONE);
    }

    private void checkFriendStatus() {
        if (!mUser.getUsername().equals(ParseUser.getCurrentUser().getUsername()) && !mConfirmed) {
            checkAlreadyFriends();
            checkPendingSentRequest();
            checkPendingReceivedRequest();
        }
    }

    private void checkAlreadyFriends() {
        ParseQuery<ParseObject> friendsQuery = ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS).getQuery();
        friendsQuery.whereEqualTo(FocusUser.KEY_HANDLE, mUser.getUsername());
        friendsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error checking if friend", e);
                } else {
                    if (objects.size() == 0) {
                        mFriends = false;
                        setUpSendRequestButton();
                    }
                }
            }
        });
    }

    private void checkPendingSentRequest() {
        ParseQuery<FriendRequest> sentRequestQuery = ParseQuery.getQuery(FriendRequest.class);
        sentRequestQuery.whereEqualTo(FriendRequest.KEY_FROM, FriendRequest.makePointer(ParseUser.getCurrentUser()));
        sentRequestQuery.whereEqualTo(FriendRequest.KEY_TO, FriendRequest.makePointer(mUser));
        sentRequestQuery.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> requests, ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Error checking sent friend request");
                } else {
                    if (requests.size() == 0) {
                        mSent = false;
                        setUpSendRequestButton();
                    } else {
                        FriendRequest request = requests.get(0);
                        if (request.getAccepted()) {
                            processAcceptedRequest(request);
                        } else {
                            setUpCancelRequestButton(request);
                        }
                    }
                }
            }
        });
    }

    private void setUpCancelRequestButton(final FriendRequest request) {
        mBinding.btnFriendRequest.setText(getString(R.string.cancel_request));
        mBinding.btnFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.i(TAG, "Error cancelling friend request");
                        } else {
                            mSent = false;
                            setUpSendRequestButton();
                        }
                    }
                });
            }
        });
        mBinding.btnFriendRequest.setVisibility(View.VISIBLE);
    }

    private void processAcceptedRequest(FriendRequest request) {
        ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS).add(FriendRequest.makePointer(mUser));
        request.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Error deleting friend request");
                }
            }
        });
    }

    private void checkPendingReceivedRequest() {
        ParseQuery<FriendRequest> receivedRequestQuery = ParseQuery.getQuery(FriendRequest.class);
        receivedRequestQuery.whereEqualTo(FriendRequest.KEY_TO, FriendRequest.makePointer(ParseUser.getCurrentUser()));
        receivedRequestQuery.whereEqualTo(FriendRequest.KEY_FROM, FriendRequest.makePointer(mUser));
        receivedRequestQuery.whereEqualTo(FriendRequest.KEY_ACCEPTED, false);
        receivedRequestQuery.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> requests, ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Error checking received friend request");
                } else {
                    if (requests.size() == 0) {
                        mReceived = false;
                        setUpSendRequestButton();
                    } else {
                        // Received request, can redirect to Settings to respond
                        mBinding.btnFriendRequest.setText(getString(R.string.respond_to_request));
                        mBinding.btnFriendRequest.setVisibility(View.VISIBLE);
                        mBinding.btnFriendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                assert getFragmentManager() != null;
                                getFragmentManager().beginTransaction().replace(R.id.flContainer, new SettingsFragment()).commit();
                            }
                        });
                    }
                }
            }
        });
    }

    private void setUpSendRequestButton() {
        if (!mFriends && !mSent && !mReceived) {
            mBinding.btnFriendRequest.setText(getString(R.string.send_friend_request));
            mBinding.btnFriendRequest.setVisibility(View.VISIBLE);
            mBinding.btnFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "Saving friend request from " + ParseUser.getCurrentUser().getUsername() + " to " + mUser.getUsername());
                    FriendRequest friendRequest = new FriendRequest();
                    friendRequest.setFrom(ParseUser.getCurrentUser());
                    friendRequest.setTo(mUser);
                    friendRequest.saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving friend request"));
                    setUpCancelRequestButton(friendRequest);
                }
            });
        }
    }

    private void setUpClickListeners() {
        mBinding.btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getFragmentManager() != null;
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flContainer, new SettingsFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });

        mBinding.btnSeeHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getFragmentManager() != null;
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flContainer, new HistoryFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });

        mBinding.btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getFragmentManager() != null;
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flContainer, new FriendsFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });
    }

    public static void displayAvatar(ImageView view, ParseFile avatar) {
        if (avatar != null) {
            Glide.with(view)
                    .load(avatar.getUrl())
                    .circleCrop()
                    .placeholder(R.color.grey2)
                    .into(view);
        } else {
            view.setImageResource(R.drawable.profile_24);
        }
    }

    private void findFullFocusHistory() {
        Log.i(TAG, "Querying full focus history from Parse for user " + mUser.getUsername());
        ParseQuery<Focus> fullHistoryQuery = ParseQuery.getQuery(Focus.class);
        fullHistoryQuery.whereEqualTo(Focus.KEY_CREATOR, mUser);
        fullHistoryQuery.addDescendingOrder(Focus.KEY_CREATED_AT);

        fullHistoryQuery.findInBackground(new FindCallback<Focus>() {
            @Override
            public void done(List<Focus> focuses, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue getting focus history", e);
                } else {
                    mFocuses = focuses;
                    countFocusInfo();
                }
            }
        });
    }

    private void countFocusInfo() {
        countCurrentStreaks();
        countTotals();
        confirmTotalTime();
        displayFocusInfo();
    }

    private void displayFocusInfo() {
        mBinding.tvStreak.setText(String.valueOf(mFullStreak));

        mAdapter.add(new Achievement("Weekend Warrior", "Minutes focused on weekends", mWeekendTotal, MINUTE_LIMITS));
        mAdapter.add(new Achievement("Doesn't Give Up", "Streaks started", mNumStreaks, NUM_STREAK_LIMITS));
        mAdapter.add(new Achievement("Workweek Streak", "Streak excluding weekends", mWorkweekStreak, STREAK_LIMITS));
        mAdapter.add(new Achievement("Early Bird", "Minutes focused between 4am and 7am", mEarlyBirdTotal, MINUTE_LIMITS));
        mAdapter.add(new Achievement("Night Owl", "Minutes focused between 10pm and 3am", mNightOwlTotal, MINUTE_LIMITS));
        mAdapter.add(new Achievement("Consistent", "Longest streak length", mMaxStreak, STREAK_LIMITS));
        mAdapter.add(new Achievement("Doing the Most", "Days with over 4 hours focused", mFourHourDays, NUM_STREAK_LIMITS));
        mAdapter.add(new Achievement("Monthly Max", "Most minutes focused in one month", mMaxOneMonth, MINUTE_LIMITS));
        mAdapter.add(new Achievement("Daily Max", "Most minutes focused in one day", mMaxOneDay, MINUTE_LIMITS));
        mAdapter.add(new Achievement("Intense Months", "Months with over sixty hours focused", mSixtyHourMonths, NUM_STREAK_LIMITS));

        mAdapter.notifyDataSetChanged();
    }

    // Saves manually calculated total time if not same as automatically incremented value
    private void confirmTotalTime() {
        if (!mBinding.tvTotal.getText().toString().isEmpty()
                && (mTotal != Integer.parseInt(mBinding.tvTotal.getText().toString()))
                && mUser.equals(ParseUser.getCurrentUser())) {
            mUser.put(FocusUser.KEY_TOTAL, mTotal);
            mUser.saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving accurate total time"));
        }
    }

    private void countCurrentStreaks() {
        Log.i(TAG, "Checking current streaks for user " + mUser.getUsername());
        final Calendar toCheck = Calendar.getInstance();
        final Calendar focusTime = Calendar.getInstance();
        boolean fullUnbroken = true;
        boolean workweekUnbroken = true;
        int focusIndex = 0;

        while(workweekUnbroken && (focusIndex < mFocuses.size())) {
            focusTime.setTime(mFocuses.get(focusIndex).getCreatedAt());
            if (focusTime.get(Calendar.DAY_OF_YEAR) == toCheck.get(Calendar.DAY_OF_YEAR)) {
                if(!isWeekend(focusTime)) {
                    mWorkweekStreak++;
                }
                if(fullUnbroken) {
                    mFullStreak++;
                }
                toCheck.add(Calendar.DAY_OF_YEAR, -1);
            } else if (focusTime.compareTo(toCheck) < 0) {
                if (!isWeekend(toCheck)) {
                    workweekUnbroken = false;
                }
                fullUnbroken = false;
            }
            focusIndex++;
        }
    }

    private void countTotals() {
        Log.i(TAG, "Counting total focus times and number of streaks for user " + mUser.getUsername());
        final Calendar focusTime = Calendar.getInstance();
        final Calendar toCheck = Calendar.getInstance();
        final Calendar matchDate = Calendar.getInstance();

        int daySum = 0;
        int monthSum = 0;
        int fullStreak = 0;

        for (Focus focus : mFocuses) {
            focusTime.setTime(focus.getCreatedAt());
            int length = focus.getInt(Focus.KEY_LENGTH);
            increaseLengths(length, focusTime);

            if(matchDate.get(Calendar.DAY_OF_YEAR) != focusTime.get(Calendar.DAY_OF_YEAR)) {
                if (daySum > 240) {
                    mFourHourDays++;
                }
                mMaxOneDay = Math.max(mMaxOneDay, daySum);
                daySum = 0;
            }
            if(matchDate.get(Calendar.MONTH) != focusTime.get(Calendar.MONTH)) {
                if (monthSum > 3600) {
                    mSixtyHourMonths++;
                }
                mMaxOneMonth = Math.max(mMaxOneMonth, monthSum);
                monthSum = 0;
            }
            matchDate.setTime(focusTime.getTime());
            daySum += length;
            monthSum += length;

            // Increase/save streaks
            if (focusTime.get(Calendar.DAY_OF_YEAR) == toCheck.get(Calendar.DAY_OF_YEAR)) {
                fullStreak++;
                toCheck.add(Calendar.DAY_OF_YEAR, -1);
            } else if (focusTime.compareTo(toCheck) < 0) {
                if(fullStreak >= MIN_STREAK_LENGTH) {
                    mNumStreaks++;
                    mMaxStreak = Math.max(mMaxStreak, fullStreak);
                }
                fullStreak = 1;
                toCheck.setTime(focusTime.getTime());
                toCheck.add(Calendar.DAY_OF_YEAR, -1);
            }
        }
        if(fullStreak >= MIN_STREAK_LENGTH) {
            mNumStreaks++;
        }
        if (daySum > 240) {
            mFourHourDays++;
        }
        if (monthSum > 3600) {
            mSixtyHourMonths++;
        }
        mMaxOneDay = Math.max(mMaxOneDay, daySum);
        mMaxOneMonth = Math.max(mMaxOneMonth, monthSum);
    }

    private void increaseLengths(int length, Calendar focusTime) {
        mTotal += length;

        if (isWeekend(focusTime)) {
            mWeekendTotal += length;
        } else {
            mWorkweekTotal += length;
        }

        int hour = focusTime.get(Calendar.HOUR_OF_DAY);
        if (hour < 7 && hour >= 4) {
            mEarlyBirdTotal += length;
        } else if (hour > 22 || hour <= 2) {
            mNightOwlTotal += length;
        }
    }

    private boolean isWeekend(Calendar calendar) {
        boolean isSaturday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
        boolean isSunday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        return isSaturday || isSunday;
    }
}