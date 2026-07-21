package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.DisputeStatus;
import com.example.AiTaster.constant.DisputeDecision;
import com.example.AiTaster.constant.EscrowStatus;
import com.example.AiTaster.constant.MilestoneStep;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeResponse {

    Long disputeId;

    Long projectId;
    String projectTitle;

    Long deliverableId;
    MilestoneStep disputedStep;
    String disputedStepTitle;
    Integer disputedDeliverableVersion;
    LocalDateTime disputedDeliverableSubmittedAt;

    Long reporterId;
    String reporterName;

    Long reportedAgainstId;
    String reportedAgainstName;

    String reason;
    String evidence;
    String response;

    DisputeStatus disputeStatus;
    DisputeDecision disputeDecision;
    String projectOutcome;

    BigDecimal refundAmount;
    BigDecimal releaseAmount;
    BigDecimal escrowHeldAmount;
    BigDecimal escrowPlatformFee;
    BigDecimal escrowExpertAmount;
    EscrowStatus escrowStatus;

    ProjectCardResponse project;
    List<DeliverableResponse> deliverables;
    InvoiceResponse invoice;
    Long conversationId;
    List<MessageResponse> messages;

    LocalDateTime createdAt;
    LocalDateTime resolvedAt;
}
