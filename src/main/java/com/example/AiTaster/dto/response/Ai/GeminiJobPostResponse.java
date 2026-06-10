package com.example.AiTaster.dto.response.Ai;

import lombok.AccessLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeminiJobPostResponse {

    String title; // Tiêu đề job post

    String requirementDescription; // Mô tả yêu cầu

    String businessGoal; // Mục tiêu kinh doanh

    String mainFeatures; // Chức năng chính

    //String requiredSkills; // Skill dạng text tạm thời

    BigDecimal budgets;

    String timeLine;
  // đặt tên final là cho biết trả về skill cuối cùng nha :))))
    List<Long> finalSkillIds;;
}
