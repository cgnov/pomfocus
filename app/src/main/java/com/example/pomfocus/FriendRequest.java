package com.example.pomfocus;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("FriendRequest")
public class FriendRequest extends ParseObject {
    public static final String KEY_FROM = "fromUser";
    public static final String KEY_TO = "toUser";
    public static final String KEY_ACCEPTED = "accepted";

    public void setFrom(ParseUser fromUser) {
        put(KEY_FROM, makePointer(fromUser));
    }

    public void setTo(ParseUser toUser) {
        put(KEY_TO, makePointer(toUser));
    }

    public static ParseUser makePointer(ParseUser user) {
        return (ParseUser) ParseUser.createWithoutData("_User", user.getObjectId());
    }

    public void setAccepted() {
        put(KEY_ACCEPTED, true);
    }

    public boolean getAccepted() {
        return getBoolean(KEY_ACCEPTED);
    }
}
