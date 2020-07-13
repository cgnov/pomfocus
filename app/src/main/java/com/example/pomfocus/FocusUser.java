package com.example.pomfocus;

import com.parse.ParseClassName;
import com.parse.ParseUser;

@ParseClassName("_User")
public class FocusUser extends ParseUser {

    public static final String TAG = "FocusUser";
    public static final String KEY_STREAK = "streak";
    public static final String KEY_NAME = "firstName";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_TOTAL = "totalTime";

    public String getFirstName() { return getString(KEY_NAME); }

    public void setFirstName(String name) { put(KEY_NAME, name); }

    public long getTotalTime() { return getLong(KEY_TOTAL); }

    public void incrementTotalTime(int newTime) { put(KEY_TOTAL, getTotalTime() + newTime); }
}
