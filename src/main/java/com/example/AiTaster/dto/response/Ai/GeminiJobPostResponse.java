package com.example.AiTaster.dto.response.Ai;

import lombok.AccessLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;


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

   // TargetUsers targetUsers; // Người dùng mục tiêu

    String requiredSkills; // Skill dạng text tạm thời

    BigDecimal budgets;

    String timeLine;
}
