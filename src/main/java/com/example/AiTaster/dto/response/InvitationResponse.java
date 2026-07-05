package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.TimelineUnit;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvitationResponse {

    Long invitationId;

    Long applicationId;

    Long jobPostId;

    String jobPostTitle;

    Long clientProfileId;

    String companyName;

    String contactName;

    Long expertProfileId;

    String expertName;

    String projectTitle;

    String finalRequirement;

    String expectedOutput;

    String acceptanceCriteria;

    BigDecimal finalOfferedPrice;

    Integer finalTimelineValue;

    TimelineUnit finalTimelineUnit;

    String finalTimeline;

    Boolean clientAcceptedTerms;

    Boolean expertAcceptedTerms;

    InvitationStatus invitationStatus;

    LocalDateTime expiresAt;

    LocalDateTime respondedAt;

    LocalDateTime paymentDeadline;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}
