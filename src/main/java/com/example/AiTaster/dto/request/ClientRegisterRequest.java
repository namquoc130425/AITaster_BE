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
     @NotBlank(message = "username is required")
     String username;
     @NotBlank(message = "phone is required")
     @Pattern(regexp = "^(0|84)(3|5|7|8|9)[0-9]{8}$", message = "phone invalid"
     )
     String phone;
     @NotBlank(message = "companyName is required")
     String companyName;
     @NotBlank(message = "contactName is required")
     String contactName;
     String description;
     @NotBlank(message = "businessField is required")
     String businessField;
     String address;


}
