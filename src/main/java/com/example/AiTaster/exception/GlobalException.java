package com.example.AiTaster.exception;

import com.example.AiTaster.constant.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GlobalException extends RuntimeException {
    Integer code;

    public GlobalException(String message) {
        super(message);
    }


    public GlobalException(int code,String message) {
        super(message);
        this.code = code;
    }

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public GlobalException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
    }
}
