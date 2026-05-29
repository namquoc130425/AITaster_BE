package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    String password;

    @NotBlank(message = "FIELD_REQUIRED")
    String fullName;

    String avatarUrl;

    @NotBlank(message = "FIELD_REQUIRED")
    @Pattern(regexp = "^(03|05|07|08|09|012|016|018|019)[0-9]{8}$", message = "INVALID_FORMAT")
    String phone;
}