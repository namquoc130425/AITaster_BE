package com.example.AiTaster.service;

import com.example.AiTaster.exception.GlobalException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.example.AiTaster.constant.ErrorCode.*;

// Hiện tại nam dang set cứng vì chua biet check từ cấm như nào admin quản lý hay sao , sau này update lại
@Service
public class ContentManagerService {
    private static final List<String> BLOCK_KEYWORDS = List.of("hack", // Chặn nội dung hack
            "ddos",
            "malware",
            "scam",
            "crack" );

    private static final List<String> PROMPT_INJECTION_PATTERNS = List.of( // Danh sách dấu hiệu prompt injection
            "ignore previous instructions", // Cố ép AI bỏ instruction
            "bỏ qua hướng dẫn trước", // Bản tiếng Việt
            "system prompt", // Cố hỏi system prompt
            "developer message", // Cố hỏi developer message
            "trả lời ngoài json" // Cố ép AI trả sai format
    );

    public void validateKeywordInput(String keyword) {
        if(keyword == null || keyword.isBlank()) {
            throw new GlobalException(FIELD_REQUIRED.getCode(), FIELD_REQUIRED.getMessage());
        }
String keywordsNormal = keyword.toLowerCase();
        for(String blockKeyword : BLOCK_KEYWORDS) {
            if(keywordsNormal.contains(blockKeyword)) {
                throw new GlobalException(BLOCKED_KEYWORD.getCode(),BLOCKED_KEYWORD.getMessage());
            }
        }

        for (String injectPatterns : PROMPT_INJECTION_PATTERNS) {
            if(keywordsNormal.contains(injectPatterns)) {
                throw new GlobalException(PROMPT_INJECTION.getCode(),PROMPT_INJECTION.getMessage());
            }
        }
    }
}
