package com.example.pomfocus;

public class Achievement {
    public final String mTitle;
    public final String mDescription;
    public final int mProgress;
    public final int[] mLimits;

    public Achievement(String title, String description, int progress, int[] limits) {
        this.mTitle = title;
        this.mDescription = description;
        this.mProgress = progress;
        this.mLimits = limits;
    }
}
