package com.example.pomfocus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pomfocus.databinding.ActivityLoginBinding;
import com.example.pomfocus.parse.FocusUser;
import com.facebook.Profile;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.parse.facebook.ParseFacebookUtils;


public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private ActivityLoginBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        // Set up Facebook login button and callback
        mBinding.btnFBLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.btnFBLogin.setText(getString(R.string.logging_in));
                mBinding.btnFBLogin.setEnabled(false);
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, null, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {
                        if (user == null) {
                            Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
                            mBinding.btnFBLogin.setText(getString(R.string.continue_with_facebook));
                            mBinding.btnFBLogin.setEnabled(true);
                        } else if (user.isNew()) {
                            Log.d(TAG, "User signed up and logged in through Facebook!");
                            setName();
                            goMainActivity();
                        } else {
                            Log.d(TAG, "User logged in through Facebook!");
                            goMainActivity();
                        }
                    }
                });
            }
        });
    }

    public void setName() {
        ParseUser user = ParseUser.getCurrentUser();
        String name = String.format("%s %s.", Profile.getCurrentProfile().getFirstName(), Profile.getCurrentProfile().getLastName().substring(0,1));
        user.put(FocusUser.KEY_NAME, name);
        user.put(FocusUser.KEY_NAME_LOWERCASE, name.toLowerCase());
        user.put(FocusUser.KEY_USERNAME_LOWERCASE, user.getUsername().toLowerCase());
        user.saveInBackground();
    }

    public void logInUser(View view) {
        if(mBinding.btnSignup.getVisibility()==View.VISIBLE) {
            // User wants to log in. Display relevant buttons and hide signup button
            showCredentials(true, true);
        } else {
            // Get login credentials
            final String username = mBinding.etUsername.getText().toString();
            final String password = mBinding.etPassword.getText().toString();

            logInUser(username, password);
        }
    }

    private void logInUser(String username, String password) {
        mBinding.btnLogin.setText(getString(R.string.logging_in));
        mBinding.btnLogin.setEnabled(false);
        Log.i(TAG, "Attempting to login user " + username);
        if(username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Make sure to fill in both username and password", Toast.LENGTH_LONG).show();
            mBinding.btnLogin.setText(getString(R.string.log_in));
            mBinding.btnLogin.setEnabled(true);
            return;
        }

        // Check if login credentials are valid
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    mBinding.btnLogin.setText(getString(R.string.log_in));
                    mBinding.btnLogin.setEnabled(true);
                } else {
                    // Login was valid, proceed to main activity
                    goMainActivity();
                }
            }
        });
    }

    public void signUpUser(View view) {
        if(mBinding.btnLogin.getVisibility() == View.VISIBLE) {
            // User wants to create new account. Display relevant buttons and hide login
            showCredentials(true, false);
        } else {
            mBinding.btnSignup.setText(getString(R.string.signing_up));
            mBinding.btnSignup.setEnabled(false);
            // Get info for new account
            final String username = mBinding.etUsername.getText().toString();
            final String name = mBinding.etName.getText().toString();
            final String password = mBinding.etPassword.getText().toString();

            if(username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Make sure to fill in both username and password", Toast.LENGTH_LONG).show();
                return;
            }

            // Make ParseUser object
            ParseUser user = new ParseUser();
            user.setUsername(username);
            user.setPassword(password);
            user.put(FocusUser.KEY_NAME, name);
            user.put(FocusUser.KEY_NAME_LOWERCASE, name.toLowerCase());
            user.put(FocusUser.KEY_USERNAME_LOWERCASE, username.toLowerCase());
            user.signUpInBackground(new SignUpCallback() {
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Issue with signing up", e);
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        mBinding.btnSignup.setText(getString(R.string.sign_up));
                        mBinding.btnSignup.setEnabled(true);
                    } else {
                        logInUser(username, password);
                    }
                }
            });
        }
    }

    // Hide username/password and show login/signup buttons
    public void cancelLoginSignup(View view) {
        boolean login = mBinding.btnLogin.getVisibility() == View.VISIBLE;
        showCredentials(false, login);
    }

    private void showCredentials(boolean show, boolean login) {
        int visibility = show
                ? View.VISIBLE
                : View.GONE;
        int opposite = show
                ? View.GONE
                : View.VISIBLE;

        // Set up changing bounds
        ChangeBounds changeBounds = new ChangeBounds();
        if(login) {
            changeBounds.addTarget(mBinding.btnLogin);
        } else {
            changeBounds.addTarget(mBinding.btnSignup);
        }
        changeBounds.setDuration(300);

        // Set up fade-in
        Fade fadeIn = new Fade();
        fadeIn.setMode(Fade.IN);
        fadeIn.addTarget(mBinding.etPassword);
        fadeIn.addTarget(mBinding.etUsername);
        fadeIn.addTarget(mBinding.etName);
        fadeIn.addTarget(mBinding.btnCancel);
        if(!show) {
            fadeIn.addTarget(mBinding.btnSignup);
            fadeIn.addTarget(mBinding.btnLogin);
            fadeIn.addTarget(mBinding.btnFBLogin);
        }
        fadeIn.setDuration(300);
        fadeIn.setStartDelay(300);

        // Add transitions and begin
        Transition transition = new TransitionSet().addTransition(changeBounds).addTransition(fadeIn);
        TransitionManager.beginDelayedTransition((ViewGroup) mBinding.etUsername.getParent(), transition);

        // Display views accordingly
        mBinding.etUsername.setVisibility(visibility);
        mBinding.etPassword.setVisibility(visibility);
        mBinding.btnCancel.setVisibility(visibility);
        mBinding.btnFBLogin.setVisibility(opposite);
        mBinding.etName.setVisibility(View.GONE);
        mBinding.btnLogin.setVisibility(View.VISIBLE);
        mBinding.btnSignup.setVisibility(View.VISIBLE);
        if(login) {
            mBinding.btnSignup.setVisibility(opposite);
        } else {
            mBinding.btnLogin.setVisibility(opposite);
            mBinding.etName.setVisibility(visibility);
        }
    }

    private void goMainActivity() {
        FocusTimer.MIN_PER_FOCUS = ParseUser.getCurrentUser().getInt(FocusUser.KEY_FOCUS_LENGTH);
        FocusTimer.MIN_PER_BREAK = ParseUser.getCurrentUser().getInt(FocusUser.KEY_SHORT_BREAK_LENGTH);
        FocusTimer.MIN_PER_LONG_BREAK = ParseUser.getCurrentUser().getInt(FocusUser.KEY_LONG_BREAK_LENGTH);
        Intent i = new Intent(this, MainActivity.class);
        this.startActivity(i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}