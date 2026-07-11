package com.example.AiTaster.service;

import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.example.AiTaster.dto.response.ExpertVerificationResponse;
import com.example.AiTaster.entity.ExpertVerification;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertVerificationMapper;
import com.example.AiTaster.repository.ExpertVerificationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminExpertVerificationServiceTest {

    @Mock
    private ExpertVerificationRepo expertVerificationRepo;

    @Mock
    private ExpertVerificationMapper expertVerificationMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AdminExpertVerificationService service;

    @Test
    void approve_marksSubmittedVerificationAsVerifiedAndClearsRejectReason() {
        User admin = User.builder()
                .userId(99L)
                .build();
        ExpertVerification verification = ExpertVerification.builder()
                .verificationId(1L)
                .verificationStatus(ExpertVerificationStatus.SUBMITTED)
                .rejectReason("old reason")
                .build();
        ExpertVerificationResponse response = ExpertVerificationResponse.builder()
                .verificationId(1L)
                .verificationStatus(ExpertVerificationStatus.VERIFIED)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(admin);
        when(expertVerificationRepo.findById(1L)).thenReturn(Optional.of(verification));
        when(expertVerificationRepo.save(verification)).thenReturn(verification);
        when(expertVerificationMapper.toResponse(verification)).thenReturn(response);

        ExpertVerificationResponse result = service.approve(1L);

        assertThat(result).isSameAs(response);
        assertThat(verification.getVerificationStatus()).isEqualTo(ExpertVerificationStatus.VERIFIED);
        assertThat(verification.getRejectReason()).isNull();
        assertThat(verification.getVerifiedAt()).isNotNull();
        assertThat(verification.getVerifiedByAdminId()).isEqualTo(99L);
        verify(expertVerificationRepo).save(verification);
    }

    @Test
    void reject_requiresReason() {
        assertThatThrownBy(() -> service.reject(1L, " "))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Lý do từ chối là bắt buộc");
    }

    @Test
    void reject_marksSubmittedVerificationAsRejectedWithReason() {
        ExpertVerification verification = ExpertVerification.builder()
                .verificationId(1L)
                .verificationStatus(ExpertVerificationStatus.SUBMITTED)
                .build();
        ExpertVerificationResponse response = ExpertVerificationResponse.builder()
                .verificationId(1L)
                .verificationStatus(ExpertVerificationStatus.REJECTED)
                .rejectReason("Certificate is unclear")
                .build();

        when(expertVerificationRepo.findById(1L)).thenReturn(Optional.of(verification));
        when(expertVerificationRepo.save(verification)).thenReturn(verification);
        when(expertVerificationMapper.toResponse(verification)).thenReturn(response);

        ExpertVerificationResponse result = service.reject(1L, "Certificate is unclear");

        assertThat(result).isSameAs(response);
        assertThat(verification.getVerificationStatus()).isEqualTo(ExpertVerificationStatus.REJECTED);
        assertThat(verification.getRejectReason()).isEqualTo("Certificate is unclear");
        assertThat(verification.getVerifiedAt()).isNull();
        assertThat(verification.getVerifiedByAdminId()).isNull();
        verify(expertVerificationRepo).save(verification);
    }
}
