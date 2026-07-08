package com.example.AiTaster.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpertProposalResponse {

    Long proposalId;

    Long applicationId;

    String title;

    String technologies;

    String detailContent;

    BigDecimal priceToUnlock;

    Boolean isUnlocked;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}
