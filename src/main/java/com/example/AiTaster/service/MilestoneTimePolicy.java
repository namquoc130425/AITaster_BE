package com.example.AiTaster.service;

import com.example.AiTaster.constant.MilestoneStep;
import com.example.AiTaster.entity.ProjectMilestone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class MilestoneTimePolicy {
    private final Duration autoReleaseWindow;

    public MilestoneTimePolicy(
            @Value("${app.jobs.milestone-auto-release.days:3}") long autoReleaseDays
    ) {
        this.autoReleaseWindow = Duration.ofDays(autoReleaseDays);
    }

    public LocalDateTime autoReleaseAt(ProjectMilestone milestone) {
        if (milestone == null
                || milestone.getCurrentStep() != MilestoneStep.FINAL_CONFIRMATION
                || milestone.getStep2ApprovedAt() == null
                || milestone.getFinalApprovedAt() != null) {
            return null;
        }

        return milestone.getStep2ApprovedAt().plus(autoReleaseWindow);
    }

    public LocalDateTime overdueCutoff(LocalDateTime now) {
        return now.minus(autoReleaseWindow);
    }
}
