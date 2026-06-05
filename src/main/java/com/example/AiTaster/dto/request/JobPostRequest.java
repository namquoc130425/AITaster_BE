package com.example.AiTaster.dto.request;

import com.example.AiTaster.entity.Skill;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostRequest {
    @NotBlank(message = "TITLE_REQUIRED")
    String title;

    @NotBlank(message = "REQUIREMENT_DESCRIPTION_REQUIRED")
    String requirementDescription;

    @NotBlank(message = "BUSINESS_GOAL_REQUIRED")
    String businessGoal;

    @NotBlank(message = "MAIN_FEATURES_REQUIRED")
    String mainFeatures;

//    @NotNull(message = "TARGET_USERS_REQUIRED")
//    TargetUsers targetUsers; // Nhóm người dùng mục tiêu

//
//    String requiredSkills;

    @NotNull(message = "BUDGETS_REQUIRED") // Budget không được null
    @DecimalMin(value = "0.0", inclusive = false, message = "BUDGETS_INVALID") // Budget phải lớn hơn 0
    BigDecimal budgets;

    @NotBlank(message = "TIMELINE_REQUIRED")
    String timeLine;

  @Size(max = 10)
    List<Long> selectedSkillIds;

}
