package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.ExpertApplicationRequest;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertApplicationMapper;
import com.example.AiTaster.mapper.ExpertProposalMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertApplicationRepo;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.repository.ExpertProposalRepo;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.repository.ProposalUnlockRepo;
import com.example.AiTaster.service.payment.ProposalPurchaseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertApplicationServiceNotFoundTest {

    @Mock
    private ExpertApplicationMapper expertApplicationMapper;
    @Mock
    private ExpertProposalMapper expertProposalMapper;
    @Mock
    private ExpertProposalRepo expertProposalRepo;
    @Mock
    private ExpertApplicationRepo expertApplicationRepo;
    @Mock
    private ContentManagerService contentManagerService;
    @Mock
    private ExpertProfileRepo expertProfileRepo;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ClientProfileRepo clientProfileRepo;
    @Mock
    private ProposalUnlockRepo proposalUnlockRepo;
    @Mock
    private JobPostRepo jobPostRepo;
    @Mock
    private ProposalPurchaseService proposalPurchaseService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ExpertVerificationGuardService expertVerificationGuardService;

    @InjectMocks
    private ExpertApplicationService expertApplicationService;

    @Test
    void applyJobPost_uses404WhenJobPostDoesNotExist() {
        User user = User.builder().userId(1L).build();
        ExpertProfile expertProfile = ExpertProfile.builder().expertProfileId(2L).user(user).build();
        ExpertApplicationRequest request = new ExpertApplicationRequest();
        request.setExpectedPrice(BigDecimal.ONE);
        request.setEstimatedTimeline("One day");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(expertProfileRepo.findByUser(user)).thenReturn(Optional.of(expertProfile));
        when(jobPostRepo.findJobPostByjobPostId(999L)).thenReturn(Optional.empty());

        GlobalException exception = catchThrowableOfType(
                () -> expertApplicationService.applyJobPost(999L, request),
                GlobalException.class
        );

        assertThat(exception.getCode()).isEqualTo(404);
        assertThat(exception).hasMessage("Không tìm thấy bài đăng dự án");
    }

    @Test
    void getApplicationsByJobPost_uses404WhenJobPostDoesNotExist() {
        User user = User.builder().userId(1L).build();
        ClientProfile clientProfile = ClientProfile.builder().clientProfileId(3L).user(user).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(clientProfileRepo.findByUser(user)).thenReturn(Optional.of(clientProfile));
        when(jobPostRepo.findJobPostByjobPostId(999L)).thenReturn(Optional.empty());

        GlobalException exception = catchThrowableOfType(
                () -> expertApplicationService.getApplicationsByJobPost(999L),
                GlobalException.class
        );

        assertThat(exception.getCode()).isEqualTo(404);
        assertThat(exception).hasMessage("Không tìm thấy bài đăng dự án");
    }

    @Test
    void getApplicationDetail_uses404WhenApplicationDoesNotExist() {
        when(expertApplicationRepo.findByApplicationId(999L)).thenReturn(Optional.empty());

        GlobalException exception = catchThrowableOfType(
                () -> expertApplicationService.getApplicationDetail(999L),
                GlobalException.class
        );

        assertThat(exception.getCode()).isEqualTo(404);
        assertThat(exception).hasMessage("Không tìm thấy hồ sơ ứng tuyển");
    }
}
