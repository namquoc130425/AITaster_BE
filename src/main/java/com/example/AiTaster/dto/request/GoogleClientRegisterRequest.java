package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleClientRegisterRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    String accessToken;

    @NotBlank(message = "FIELD_REQUIRED")
    String fullName;

    @NotBlank(message = "FIELD_REQUIRED")
    String username;

    @NotBlank(message = "FIELD_REQUIRED")
    String password;

    @NotBlank(message = "FIELD_REQUIRED")
    String phone;

    String companyName;

    String contactName;

    String businessField;

    String address;

    String description;
}