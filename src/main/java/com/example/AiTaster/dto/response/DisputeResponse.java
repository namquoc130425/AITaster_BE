package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.DisputeStatus;
import com.example.AiTaster.constant.DisputeDecision;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;


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

    Long reporterId;
    String reporterName;

    Long reportedAgainstId;
    String reportedAgainstName;

    String reason;
    String evidence;
    String response;

    DisputeStatus disputeStatus;
    DisputeDecision disputeDecision;

    BigDecimal refundAmount;
    BigDecimal releaseAmount;
    BigDecimal escrowHeldAmount;

    LocalDateTime createdAt;
    LocalDateTime resolvedAt;
}
