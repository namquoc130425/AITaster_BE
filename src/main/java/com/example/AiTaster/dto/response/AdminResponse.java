package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminResponse {

    Long userId;

    String username;

    String email;

    String fullName;

    String avatarUrl;

    String phone;

    Role role;

    UserStatus userStatus;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}
