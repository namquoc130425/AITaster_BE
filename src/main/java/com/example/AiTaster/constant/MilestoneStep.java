package com.example.AiTaster.constant;

public enum MilestoneStep {
    DOCUMENT("Mốc 1 - Thiết Kế và Nộp Tài liệu chi tiết "),
    SOURCE_CODE("Mốc 2 -  thực hiện và gữi Source code"),
    FINAL_CONFIRMATION("Mốc 3 - Xác nhận hoàn tất dự án ");

    private final String title;

    MilestoneStep(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
}
