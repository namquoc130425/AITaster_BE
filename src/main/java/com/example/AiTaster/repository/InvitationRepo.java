package com.example.AiTaster.repository;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.entity.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvitationRepo extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByInvitationId(Long invitationId);

    // Check jobpost đã có invitation ACCEPTED chưa.
    // Nếu có rồi thì không cho gửi lời mời mới nữa.
    boolean existsByExpertApplication_JobpostAndInvitationStatus(JobPost jobPost, InvitationStatus invitationStats
    );

    //kiểm tra jobpost có invitation PENDING còn hạn không
    // nếu đã mời expert thì không được gữi lời mời khác trong vòng 24 .
    boolean existsByExpertApplication_JobpostAndInvitationStatusAndExpiresAtAfter(JobPost jobPost, InvitationStatus invitationStatus, LocalDateTime expiresAt);

    // Lấy các invitation PENDING đã hết hạn để đổi sang EXPIRED theo kiểu lazy expire.
    List<Invitation> findByInvitationStatusAndExpiresAtBefore(
            InvitationStatus invitationStatus,
            LocalDateTime now
    );

    //Collection : biến chứa nhiều status
    boolean existsByExpertApplicationAndInvitationStatusIn(ExpertApplication expertApplication, Collection<InvitationStatus> invitationStatus);

    // Client xem các lời mời thuộc jobpost của mình.
    @EntityGraph(attributePaths = {
            "expertApplication",
            "expertApplication.jobpost",
            "expertApplication.jobpost.clientProfile",
            "expertApplication.jobpost.clientProfile.user",
            "expertApplication.expertProfile",
            "expertApplication.expertProfile.user"
    })
    List<Invitation> findByExpertApplication_Jobpost_ClientProfileOrderByCreateAtDesc(ClientProfile clientProfile);

    // Expert xem lời mời mình nhận được.
    @EntityGraph(attributePaths = {
            "expertApplication",
            "expertApplication.jobpost",
            "expertApplication.jobpost.clientProfile",
            "expertApplication.jobpost.clientProfile.user",
            "expertApplication.expertProfile",
            "expertApplication.expertProfile.user"
    })
    List<Invitation> findByExpertApplication_ExpertProfileOrderByCreateAtDesc(ExpertProfile expertProfile);

    // Detail có load sẵn các quan hệ cần map response.
    // khi lấy Invitation , hãy load sẵn luôn expertAppplication v.v.v.v. . vì dùng Lazy nên khi nào gọi tới mới lấy thì anottation này giúp lấy nhanh
    @EntityGraph(attributePaths = {
            "expertApplication",
            "expertApplication.jobpost",
            "expertApplication.jobpost.clientProfile",
            "expertApplication.jobpost.clientProfile.user",
            "expertApplication.expertProfile",
            "expertApplication.expertProfile.user"
    })
    Optional<Invitation> findWithDetailByInvitationId(Long invitationId);

}
