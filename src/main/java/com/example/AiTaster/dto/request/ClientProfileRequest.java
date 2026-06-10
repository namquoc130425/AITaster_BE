package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.Email;
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
    @NotBlank(message = "FIELD_REQUIRED")
    @Email(message = "INVALID_FORMART")
    String email;
    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, message = "INVALID_FORMART")
    String password;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 50, message = "fullName max 50 characters")
    String fullName;


    String avatarUrl;

    @NotBlank(message = "FIELD_REQUIRED")
    @Pattern(regexp = "^(84|0)(3|5|7|8|9)[0-9]{8}$", message = "INVALID_FORMART")
    //đúng kiểu sdt với không được để trống
    String phone;

//    Long userId;
//    @NotBlank(message = "field is required")
    String companyName;
    @NotBlank(message = "FIELD_REQUIRED")
    String contactName;

    String description;
    @NotBlank(message = "FIELD_REQUIRED")
    String businessField;
    @NotBlank(message = "FIELD_REQUIRED")
    String address;
}
