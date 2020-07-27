package com.example.pomfocus;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Focus")
public class Focus extends ParseObject {

    public static final String TAG = "Post";
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_LENGTH = "length";

    public void setCreator(ParseUser creator) { put(KEY_CREATOR, creator); }
}
