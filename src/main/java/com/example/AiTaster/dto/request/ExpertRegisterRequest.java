package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ExpertRegisterRequest {
     @NotBlank(message = "email is required")
     @Email(message = "email invalid")
     String email;

     @NotBlank(message = "password is required")
     @Size(min = 8, message = "password must be at least 8 characters")
     String password;
     @NotBlank(message = "fullName is required")
     @Size(max = 30, message = "fullName max 30 characters")
     String fullName;
     String avatarUrl;

     @NotBlank(message = "phone is required")
     @Pattern(regexp = "^(0|84)(3|5|7|8|9)[0-9]{8}$", message = "phone invalid")
     String phone;

     @NotBlank(message = "bio is required")
     @Size(max = 1000, message = "bio max 1000 characters")
     String bio;

     @NotBlank(message = "category is required")
     String category;

     @NotBlank(message = "skills is required")
     @Size(max = 1000, message = "skills max 1000 characters")
     String skills;

     @NotBlank(message = "yearOfExperience is required")
     String yearOfExperience;

//     // rating mặc định thường là 0
//     // không được âm
//     @DecimalMin(value = "0.0", message = "rating must be greater than or equal to 0")
//     BigDecimal rating;

//     //số project hoàn thành mặc định thường là 0
//     // không được âm
//     @DecimalMin(value = "0", message = "completedProjects must be greater than or equal to 0")
//     Integer completedProjects;

     String portfolioUrl;


}
