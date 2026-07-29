package com.example.AiTaster.service.scheduler;

import com.example.AiTaster.constant.MilestoneStep;
import com.example.AiTaster.repository.ProjectMilestoneRepo;
import com.example.AiTaster.service.ProjectMilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MilestoneAutoReleaseScheduler {
    private final ProjectMilestoneRepo projectMilestoneRepo;
    private final ProjectMilestoneService projectMilestoneService;

    @Value("${app.jobs.milestone-auto-release.days:3}")
    private int autoReleaseDays;

    @Scheduled(fixedDelayString = "${app.jobs.milestone-auto-release.fixed-delay-ms:60000}")
    public void autoReleaseOverdueFinalMilestones() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(autoReleaseDays);

        List<Long> overdueMilestoneIds = projectMilestoneRepo.findOverdueFinalConfirmationMilestoneIds(MilestoneStep.FINAL_CONFIRMATION, deadline
        );

        if (overdueMilestoneIds.isEmpty()) {
            return;
        }

        // Xử lý từng milestone độc lập: 1 project lỗi (VD escrow đã bị đụng ở nơi khác) không được làm hỏng cả batch.
        overdueMilestoneIds.forEach(milestoneId -> {
            try {
                projectMilestoneService.autoReleaseOverdueFinalMilestone(milestoneId);
            } catch (Exception e) {
                log.error("Failed to auto-release milestone {}", milestoneId, e);
            }
        });

        log.info("Processed {} overdue final-confirmation milestones", overdueMilestoneIds.size());
    }

}
