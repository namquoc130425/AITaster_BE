package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
// dùng cho updateProfile của client
public class ClientProfileRequest {
    @NotBlank(message = "field is required")
    String email;
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    String password;

    @NotBlank(message = "fullName is required")
    @Size(max = 50, message = "fullName max 50 characters")
    String fullName;


    String avatarUrl;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^(84|0)(3|5|7|8|9)[0-9]{8}$", message = "phone invalid")
    //đúng kiểu sdt với không được để trống
    String phone;

//    Long userId;
//    @NotBlank(message = "field is required")
    String companyName;
    @NotBlank(message = "field is required")
    String contactName;

    String description;
    @NotBlank(message = "field is required")
    String businessField;
    @NotBlank(message = "field is required")
    String address;
}
