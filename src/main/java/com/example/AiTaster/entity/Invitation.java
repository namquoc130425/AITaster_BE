package com.example.AiTaster.entity;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.TimelineUnit;
import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "invitation",
        indexes = {
                @Index(name = "idx_invitation_application",columnList = "application_id"),
                @Index(name = "idx_invitation_status",columnList = "invitation_status"),
                @Index(name = "idx_invitation_expires_at",columnList = "expires_at")
        }
)
// Nếu không dùng index, DB phải quét nhiều dòng để tìm dữ liệu chính xác.
// Dùng index giúp DB có mục lục để truy vấn nhanh hơn.
// Ví dụ invitation_status = PENDING nằm ở dòng 2, 5, 7, 9, 10.
// Khi truy vấn, DB nhìn vào index để đi thẳng đến các dòng liên quan.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long invitationId;

    @ManyToOne(fetch = FetchType.LAZY)  // LAZY giúp chưa lấy application ngay; khi gọi tới invitation.getApplication() thì mới query thêm.
    @JoinColumn(name = "application_Id",nullable = false)
   ExpertApplication expertApplication;

    @Column(nullable = false)
    String projectTitle;

    @Column(columnDefinition = "TEXT", nullable = false)
    String finalRequirement;

    @Column(columnDefinition = "TEXT", nullable = false)
    String expectedOutput;

    @Column(columnDefinition = "TEXT", nullable = false)
    String acceptanceCriteria;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal finalOfferedPrice;

    @Column(nullable = false)
    Integer finalTimelineValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    TimelineUnit finalTimelineUnit;

    @Column(nullable = false)
    String finalTimeline;

    @Column(nullable = false)
    Boolean clientAcceptedTerms;

    @Column(nullable = false)
    Boolean expertAcceptedTerms;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", nullable = false, length = 30)
    InvitationStatus invitationStatus;

    Boolean clientDeleted;

    Boolean expertDeleted;

    // Thời gian hết hạn.
    @Column(nullable = false)
    LocalDateTime expiresAt;

    // Thời điểm expert phản hồi.
    // Accept/reject thì set now, còn pending thì null.
    LocalDateTime respondedAt;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;



    @PrePersist
    public void prePersist() {
        if (clientAcceptedTerms == null) clientAcceptedTerms = false;
        if (expertAcceptedTerms == null) expertAcceptedTerms = false;
        if (invitationStatus == null) invitationStatus = InvitationStatus.PENDING;
        if (clientDeleted == null) clientDeleted = false;
        if (expertDeleted == null) expertDeleted = false;
        if (expiresAt == null) expiresAt = LocalDateTime.now().plusHours(24);
    }
    // Nếu expiresAt chưa có giá trị thì tự động gán hạn 24 tiếng từ thời gian hiện tại.
}
