package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.JobPostAiRequest;
import com.example.AiTaster.dto.response.Ai.GeminiJobPostResponse;
import com.example.AiTaster.dto.response.Ai.VectorSkillResult;
import com.example.AiTaster.exception.GlobalException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiClientServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private GeminiClientService geminiClientService;

    // Cùng cấu hình với ObjectMapper bean mà Spring Boot autoconfigure
    // (Jackson2ObjectMapperBuilder tắt sẵn FAIL_ON_UNKNOWN_PROPERTIES).
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JobPostAiRequest request;
    private List<VectorSkillResult> vectorSkillResults;

    @BeforeEach
    void setUp() {
        geminiClientService = new GeminiClientService(chatClientBuilder, objectMapper);

        request = new JobPostAiRequest();
        request.setTitle("Xay dung chatbot cham soc khach hang cho shop ban le");
        request.setRequirementDescription("chua biet");
        request.setBusinessGoal("chua biet");
        request.setMainFeatures("chua biet");
        request.setBudgets(null);
        request.setTimeLine("chua biet");

        vectorSkillResults = List.of(
                VectorSkillResult.builder().skillId(1L).skillName("Chatbot AI").score(0.92).build(),
                VectorSkillResult.builder().skillId(2L).skillName("NLP").score(0.81).build()
        );

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
    }

    @Test
    void generateJobPost_throwsInvalidJobPostInput_whenAiFlagsInputInvalid_notGenericCallAiFailed() {
        String aiJson = """
                {
                  "isValid": false,
                  "rejectionReason": "Noi dung khong lien quan den dich vu AI",
                  "title": "",
                  "description": "",
                  "requirementDescription": "",
                  "businessGoal": "",
                  "mainFeatures": "",
                  "deliverables": "",
                  "budgets": 0,
                  "timeLine": "",
                  "finalSkillIds": []
                }
                """;
        when(callResponseSpec.content()).thenReturn(aiJson);

        assertThatThrownBy(() -> geminiClientService.generateJobPost(request, vectorSkillResults))
                .isInstanceOf(GlobalException.class)
                .extracting(ex -> ((GlobalException) ex).getCode())
                .isEqualTo(400);
    }

    @Test
    void generateJobPost_returnsResponse_whenAiFlagsInputValid() {
        String aiJson = """
                {
                  "isValid": true,
                  "rejectionReason": "",
                  "title": "Xay dung chatbot cham soc khach hang cho shop ban le",
                  "description": "Mo ta ngan gon",
                  "requirementDescription": "Chatbot tra loi FAQ va tu van san pham",
                  "businessGoal": "Giam tai cham soc khach hang",
                  "mainFeatures": "Chat FAQ, goi y san pham, luu lich su hoi thoai",
                  "deliverables": "Chatbot hoan thien, tai lieu huong dan",
                  "budgets": 8000000,
                  "timeLine": "2 thang",
                  "finalSkillIds": [1, 2]
                }
                """;
        when(callResponseSpec.content()).thenReturn(aiJson);

        GeminiJobPostResponse response = geminiClientService.generateJobPost(request, vectorSkillResults);

        assertThat(response.getIsValid()).isTrue();
        assertThat(response.getBudgets()).isEqualByComparingTo(BigDecimal.valueOf(8_000_000));
        assertThat(response.getTimeLine()).isEqualTo("2 thang");
        assertThat(response.getFinalSkillIds()).containsExactly(1L, 2L);
    }

    @Test
    void generateJobPost_throwsCallAiFailed_whenAiReturnsMalformedJson() {
        when(callResponseSpec.content()).thenReturn("not a json at all");

        assertThatThrownBy(() -> geminiClientService.generateJobPost(request, vectorSkillResults))
                .isInstanceOf(GlobalException.class)
                .extracting(ex -> ((GlobalException) ex).getCode())
                .isEqualTo(500);
    }
}
