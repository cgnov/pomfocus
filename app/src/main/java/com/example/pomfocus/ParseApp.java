package com.example.pomfocus;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.parse.facebook.ParseFacebookUtils;

public class ParseApp extends Application {
    private static final String TAG = "ParseApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Register parse models
        ParseObject.registerSubclass(Focus.class);
        ParseObject.registerSubclass(FriendRequest.class);

        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("pomodoro-focus") // APP_ID env variable
                .clientKey("pomodoroFocusMasterKey")  // set explicitly unless clientKey is explicitly configured on Parse server
                .server("https://pomodoro-focus.herokuapp.com/parse/").build());

        ParseFacebookUtils.initialize(this);
    }

    public static int getAttrColor(@Nullable Context context, int attr) {
        assert context != null;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }

    public static SaveCallback makeSaveCallback(final String TAG, final String logExplanation) {
        return new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, logExplanation, e);
                }
            }
        };
    }
}