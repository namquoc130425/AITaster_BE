package com.example.AiTaster.service;

import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ReferenceType;
import com.example.AiTaster.dto.response.ExpertVerificationResponse;
import com.example.AiTaster.entity.ExpertVerification;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertVerificationMapper;
import com.example.AiTaster.repository.ExpertVerificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminExpertVerificationService {

    private final ExpertVerificationRepo expertVerificationRepo;
    private final ExpertVerificationMapper expertVerificationMapper;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ExpertVerificationResponse> getSubmittedVerifications() {
        return expertVerificationRepo.findByVerificationStatus(ExpertVerificationStatus.SUBMITTED)
                .stream()
                .map(expertVerificationMapper::toResponse)
                .toList();
    }

    // Hàm chấp nhận chứng chỉ Expert sau khi admin kiểm tra file minh chứng.
    @Transactional
    public ExpertVerificationResponse approve(Long verificationId) {
        User admin = currentUserService.getCurrentUser();
        ExpertVerification verification = getVerification(verificationId);

        if (verification.getVerificationStatus() != ExpertVerificationStatus.SUBMITTED) {
            throw new GlobalException(400, "Chỉ yêu cầu xác minh đã gửi mới có thể được phê duyệt");
        }

        verification.setVerificationStatus(ExpertVerificationStatus.VERIFIED);
        verification.setRejectReason(null);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setVerifiedByAdminId(admin.getUserId());

        ExpertVerification saved = expertVerificationRepo.save(verification);

        notifyExpert(
                saved,
                "Chứng chỉ đã được phê duyệt",
                "Chứng chỉ chuyên gia của bạn đã được phê duyệt."
        );

        return expertVerificationMapper.toResponse(saved);
    }

    // Hàm từ chối chứng chỉ Expert và lưu lý do để Expert biết cần nộp lại gì.
    @Transactional
    public ExpertVerificationResponse reject(Long verificationId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new GlobalException(400, "Lý do từ chối không được để trống");
        }

        ExpertVerification verification = getVerification(verificationId);

        if (verification.getVerificationStatus() != ExpertVerificationStatus.SUBMITTED) {
            throw new GlobalException(400, "Chỉ yêu cầu xác minh đã gửi mới có thể bị từ chối");
        }

        verification.setVerificationStatus(ExpertVerificationStatus.REJECTED);
        verification.setRejectReason(reason);
        verification.setVerifiedAt(null);
        verification.setVerifiedByAdminId(null);

        ExpertVerification saved = expertVerificationRepo.save(verification);

        notifyExpert(
                saved,
                "Chứng chỉ đã bị từ chối",
                "Chứng chỉ chuyên gia của bạn đã bị từ chối. Lý do: " + reason
        );

        return expertVerificationMapper.toResponse(saved);
    }

    private ExpertVerification getVerification(Long verificationId) {
        return expertVerificationRepo.findById(verificationId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy yêu cầu xác minh"));
    }

    private void notifyExpert(
            ExpertVerification verification,
            String title,
            String content
    ) {
        if (verification == null
                || verification.getExpertProfile() == null
                || verification.getExpertProfile().getUser() == null) {
            return;
        }

        notificationService.notify(
                verification.getExpertProfile().getUser(),
                NotificationType.SYSTEM,
                ReferenceType.NONE,
                verification.getVerificationId(),
                title,
                content
        );
    }
}
