package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminRequest {
    String email;
    String password;
    String fullName;
    String avatarUrl;
    String phone;
    Role role;
    UserStatus userStatus;
}