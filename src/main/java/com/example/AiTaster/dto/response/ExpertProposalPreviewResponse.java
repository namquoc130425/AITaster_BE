package com.example.AiTaster.dto.response;

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
//Preview thi ko có detailContent
public class ExpertProposalPreviewResponse {

    Long proposalId;

    Long jobPostId;

    Long expertProfileId;

    String expertName;

    String title;

    String summary;

    String technologies;

    BigDecimal priceToUnlock;

    Boolean isUnlocked;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}
