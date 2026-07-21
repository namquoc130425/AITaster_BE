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
public class ExpertApplicationResponse {
    Long applicationId;

    Long jobPostId;

    Long expertProfileId;

    String expertName;
<<<<<<< HEAD
=======
    String expertAvatarUrl;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

    BigDecimal expectedPrice;

    String estimatedTimeline;

    String shortMessage;

    ExpertProposalResponse proposal;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}
