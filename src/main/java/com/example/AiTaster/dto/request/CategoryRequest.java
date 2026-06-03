package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
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
    String categoryName;

    String slug;

    @NotBlank(message = "FIELD_REQUIRED")
    String description;
}
