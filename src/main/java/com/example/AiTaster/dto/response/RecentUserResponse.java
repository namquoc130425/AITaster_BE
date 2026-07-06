package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecentUserResponse {

    Long userId;

    String username;

    String fullName;

    String email;

    String avatarUrl;

    Role role;

    UserStatus userStatus;

    LocalDateTime createAt;
}