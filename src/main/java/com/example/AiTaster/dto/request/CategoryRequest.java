package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CategoryRequest {
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min = 2, max = 100, message = "Tên danh mục phải có từ 2 đến 100 ký tự")
    String categoryName;

    @Size(max = 120, message = "Slug không được vượt quá 120 ký tự")
    String slug;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min = 10, max = 500, message = "Mô tả danh mục phải có từ 10 đến 500 ký tự")
    String description;
}
