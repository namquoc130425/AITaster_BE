package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminRequest {
    @NotBlank(message = "FIELD_REQUIRED")
    String email;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min = 8, message = "INVALID_FORMART")
    String password;

    String fullName;

    String avatarUrl;

    String phone;

    @NotBlank(message = "FIELD_REQUIRED")
    Role role;

    UserStatus userStatus;
}