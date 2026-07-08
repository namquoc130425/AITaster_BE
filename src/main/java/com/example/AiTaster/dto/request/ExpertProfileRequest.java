package com.example.AiTaster.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ExpertProfileRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    @Email(message = "INVALID_FORMART")
    String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, message = "INVALID_FORMART")
    String password;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 30, message = "fullName max 30 characters")
    String fullName;

    String avatarUrl;

    @NotBlank(message = "FIELD_REQUIRED")
    @Pattern(regexp = "^(0|84)(3|5|7|8|9)[0-9]{8}$", message = "INVALID_FORMART")
    String phone;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 1000, message = "bio max 1000 characters")
    String bio;

    @NotBlank(message = "FIELD_REQUIRED")
    String category;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 1000, message = "skills max 1000 characters")
    String skills;

    @JsonAlias({"yearOfExperience", "yearsOfExperience"})
    Integer yearOfExperience;

    String portfolioUrl;
}