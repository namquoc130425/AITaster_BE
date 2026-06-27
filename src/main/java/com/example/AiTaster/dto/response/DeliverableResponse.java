package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.MilestoneStep;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliverableResponse {
    Long deliverableId;
    Long projectId;
    Long milestoneId;
    Long expertId;
    MilestoneStep step;
    Integer version;
    LocalDateTime submittedAt;
    LocalDateTime reviewedAt;
    List<ServiceFileResponse> files;
}
