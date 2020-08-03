package com.example.pomfocus.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("FriendRequest")
public class FriendRequest extends ParseObject {
    public static final String KEY_FROM = "fromUser";
    public static final String KEY_TO = "toUser";
    public static final String KEY_STATUS = "status";
    public static final int PENDING = -1, ACCEPTED = 0, PROCESSED = 1;

    public void setFrom(ParseUser fromUser) {
        put(KEY_FROM, makePointer(fromUser));
    }

    public void setTo(ParseUser toUser) {
        put(KEY_TO, makePointer(toUser));
    }

    public static ParseUser makePointer(ParseUser user) {
        return (ParseUser) ParseUser.createWithoutData("_User", user.getObjectId());
    }

    public void setStatus(int status) {
        put(KEY_STATUS, status);
    }

    public int getStatus() {
        return getInt(KEY_STATUS);
    }

    public ParseUser getFromUser() {
        return getParseUser(KEY_FROM);
    }

    public String getFromUsername() {
        return getFromUser().getUsername();
    }
}
