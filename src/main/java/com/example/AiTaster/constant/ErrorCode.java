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
    INVALID_SIZE("Must be between 1 and 50", HttpStatus.BAD_REQUEST),
    INVALID_FORMART("Invalid format", HttpStatus.BAD_REQUEST),

    NOT_FOUND("Not Found", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),

    DUPLICATE_EMAIL("Duplicate email", HttpStatus.BAD_REQUEST),
    DUPLICATE_PHONE("Duplicate phone number", HttpStatus.BAD_REQUEST),

    INVALID_TOKEN("Invalid Token", HttpStatus.UNAUTHORIZED),
    INVALID_ROLE("Invalid Role", HttpStatus.BAD_REQUEST),

    ACCOUNT_LOCKED("Account is locked", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED("Account is disabled", HttpStatus.FORBIDDEN),
    INVALID_LOGIN("Invalid name or password", HttpStatus.BAD_REQUEST),
    ALREADY_EXIST("Account already exist", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED("Password is required", HttpStatus.BAD_REQUEST),

    //---------------------------------------------------------------------------
    INVALID_REFRESH_TOKEN("Invalid refresh token!", HttpStatus.UNAUTHORIZED),;



    final String message;
    final HttpStatus httpStatus;

    public int getCode() {
        return  httpStatus.value();
    }
}
