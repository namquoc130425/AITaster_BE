package com.example.AiTaster.service;

import com.example.AiTaster.constant.ExpertVerificationStatus;
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
            throw new GlobalException(400, "Only submitted verification can be approved");
        }

        verification.setVerificationStatus(ExpertVerificationStatus.VERIFIED);
        verification.setRejectReason(null);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setVerifiedByAdminId(admin.getUserId());

        return expertVerificationMapper.toResponse(expertVerificationRepo.save(verification));
    }

    // Hàm từ chối chứng chỉ Expert và lưu lý do để Expert biết cần nộp lại gì.
    @Transactional
    public ExpertVerificationResponse reject(Long verificationId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new GlobalException(400, "Reject reason is required");
        }

        ExpertVerification verification = getVerification(verificationId);

        if (verification.getVerificationStatus() != ExpertVerificationStatus.SUBMITTED) {
            throw new GlobalException(400, "Only submitted verification can be rejected");
        }

        verification.setVerificationStatus(ExpertVerificationStatus.REJECTED);
        verification.setRejectReason(reason);
        verification.setVerifiedAt(null);
        verification.setVerifiedByAdminId(null);

        return expertVerificationMapper.toResponse(expertVerificationRepo.save(verification));
    }

    private ExpertVerification getVerification(Long verificationId) {
        return expertVerificationRepo.findById(verificationId)
                .orElseThrow(() -> new GlobalException(404, "Verification not found"));
    }
}
