package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminRegisterRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    String email;

    @NotBlank(message = "FIELD_REQUIRED")
    String username;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min = 8, message = "INVALID_FORMART")
    String password;

    @NotBlank(message = "FIELD_REQUIRED")
    String fullName;

    @NotBlank(message = "FIELD_REQUIRED")
    String avatarUrl;

    @NotBlank(message = "ErrorCode.FIELD_REQUIRED")
    @Pattern(regexp = "^(03|05|07|08|09|012|016|018|019)[0-9]{8}$", message = "INVALID_FORMAT")
    String phone;
}