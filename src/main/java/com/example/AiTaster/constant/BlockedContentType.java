package com.example.AiTaster.constant;

public enum BlockedContentType {
    KEYWORD,  // Từ khóa cấm thường: hack, scam, malware.
    PROMPT_INJECTION // Câu kiểu phá AI: ignore previous instructions.
}
