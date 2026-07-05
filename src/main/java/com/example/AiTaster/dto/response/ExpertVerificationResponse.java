package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpertVerificationResponse {
    Long verificationId;
    String certificateUrl;
    ExpertVerificationStatus verificationStatus;
    String rejectReason;
    LocalDateTime verifiedAt;
    Long verifiedByAdminId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
