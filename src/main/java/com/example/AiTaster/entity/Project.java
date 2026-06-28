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
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long projectId;

    @Column(nullable = false)
    String title;

    // Snapshot yêu cầu chốt tại thời điểm accept
    @Column(columnDefinition = "TEXT")
    String finalRequirementSnapshot; // yêu cầu cuối lấy từ invitation

    @Column(columnDefinition = "TEXT")
    String expectedOutputSnapshot;    // sản phẩm đầu ra mong muốn lấy từ invitaton
    @Column(columnDefinition = "TEXT")
    String acceptanceCriteriaSnapshot; // tiêu chí nghiệm tụ lấy từ invitation

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

    // Hạn chót của CẢ PROJECT, set khi client thanh toán xong = paidAt + timelineValue/timelineUnit (theo lịch).
    LocalDateTime deadlineAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false, length = 30)
    ProjectStatus projectStatus;

    // isActive=false khi chờ escrow; chỉ bật true khi escrow đã giữ tiền (webhook payment success). // true thì expert mới được làm việt
    Boolean isActive;

    // Project bat dau chay tu luc payment success.
     // Gia tri nay set bang paidAt cua PaymentTransaction.
    LocalDateTime startAt;
    //completedAt: khi client approve final deliverable.
    LocalDateTime completedAt;

    //hạn client phải thanh toán
    @Column(nullable = false)
    LocalDateTime paymentDeadlineAt;

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
