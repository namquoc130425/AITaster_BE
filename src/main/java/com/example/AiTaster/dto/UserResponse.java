package com.example.AiTaster.dto;

import com.example.AiTaster.constant.Gender;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    Long userId;
    String username;
    String fullName;
    Role role;
    String email;
    String phone;
    String avatarUrl;
    String accessToken;
}
