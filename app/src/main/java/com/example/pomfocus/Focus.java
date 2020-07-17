package com.example.pomfocus;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Focus")
public class Focus extends ParseObject {

    public static final String TAG = "Post";
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_CREATED = "createdAt";
    public static final String KEY_LENGTH = "length";

    public ParseUser getCreator() { return getParseUser(KEY_CREATOR); }

    public void setCreator(ParseUser creator) { put(KEY_CREATOR, creator); }
}
