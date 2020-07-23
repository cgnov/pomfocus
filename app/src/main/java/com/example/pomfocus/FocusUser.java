package com.example.pomfocus;

import com.parse.ParseClassName;
import com.parse.ParseUser;

@ParseClassName("_User")
public class FocusUser extends ParseUser {
    public static final String TAG = "FocusUser";
    public static final String KEY_STREAK = "streak";
    public static final String KEY_NAME = "name";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_TOTAL = "totalTime";
    public static final String KEY_RANK = "rank";
}
