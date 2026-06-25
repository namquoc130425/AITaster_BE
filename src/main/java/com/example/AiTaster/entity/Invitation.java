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
// không dùng index thì db phải querry rất nhiều lần lặp qua từng giá trị để tìm ra giá trị chính xác nhất .20 dòng thì không sao nhưng 1000 dòng là chậm à
// dùng index qurry vẫn chạy nhưng db sẽ có mục lục để chạy nhanh hơn
// ví dụ : invitation_status : PEDING . dòng 2 , dòng 5 ,dòng 7 , dòng 9 , dòng 10
// khi querry ko cần do 1 đến 1000 nữa mà nó nhìn vào index :PENDING nằm ở các dòng này nè :2,5,7,9,10 v.v.
// -> nó nhảy thẳng vào máy dòng đó để lấy dữ liệu thay vì phải đi từ đầu 1-100 mà tìm ra các thằng lien quan `
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

    @ManyToOne(fetch = FetchType.LAZY)  // LAZY khi lấy Invitation , chưa lấy ngay application . Khi nào gọi tới invitation.getApplication() thì mới querry thêm -> LAZY LOADING
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

    @Column(nullable = false)
    LocalDateTime expiresAt;

    // Thời điểm expert phản hồi.
    // Accept/reject thì set now, còn pending thì null.,
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
        if (expiresAt == null) expiresAt = LocalDateTime.now().plusHours(24);
    }
    //nếu expiresAt chưa có giá trị thì tự động gán thời gian hết hạn là 24 tiếng tính từ thời gian hiện tại
}
