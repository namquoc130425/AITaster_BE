package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.MilestoneStatus;
import com.example.AiTaster.constant.MilestoneStep;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MilestoneEventResponse {
    Long projectId;
    // Loại sự kiện: SUBMITTED, APPROVED, REVISION_REQUESTED, COMPLETED
    String eventType;
    MilestoneStep currentStep;
    MilestoneStatus status;
    // Câu thông báo hiển thị cho FE (vd: "Expert đã nộp file mốc 1")
    String message;
    // userId của người NÊN nhận thông báo (client hoặc expert)
    Long targetUserId;
    LocalDateTime at;
}
