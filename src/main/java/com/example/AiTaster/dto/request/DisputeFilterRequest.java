package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.DisputeDecision;
import com.example.AiTaster.constant.DisputeStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeFilterRequest extends PageRequest {
    DisputeStatus disputeStatus;
    DisputeDecision disputeDecision;
    Long projectId;
    Long reporterId;
}
