package com.example.AiTaster.service;

import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.ExpertVerificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpertVerificationGuardService {

    private final ExpertVerificationRepo expertVerificationRepo;

    @Transactional(readOnly = true)
    public void ensureVerified(ExpertProfile expertProfile) {
        if (expertProfile == null || expertProfile.getExpertProfileId() == null) {
            throw new GlobalException(403, "Chỉ chuyên gia mới được dùng API này");
        }

        boolean verified =
                expertVerificationRepo.existsByExpertProfile_ExpertProfileIdAndVerificationStatus(
                        expertProfile.getExpertProfileId(),
                        ExpertVerificationStatus.VERIFIED
                );

        if (!verified) {
            throw new GlobalException(403, "Chuyên gia phải được quản trị viên xác minh trước khi dùng tính năng này");
        }
    }
}
