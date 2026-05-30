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

    SKILL_EXISTS("Skill already exist", HttpStatus.BAD_REQUEST),
    NOT_FOUND_SKILL("Skill not found", HttpStatus.BAD_REQUEST),
    ;


    final String message;
    final HttpStatus httpStatus;

    int getCode() {
        return httpStatus.value();
    }
    }

