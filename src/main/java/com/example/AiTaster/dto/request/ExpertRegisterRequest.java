package com.example.AiTaster.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ExpertRegisterRequest {
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
     @NotBlank(message = "username is required")
     String username;

     @NotBlank(message = "phone is required")
     @Pattern(regexp = "^(0|84)(3|5|7|8|9)[0-9]{8}$", message = "phone invalid")
     String phone;

     @NotBlank(message = "FIELD_REQUIRED")
     @Size(max = 1000, message = "bio max 1000 characters")
     String bio;

     @NotNull(message = "FIELD_REQUIRED")
     @JsonAlias({"selectedCategoryId", "category"})
     Long categoryId;

     @NotEmpty(message = "FIELD_REQUIRED")
     @JsonAlias({"selectedSkillIds", "skills"})
     List<Long> skillIds;


     @JsonAlias("yearOfExperience")
     Integer yearOfExperience;

//     // rating mặc định thường là 0
//     // không được âm
//     @DecimalMin(value = "0.0", message = "rating must be greater than or equal to 0")
//     BigDecimal rating;

//     //số project hoàn thành mặc định thường là 0
//     // không được âm
//     @DecimalMin(value = "0", message = "completedProjects must be greater than or equal to 0")
//     Integer completedProjects;

     String portfolioUrl;

     @NotBlank(message = "certificateUrl is required")
     String certificateUrl;

}
