package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
// hàm dùng đăng ký client
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ClientRegisterRequest {
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
     @NotBlank(message = "username is required")
     String username;
     @NotBlank(message = "phone is required")
     @Pattern(regexp = "^(0|84)(3|5|7|8|9)[0-9]{8}$", message = "phone invalid")
     String phone;
     @NotBlank(message = "FIELD_REQUIRED")
     String companyName;
     @NotBlank(message = "FIELD_REQUIRED")
     String contactName;
     String description;
     @NotBlank(message = "FIELD_REQUIRED")
     String businessField;
     String address;


}
