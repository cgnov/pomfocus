package com.example.pomfocus;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.facebook.ParseFacebookUtils;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseObject.registerSubclass(Focus.class);

        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("pomodoro-focus") // APP_ID env variable
                .clientKey("pomodoroFocusMasterKey")  // set explicitly unless clientKey is explicitly configured on Parse server
                .server("https://pomodoro-focus.herokuapp.com/parse/").build());

        ParseFacebookUtils.initialize(this);
    }
}