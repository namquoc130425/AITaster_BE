package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.entity.Category;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.Skill;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertServiceMapper;
import com.example.AiTaster.repository.CategoryRepo;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.SkillRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertProductServiceSubmissionTest {

    @Mock
    private ContentManagerService contentManagerService;
    @Mock
    private ExpertServiceMapper expertServiceMapper;
    @Mock
    private ExpertServiceRepo expertServiceRepo;
    @Mock
    private SkillRepo skillRepo;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ExpertProfileRepo expertProfileRepo;
    @Mock
    private CategoryRepo categoryRepo;
    @Mock
    private LocalFileStorageService localFileStorageService;
    @Mock
    private ClientProfileRepo clientProfileRepo;
    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ExpertVerificationGuardService expertVerificationGuardService;

    @InjectMocks
    private ExpertProductService expertProductService;

    @Test
    void createService_rejectsFeeBelowMinimum() {
        User user = User.builder().userId(1L).build();
        ExpertProfile expertProfile = ExpertProfile.builder().expertProfileId(2L).user(user).build();
        ExpertServiceRequest request = new ExpertServiceRequest();
        request.setServiceName("AI chatbot");
        request.setServiceDescription("Build a support chatbot");
        request.setServiceFee(BigDecimal.valueOf(9_999));

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(expertProfileRepo.findByUser(user)).thenReturn(Optional.of(expertProfile));

        assertThatThrownBy(() -> expertProductService.CreatService(request))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Phí dịch vụ AI phải từ 10.000 VND trở lên");

        verify(expertServiceRepo, never()).save(any());
    }

    @Test
    void createService_rejectsSubmissionWhenRequiredFilesAreMissing() {
        User user = User.builder().userId(1L).build();
        ExpertProfile expertProfile = ExpertProfile.builder().expertProfileId(2L).user(user).build();
        ExpertService expertService = ExpertService.builder().expertProfile(expertProfile).build();
        ExpertServiceRequest request = new ExpertServiceRequest();
        request.setServiceName("AI chatbot");
        request.setServiceDescription("Build a support chatbot");
        request.setServiceFee(BigDecimal.valueOf(10_000));
        request.setSelectedCategoryId(3L);
        request.setSelectedSkillIds(List.of(4L));

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(expertProfileRepo.findByUser(user)).thenReturn(Optional.of(expertProfile));
        assertThatThrownBy(() -> expertProductService.CreatService(request))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Phải có tệp tài liệu và mã nguồn trước khi gửi duyệt");

        verify(expertServiceRepo, never()).save(expertService);
    }
}
