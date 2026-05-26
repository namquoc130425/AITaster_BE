package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.Gender;
import com.example.AiTaster.constant.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class RegisterRequest {

    @NotBlank(message =  "FIELD_REQUIRED")  // dùng cho String -> yêu cầu không đc null , không spack , không rỗng
    @Size(min = 1 , max = 50,message = "INVALID_SIZE")
    // phải là chữ và không đc để trống
    String name;


    @NotBlank(message =  "FIELD_REQUIRED")
    String username;

    @NotBlank(message =  "FIELD_REQUIRED")
    String password;

    @NotNull(message =  "FIELD_REQUIRED")
    int age;

    @NotBlank(message =  "FIELD_REQUIRED")
    String address;

    @NotBlank(message =  "FIELD_REQUIRED")
    @Pattern(regexp = "^(03|05|07|08|09|012|016|018|019)[0-9]{8}$", message = "INVALID_FORMART")
    String phone;

    // không đc null
    @NotNull(message =  "FIELD_REQUIRED")  // enum -> phải dùng not null -> không null + không rỗng
    @Enumerated(EnumType.STRING) // lưu xuongs db sẽ là dạng chữ thay vì số
            Gender gender;
    @NotNull(message =  "FIELD_REQUIRED")
    @Enumerated(EnumType.STRING)
    Role role;
}
