package com.example.AiTaster.constant;

public enum TimelineUnit {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month");
    private final String displayName;

    TimelineUnit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
