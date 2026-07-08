package com.example.AiTaster.dto.request.Category;

import com.example.AiTaster.dto.request.PageRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CategoryFilterRequest extends PageRequest {
    SubCategoryFilterRequest filter;
}