package com.example.AiTaster.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor

public enum ErrorCode {
    FIELD_REQUIRED("Cannot be blank", HttpStatus.BAD_REQUEST),
    INVALID_SIZE("Must be between 1 and 50",HttpStatus.BAD_REQUEST),
    INVALID_FORMART("Invalid format",HttpStatus.BAD_REQUEST),
    NOT_FOUND("Not Found",HttpStatus.NOT_FOUND),
    INVALID_TOKEN("Invalid Token",HttpStatus.UNAUTHORIZED),
    INVALID_ROLE("Invalid Role",HttpStatus.BAD_REQUEST),

    INAPPROPRIATE_CONTENT("Nội dung không phù hợp", HttpStatus.BAD_REQUEST),


    BLOCKED_KEYWORD("Từ khóa bị chặn", HttpStatus.BAD_REQUEST),
    PROMPT_INJECTION("Phát hiện prompt injection", HttpStatus.BAD_REQUEST),
    CALL_AI_FAIL("Gọi Gemini API thất bại",HttpStatus.BAD_REQUEST);


    final String message;
    final HttpStatus httpStatus;

    public int getCode() {
        return  httpStatus.value();
    }
}
