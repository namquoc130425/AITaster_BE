package com.example.AiTaster.dto.request.skillsRequest;

import com.example.AiTaster.constant.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkillRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    String skillName;

    @NotBlank(message = "FIELD_REQUIRED")
    String slug;

    String description;


}
