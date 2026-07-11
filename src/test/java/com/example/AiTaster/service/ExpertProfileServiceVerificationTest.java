package com.example.AiTaster.service;

import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.example.AiTaster.dto.request.ResubmitExpertCertificateRequest;
import com.example.AiTaster.dto.response.ExpertVerificationResponse;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertVerification;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.CurrentUserResponseMapper;
import com.example.AiTaster.mapper.ExpertProfileMapper;
import com.example.AiTaster.mapper.ExpertVerificationMapper;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.repository.ExpertVerificationRepo;
import com.example.AiTaster.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertProfileServiceVerificationTest {

    @Mock
    private ExpertProfileMapper expertProfileMapper;
    @Mock
    private ExpertProfileRepo expertProfileRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private CurrentUserResponseMapper currentUserResponseMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ExpertVerificationRepo expertVerificationRepo;
    @Mock
    private ExpertVerificationMapper expertVerificationMapper;

    @InjectMocks
    private ExpertProfileService service;

    @Test
    void resubmitCertificate_updatesCertificateUrlAndReturnsRejectedVerificationToSubmitted() {
        User user = User.builder()
                .userId(10L)
                .build();
        ExpertProfile expertProfile = ExpertProfile.builder()
                .expertProfileId(20L)
                .user(user)
                .build();
        ExpertVerification verification = ExpertVerification.builder()
                .verificationId(30L)
                .expertProfile(expertProfile)
                .certificateUrl("https://old.example/cert.pdf")
                .verificationStatus(ExpertVerificationStatus.REJECTED)
                .rejectReason("Need clearer file")
                .verifiedAt(LocalDateTime.now())
                .verifiedByAdminId(99L)
                .build();
        ResubmitExpertCertificateRequest request = new ResubmitExpertCertificateRequest();
        request.setCertificateUrl("https://supabase.example/new-cert.pdf");
        ExpertVerificationResponse response = ExpertVerificationResponse.builder()
                .verificationId(30L)
                .certificateUrl("https://supabase.example/new-cert.pdf")
                .verificationStatus(ExpertVerificationStatus.SUBMITTED)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(expertProfileRepo.findByUser(user)).thenReturn(Optional.of(expertProfile));
        when(expertVerificationRepo.findByExpertProfile(expertProfile)).thenReturn(Optional.of(verification));
        when(expertVerificationRepo.save(verification)).thenReturn(verification);
        when(expertVerificationMapper.toResponse(verification)).thenReturn(response);

        ExpertVerificationResponse result = service.resubmitCertificate(request);

        assertThat(result).isSameAs(response);
        assertThat(verification.getCertificateUrl()).isEqualTo("https://supabase.example/new-cert.pdf");
        assertThat(verification.getVerificationStatus()).isEqualTo(ExpertVerificationStatus.SUBMITTED);
        assertThat(verification.getRejectReason()).isNull();
        assertThat(verification.getVerifiedAt()).isNull();
        assertThat(verification.getVerifiedByAdminId()).isNull();
        verify(expertVerificationRepo).save(verification);
    }

    @Test
    void resubmitCertificate_blocksVerifiedExpert() {
        User user = User.builder()
                .userId(10L)
                .build();
        ExpertProfile expertProfile = ExpertProfile.builder()
                .expertProfileId(20L)
                .user(user)
                .build();
        ExpertVerification verification = ExpertVerification.builder()
                .verificationStatus(ExpertVerificationStatus.VERIFIED)
                .build();
        ResubmitExpertCertificateRequest request = new ResubmitExpertCertificateRequest();
        request.setCertificateUrl("https://supabase.example/new-cert.pdf");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(expertProfileRepo.findByUser(user)).thenReturn(Optional.of(expertProfile));
        when(expertVerificationRepo.findByExpertProfile(expertProfile)).thenReturn(Optional.of(verification));

        assertThatThrownBy(() -> service.resubmitCertificate(request))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Chuyên gia đã xác minh không cần gửi lại chứng chỉ");
    }
}
