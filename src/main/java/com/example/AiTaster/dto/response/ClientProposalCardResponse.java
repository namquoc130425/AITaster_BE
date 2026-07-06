package com.example.AiTaster.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientProposalCardResponse {

    Long proposalId;

    Long applicationId;

    Long jobPostId;

    String jobPostTitle;

    Long expertProfileId;

    Long expertUserId;

    String expertName;

    String expertAvatarUrl;

    String proposalTitle;

    String technologies;

    BigDecimal priceToUnlock;

    Boolean isUnlocked;

    LocalDateTime createAt;
}