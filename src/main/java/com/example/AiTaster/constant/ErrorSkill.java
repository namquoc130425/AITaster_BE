package com.example.AiTaster.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public enum ErrorSkill {

    SKILL_EXISTS("Kỹ năng đã tồn tại", HttpStatus.BAD_REQUEST),
    NOT_FOUND_SKILL("Không tìm thấy kỹ năng", HttpStatus.BAD_REQUEST),
    ;


    final String message;
    final HttpStatus httpStatus;

    public int getCode() {
        return httpStatus.value();
    }
    }

