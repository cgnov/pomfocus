package com.example.pomfocus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;

import com.example.pomfocus.fragments.EditLengthsDialogFragment;

public class CustomTextView extends AppCompatTextView {

    private static final String TAG = "CustomTextView";

    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
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

}