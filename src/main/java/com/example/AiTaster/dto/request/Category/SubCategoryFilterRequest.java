package com.example.AiTaster.dto.request.Category;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubCategoryFilterRequest {
    String categoryName;
    String slug;
}