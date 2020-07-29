package com.example.pomfocus;

public class Achievement {
    public final String title;
    public final String description;
    public final int progress;
    public final int[] limits;

    public Achievement(String title, String description, int progress, int[] limits) {
        this.title = title;
        this.description = description;
        this.progress = progress;
        this.limits = limits;
    }
}
