package com.example.pomfocus;

public class Milestone {
    public final int mLevel, mMax, mProgress;
    public final boolean mCurrentLevel;

    public Milestone(int level, int max, int progress, boolean currentLevel) {
        mLevel = level;
        mMax = max;
        mProgress = progress;
        mCurrentLevel = currentLevel;
    }
}
