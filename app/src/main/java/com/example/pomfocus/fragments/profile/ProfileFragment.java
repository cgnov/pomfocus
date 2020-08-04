package com.example.pomfocus.fragments.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pomfocus.ParseApp;
import com.example.pomfocus.parse.FriendRequest;
import com.example.pomfocus.parse.Focus;
import com.example.pomfocus.parse.FocusUser;
import com.example.pomfocus.R;
import com.example.pomfocus.databinding.FragmentProfileBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";
    public ParseUser mUser;
    private FragmentProfileBinding mBinding;
    private List<Focus> mFocuses;
    private int mFullStreak = 0, mWorkweekStreak = 0;
    private boolean mConfirmedFriend = false;
    private ProfileSnapshotFragment mProfileSnapshotFragment = null;
    private ProfileAchievementsFragment mProfileAchievementsFragment = null;

    public ProfileFragment(ParseUser user) {
        this.mUser = user;
    }

    public ProfileFragment(ParseUser user, boolean friend) {
        this.mUser = user;
        mConfirmedFriend = friend;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Implement view binding
        mBinding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(TAG, "onViewCreated, mFocuses==null: " + (mFocuses==null) + ", mProfileSnapshot==null: " + (mProfileSnapshotFragment==null));
        displayRelevantFragments();
    }

    private void displayRelevantFragments() {
        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction()
                .replace(R.id.flPublicInfo, new ProfilePublicInfoFragment(mUser))
                .commit();
        if (mUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            displayFriendPrivileges();
            getFragmentManager().beginTransaction()
                    .replace(R.id.flButtons, new ProfileButtonFragment())
                    .commit();
        } else {
            if (mConfirmedFriend) {
                displayFriendPrivileges();
                getFragmentManager().beginTransaction()
                        .replace(R.id.flRequest, new ProfileRequestFragment(mUser, FriendRequest.PROCESSED, null))
                        .commit();
            } else {
                checkFriendStatus();
            }
        }
    }

    private void displayFriendPrivileges() {
        assert getFragmentManager() != null;
        mProfileSnapshotFragment = new ProfileSnapshotFragment(mUser.getInt(FocusUser.KEY_TOTAL));
        getFragmentManager().beginTransaction().replace(R.id.flSnapshot, mProfileSnapshotFragment).commit();
        mProfileAchievementsFragment = new ProfileAchievementsFragment();
        getFragmentManager().beginTransaction().replace(R.id.flAchievements, mProfileAchievementsFragment).commit();

        if (mFocuses == null) {
            findFullFocusHistory();
        } else {
            displayInfo();
        }
    }

    private void findFullFocusHistory() {
        Log.i(TAG, "Querying full focus history from Parse for user " + mUser.getUsername());
        mFocuses = new ArrayList<>();
        ParseQuery<Focus> fullHistoryQuery = ParseQuery.getQuery(Focus.class);
        fullHistoryQuery.whereEqualTo(Focus.KEY_CREATOR, mUser);
        fullHistoryQuery.addDescendingOrder(Focus.KEY_CREATED_AT);

        fullHistoryQuery.findInBackground(new FindCallback<Focus>() {
            @Override
            public void done(List<Focus> focuses, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue getting focus history", e);
                    mFocuses = null;
                } else {
                    mFocuses = focuses;
                    countCurrentStreaks();
                    displayInfo();
                }
            }
        });
    }

    public void countCurrentStreaks() {
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

    public void displayInfo() {
        Log.i(TAG, "displayInfo");
        mProfileSnapshotFragment.setStreak(mFullStreak);
        if (mProfileAchievementsFragment != null) {
            mProfileAchievementsFragment.countTotals(mFocuses, mUser.getUsername().equals(ParseApp.currentUsername()));
        }
    }

    public static boolean isWeekend(Calendar calendar) {
        boolean isSaturday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
        boolean isSunday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        return isSaturday || isSunday;
    }

    private void checkFriendStatus() {
        ParseQuery<FriendRequest> sentRequest = ParseQuery.getQuery(FriendRequest.class);
        sentRequest.whereEqualTo(FriendRequest.KEY_TO, ParseUser.getCurrentUser());
        sentRequest.whereEqualTo(FriendRequest.KEY_FROM, mUser);

        ParseQuery<FriendRequest> receivedRequest = ParseQuery.getQuery(FriendRequest.class);
        receivedRequest.whereEqualTo(FriendRequest.KEY_FROM, ParseUser.getCurrentUser());
        receivedRequest.whereEqualTo(FriendRequest.KEY_TO, mUser);

        List<ParseQuery<FriendRequest>> queryList = new ArrayList<>();
        queryList.add(sentRequest);
        queryList.add(receivedRequest);

        ParseQuery<FriendRequest> fullQuery = ParseQuery.or(queryList);
        fullQuery.include(FriendRequest.KEY_FROM);
        fullQuery.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> friendRequests, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting friend request status", e);
                } else {
                    startRequestFragment(friendRequests);
                }
            }
        });
    }

    private void startRequestFragment(List<FriendRequest> friendRequests) {
        Log.i(TAG, "Checking friend status");
        int status = ProfileRequestFragment.NONE;
        FriendRequest request = null;
        if (friendRequests.size() > 0) {
            request = friendRequests.get(0);
            status = request.getStatus();
            if (request.getFromUsername().equals(ParseApp.currentUsername())) {
                if (status == FriendRequest.PENDING) {
                    status = ProfileRequestFragment.SENT;
                } else if (status == FriendRequest.ACCEPTED) {
                    processAcceptance(request);
                    status = FriendRequest.PROCESSED;
                }
            }
        }
        Log.i(TAG, "Friend status of " + status);
        if ((status == FriendRequest.PROCESSED) || (status == FriendRequest.ACCEPTED)) {
            displayFriendPrivileges();
        } else {
            displayNotFriends();
        }
        assert getFragmentManager() != null;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.flRequest, new ProfileRequestFragment(mUser, status, request))
                .commit();
    }

    private void displayNotFriends() {
        if (mUser.getBoolean(FocusUser.KEY_PRIVATE)) {
            Toast.makeText(getContext(), "This user is private. Become friends to see their focus details and achievements", Toast.LENGTH_LONG).show();
        } else {
            displayFriendPrivileges();
        }
    }

    private void processAcceptance(FriendRequest friendRequest) {
        ParseRelation<ParseUser> friends = ParseUser.getCurrentUser().getRelation(FocusUser.KEY_FRIENDS);
        friends.add(FriendRequest.makePointer(friendRequest.getFromUser()));
        ParseUser.getCurrentUser().put(FocusUser.KEY_FRIENDS, friends);
        ParseUser.getCurrentUser().saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving new accepted friend"));

        // Update friend request to have processed and accepted
        friendRequest.setStatus(FriendRequest.PROCESSED);
        friendRequest.saveInBackground(ParseApp.makeSaveCallback(TAG, "Error saving processed friend request"));
    }
}