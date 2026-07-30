package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertApplicationRepo;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.repository.SkillRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JobPostServiceTest {

    @Mock
    private JobPostRepo jobPostRepo;

    @Mock
    private JobPostMapper jobPostMapper;

    @Mock
    private ClientProfileRepo clientProfileRepo;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ContentManagerService contentManagerService;

    @Mock
    private SkillRepo skillRepo;

    @Mock
    private ExpertApplicationRepo expertApplicationRepo;

    @InjectMocks
    private JobPostService jobPostService;

    @Test
    void createJobPost_rejectsTitleWithLessThanTenWords() {
        JobPostRequest request = new JobPostRequest();
        request.setTitle("Build AI chatbot for ecommerce");
        request.setRequirementDescription("Build a chatbot for product support and order tracking.");
        request.setBusinessGoal("Reduce support workload and improve conversion.");
        request.setMainFeatures("Chat, FAQ, product suggestions, and dashboard.");
        request.setBudgets(BigDecimal.valueOf(5_000_000));
        request.setTimeLine("4 weeks");

        assertThatThrownBy(() -> jobPostService.createJobPost(request))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("at least 10 words");

        verify(currentUserService, never()).getCurrentUser();
    }
}
