package com.example.pomfocus.parse;

import com.parse.ParseClassName;
import com.parse.ParseUser;

@ParseClassName("_User")
public class FocusUser extends ParseUser {
    public static final String TAG = "FocusUser";
    public static final String KEY_NAME = "name";
    public static final String KEY_NAME_LOWERCASE = "lowercaseName";
    public static final String KEY_HANDLE = "username";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_TOTAL = "totalTime";
    public static final String KEY_RANK = "rank";
    public static final String KEY_FOCUS = "focusMode";
    public static final String KEY_SCREEN = "keepScreenOn";
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_PRIVATE = "private";
    public static final String KEY_FOCUS_LENGTH = "focusLength";
    public static final String KEY_SHORT_BREAK_LENGTH = "shortBreakLength";
    public static final String KEY_LONG_BREAK_LENGTH = "longBreakLength";
    public static final String KEY_HIDE_FROM_LEADERBOARD = "hideFromAllLeaderboard";

    public static int getFocusLength() {
        return ParseUser.getCurrentUser().getInt(KEY_FOCUS_LENGTH);
    }

    public static int getShortBreakLength() {
        return ParseUser.getCurrentUser().getInt(KEY_SHORT_BREAK_LENGTH);
    }

    public static int getLongBreakLength() {
        return ParseUser.getCurrentUser().getInt(KEY_LONG_BREAK_LENGTH);
    }
}
