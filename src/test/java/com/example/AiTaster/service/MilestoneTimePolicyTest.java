package com.example.AiTaster.service;

import com.example.AiTaster.constant.MilestoneStep;
import com.example.AiTaster.entity.ProjectMilestone;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MilestoneTimePolicyTest {

    @Test
    void autoReleaseAt_usesConfiguredWindow() {
        MilestoneTimePolicy policy = new MilestoneTimePolicy(3);
        LocalDateTime approvedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        ProjectMilestone milestone = ProjectMilestone.builder()
                .currentStep(MilestoneStep.FINAL_CONFIRMATION)
                .step2ApprovedAt(approvedAt)
                .build();

        assertThat(policy.autoReleaseAt(milestone)).isEqualTo(approvedAt.plusDays(3));
        assertThat(policy.overdueCutoff(approvedAt.plusDays(4))).isEqualTo(approvedAt.plusDays(1));
    }

    @Test
    void autoReleaseAt_isNullOutsidePendingFinalConfirmation() {
        MilestoneTimePolicy policy = new MilestoneTimePolicy(3);
        ProjectMilestone milestone = ProjectMilestone.builder()
                .currentStep(MilestoneStep.SOURCE_CODE)
                .step2ApprovedAt(LocalDateTime.now())
                .build();

        assertThat(policy.autoReleaseAt(milestone)).isNull();
    }
}
