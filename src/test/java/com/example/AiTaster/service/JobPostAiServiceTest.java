package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.JobPostAiRequest;
import com.example.AiTaster.dto.response.Ai.GeminiJobPostResponse;
import com.example.AiTaster.dto.response.Ai.VectorSkillResult;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.Skill;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.SkillRepo;
import com.example.AiTaster.service.vector.SkillVectorSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPostAiServiceTest {

    @Mock
    private GeminiClientService geminiClientService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private JobPostMapper jobPostMapper;

    @Mock
    private ContentManagerService contentManagerService;

    @Mock
    private ClientProfileRepo clientProfileRepo;

    @Mock
    private SkillRepo skillRepo;

    @Mock
    private SkillVectorSearchService skillVectorSearchService;

    @InjectMocks
    private JobPostAiService jobPostAiService;

    @Test
    void creatJobPostByAi_rejectsInvalidTitleBeforeVectorSearch() {
        JobPostAiRequest request = new JobPostAiRequest();
        request.setTitle(" ");

        doThrow(new GlobalException(400, "Cannot be blank"))
                .when(contentManagerService)
                .validateKeywordInput(" ");

        assertThatThrownBy(() -> jobPostAiService.creatJobPostByAi(request))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("Cannot be blank");

        verify(skillVectorSearchService, never()).searchSkillResult(any(), any(Integer.class));
        verify(geminiClientService, never()).generateJobPost(any(), any());
    }

    @Test
    void creatJobPostByAi_rejectsThinActualInputBeforeVectorSearch() {
        User user = User.builder().userId(10L).build();
        ClientProfile clientProfile = ClientProfile.builder().clientProfileId(20L).build();
        JobPostAiRequest request = new JobPostAiRequest();
        request.setTitle("AI");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(clientProfileRepo.findByUser_UserId(10L)).thenReturn(Optional.of(clientProfile));

        assertThatThrownBy(() -> jobPostAiService.creatJobPostByAi(request))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("45");

        verify(skillVectorSearchService, never()).searchSkillResult(any(), any(Integer.class));
        verify(geminiClientService, never()).generateJobPost(any(), any());
    }

    @Test
    void creatJobPostByAi_returnsGeneratedDraftPreview() {
        User user = User.builder().userId(10L).build();
        ClientProfile clientProfile = ClientProfile.builder().clientProfileId(20L).build();
        Skill skill = Skill.builder()
                .skillId(1L)
                .skillName("Chatbot AI")
                .slug("chatbot-ai")
                .build();
        JobPostAiRequest request = new JobPostAiRequest();
        request.setTitle("Tao chatbot AI tu van khach hang cho website ban hang");
        request.setRequirementDescription("Can chatbot tra loi FAQ, tu van san pham va luu thong tin khach hang.");
        request.setBusinessGoal("Giam tai cham soc khach hang va tang ty le chuyen doi.");
        request.setMainFeatures("Chat tren website, kho FAQ, goi y san pham va dashboard theo doi.");
        request.setBudgets(BigDecimal.valueOf(12_000_000));
        request.setTimeLine("4 tuan");
        request.setSelectedSkillIds(List.of(1L));

        GeminiJobPostResponse aiResponse = new GeminiJobPostResponse(
                "Tao chatbot AI tu van khach hang",
                "Can chatbot tra loi FAQ va tu van san pham.",
                "Giam tai cham soc khach hang.",
                "Chat website, FAQ va dashboard.",
                BigDecimal.valueOf(12_000_000),
                "4 tuan",
                List.of(1L)
        );
        JobPost mappedJobPost = JobPost.builder().title(aiResponse.getTitle()).build();
        JobPostResponse expectedResponse = JobPostResponse.builder()
                .title(aiResponse.getTitle())
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(clientProfileRepo.findByUser_UserId(10L)).thenReturn(Optional.of(clientProfile));
        when(skillRepo.findAllById(List.of(1L))).thenReturn(List.of(skill));
        when(skillVectorSearchService.searchSkillResult(any(), any(Integer.class)))
                .thenReturn(List.of(VectorSkillResult.builder()
                        .skillId(1L)
                        .skillName("Chatbot AI")
                        .score(0.95)
                        .build()));
        when(geminiClientService.generateJobPost(any(), any())).thenReturn(aiResponse);
        when(jobPostMapper.toEntityJobPostDraft(aiResponse, clientProfile)).thenReturn(mappedJobPost);
        when(jobPostMapper.toResponse(mappedJobPost)).thenReturn(expectedResponse);

        JobPostResponse response = jobPostAiService.creatJobPostByAi(request);

        assertThat(response).isSameAs(expectedResponse);
        verify(jobPostMapper).toResponse(mappedJobPost);
    }
}
