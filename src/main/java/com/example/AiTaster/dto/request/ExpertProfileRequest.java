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
public class ExpertProfileRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    @Email(message = "INVALID_FORMART")
    String email;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 30, message = "Họ và tên không được vượt quá 30 ký tự")
    String fullName;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min = 5, max = 50, message = "Tên người dùng phải có từ 5 đến 50 ký tự")
    String username;

    String avatarUrl;

    @NotBlank(message = "FIELD_REQUIRED")
    @Pattern(regexp = "^(0|84)(3|5|7|8|9)[0-9]{8}$", message = "INVALID_FORMART")
    String phone;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 1000, message = "Phần giới thiệu không được vượt quá 1.000 ký tự")
    String bio;

    @NotNull(message = "FIELD_REQUIRED")
    @Positive(message = "Danh mục không hợp lệ")
    Long categoryId;

    @NotEmpty(message = "Vui lòng chọn ít nhất một kỹ năng")
    List<@Positive(message = "Kỹ năng không hợp lệ") Long> skillIds;

    @JsonAlias({"yearOfExperience", "yearsOfExperience"})
    @NotNull(message = "FIELD_REQUIRED")
    @Min(value = 0, message = "Số năm kinh nghiệm phải từ 0 trở lên")
    Integer yearOfExperience;

    String portfolioUrl;
}
