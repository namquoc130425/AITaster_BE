package com.example.AiTaster.entity;

import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.constant.TimelineUnit;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Project {
    @Column(nullable = false)
    String title;

    // Snapshot yêu cầu chốt tại thời điểm accept, để sau này client/expert sửa invitation cũng không đổi hợp đồng.
    @Column(columnDefinition = "TEXT")
    String requirementSnapshot;

    // Giá đã chốt cho cả project (lấy từ invitation.finalOfferedPrice). Đây mới là số tiền dùng cho escrow.
    @Column(precision = 12, scale = 2)
    BigDecimal agreedPrice;

    // Timeline tổng dạng hiển thị, copy từ invitation.finalTimeline.
    String timeline;

    // Snapshot timeline dạng số + đơn vị (copy từ invitation lúc accept). Dùng để tính deadlineAt của cả project.
    Integer timelineValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "timeline_unit", length = 10)
    TimelineUnit timelineUnit;

    // Hạn chót của CẢ PROJECT, set khi client thanh toán xong = startAt + timelineValue/timelineUnit (theo lịch).
    LocalDateTime deadlineAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false, length = 30)
    ProjectStatus projectStatus;

    // isActive=false khi chờ escrow; chỉ bật true khi escrow đã giữ tiền (webhook payment success).
    Boolean isActive;

    // startAt: thời điểm project bắt đầu chạy (khi escrow HELD). completedAt: khi client approve final deliverable.
    LocalDateTime startAt;

    LocalDateTime completedAt;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id")
    Invitation invitation;

    @PrePersist
    public void prePersist() {
        if (projectStatus == null) projectStatus = ProjectStatus.WAITING_ESCROW;
        if (isActive == null) isActive = false;
    }
}
