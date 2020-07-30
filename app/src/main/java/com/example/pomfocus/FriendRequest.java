package com.example.pomfocus;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("FriendRequest")
public class FriendRequest extends ParseObject {
    public static final String KEY_FROM = "fromUser";
    public static final String KEY_TO = "toUser";

    public void setFrom(ParseUser fromUser) {
        ParseUser pointer = (ParseUser) ParseUser.createWithoutData("_User", fromUser.getObjectId());
        put(KEY_FROM, pointer);
    }

    public void setTo(ParseUser toUser) {
        ParseUser pointer = (ParseUser) ParseUser.createWithoutData("_User", toUser.getObjectId());
        put(KEY_TO, pointer);
    }
}
