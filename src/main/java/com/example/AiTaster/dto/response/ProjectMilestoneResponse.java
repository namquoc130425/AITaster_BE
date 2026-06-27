package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.MilestoneStatus;
import com.example.AiTaster.constant.MilestoneStep;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectMilestoneResponse {
    Long projectId;

    Long milestoneId;

    MilestoneStep currentStep;

    String currentStepTitle;

    MilestoneStatus status;

    LocalDateTime step1ApprovedAt;

    LocalDateTime step2ApprovedAt;

    LocalDateTime finalApprovedAt;
}
