package com.example.pomfocus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;

import com.example.pomfocus.fragments.EditLengthsDialogFragment;

public class TimerTextView extends AppCompatTextView {

    private static final String TAG = "CustomTextView";

    public TimerTextView(Context context) {
        super(context);
    }

    public TimerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        return super.onTouchEvent(event);
    }

    // Because we call this from onTouchEvent, this code will be executed for both
    // normal touch events and for when the system calls this using Accessibility
    @Override
    public boolean performClick() {
        super.performClick();
        FragmentManager fragmentManager = ((AppCompatActivity)getContext()).getSupportFragmentManager();
        EditLengthsDialogFragment eldFragment = new EditLengthsDialogFragment();
        eldFragment.show(fragmentManager, TAG);
        return true;
    }

    public static void setUpTouchListener(final TimerTextView timerTextView, final GestureDetector gestureDetector) {
        timerTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(gestureDetector.onTouchEvent(motionEvent)) {
                    timerTextView.performClick();
                }
                return true;
            }
        });
    }
}