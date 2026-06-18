package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.InvitationStatus;
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

    String finalTimeline;

    Boolean clientAcceptedTerms;

    Boolean expertAcceptedTerms;

    InvitationStatus invitationStatus;

    LocalDateTime expiresAt;

    LocalDateTime respondedAt;

    LocalDateTime createAt;


    LocalDateTime updateAt;
}
