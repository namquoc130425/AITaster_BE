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
    DUPLICATE_EMAIL("Email trùng", HttpStatus.BAD_REQUEST),
    DUPLICATE_PHONE("Phone bị trùng",HttpStatus.BAD_REQUEST),
    INVALID_ROLE("Invalid Role",HttpStatus.BAD_REQUEST),;

    final String message;
    final HttpStatus httpStatus;

    public int getCode() {
        return  httpStatus.value();
    }
}
