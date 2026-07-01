package com.example.AiTaster.constant;

public enum MilestoneStep {
    DOCUMENT("Mốc 1: Review Tài Liệu"),
    SOURCE_CODE("Mốc 2: Phát triển"),
    FINAL_CONFIRMATION("Mốc 3: Nghiệm thu");

    private final String title;

    MilestoneStep(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
