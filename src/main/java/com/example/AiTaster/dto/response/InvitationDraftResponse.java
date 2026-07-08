package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.TimelineUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class InvitationDraftResponse {
    Long applicationId;

    String projectTitle;

    String finalRequirement;

    String expectedOutput;

    String acceptanceCriteria;

    BigDecimal finalOfferedPrice;

    Integer finalTimelineValue;

    TimelineUnit finalTimelineUnit;

    Boolean clientAcceptedTerms;
}
