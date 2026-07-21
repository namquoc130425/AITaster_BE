package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.ProjectStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectCardResponse {
    Long projectId;
    Long invitationId;
    Long applicationId;
    Long jobPostId;
    String currentUserRole;
    String sourceType;
    String workflowStatus;

    String title;
    String description;
    String expectedOutput;
    String acceptanceCriteria;

    ProjectStatus projectStatus;
    InvitationStatus invitationStatus;
    String escrowStatus;
    String paymentStatus;

    String clientName;
<<<<<<< HEAD
    String companyName;
    String expertName;
=======
    String clientAvatarUrl;
    String companyName;
    String expertName;
    String expertAvatarUrl;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

    BigDecimal budget;
    String timeline;
    LocalDateTime deadlineAt;
    LocalDateTime paymentDeadlineAt;

    String currentStepCode;
    String currentStepTitle;
    String currentStepDescription;
    String milestoneStatus;

    Boolean canPayWithSepay;
    Boolean canOpenWorkspace;
    Boolean canViewPaymentStatus;
    Boolean canViewDetails;
    Boolean canViewSummary;
    Boolean canDownloadReceipt;

    List<ProjectStepResponse> steps;

    LocalDateTime startAt;
    LocalDateTime completedAt;
    LocalDateTime createAt;
    LocalDateTime updateAt;
}
