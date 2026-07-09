package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleExpertRegisterRequest {

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

    @NotNull(message = "FIELD_REQUIRED")
    Long categoryId;

    String bio;

    List<Long> skillIds;

    Integer yearOfExperience;

    String portfolioUrl;

    String certificateUrl;
}