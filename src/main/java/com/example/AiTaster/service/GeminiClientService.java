package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.dto.response.Ai.AiSearchSkilResponse;
import com.example.AiTaster.dto.response.Ai.AiSkillResult;
import com.example.AiTaster.dto.response.Ai.GeminiJobPostResponse;
import com.example.AiTaster.exception.GlobalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiClientService {
    private final ChatClient.Builder chatClientBuilder; // BUILDER CỦA SPRINGAI
    private final ObjectMapper objectMapper;  // bieens  json thanh dto

//    // từ skill nguoi dùng nhập ,tạo promt gữi cho Ai .để Ai trả ra dử liệu . cầm dữ liệu đó để querry và gữi lại cho Ai
//    public AiSearchSkilResponse searchSkillFromAi(JobPostRequest request) {
//        try {
//            String prompt = buildPromptSearchSkill(request);
//            String aicontext = chatClientBuilder.build().prompt().user(prompt).call().content();
//            String clearJsonContext = clearJson(aicontext);
//            return objectMapper.readValue(clearJsonContext, AiSearchSkilResponse.class);
//        } catch (Exception e) {
//            throw new GlobalException("AI tạo keyword tìm skill thất bại: ");
//
//        }
//    }


    // format jobpost và chọn skill
    public GeminiJobPostResponse generateJobPost(JobPostRequest request, List<AiSkillResult> aiSkillResults) throws JsonProcessingException {
        try {

            String prompt = buildPrompt(request, aiSkillResults);
            String aicontext = chatClientBuilder.build().prompt().user(prompt).call().content();
            String clearJsonContext = clearJson(aicontext);
            return objectMapper.readValue(clearJsonContext, GeminiJobPostResponse.class); // chuyển json sang
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ErrorCode.CALL_AI_FAILED);
        }

    }

//    private String buildPromptSearchSkill(JobPostRequest request) {
//        return """
//                Bạn là AI phân tích JobPost để tìm skill phù hợp trong database.
//
//                                    DỮ LIỆU JOBPOST CLIENT NHẬP:
//                                    title: "%s"
//                                    requirementDescription: "%s"
//                                    businessGoal: "%s"
//                                    mainFeatures: "%s"
//                                    requiredSkills client nhập: "%s"
//                                    budgets: "%s"
//                                    timeLine: "%s"
//
//                                    NHIỆM VỤ:
//                                    - Đọc nội dung JobPost.
//                                    - Tạo keyword ngắn để backend tìm skill trong database.
//                                    - Không tạo mô tả dài.
//                                    - Không trả về skill không liên quan.
//                                    - keywork là JSON array.
//                                    - Mỗi phần tử là một keyword ngắn.
//                                    - Tối đa 5 keyword.
//                                    - Không thêm giải thích ngoài JSON.
//                                    - Nếu client nhập Springboot thì trả về Spring Boot.
//                                    - Nếu client nhập ReactJS thì trả về React.
//                                    - Nếu client nhập Nodejs thì trả về Node.js.
//
//                                    CHỈ TRẢ VỀ JSON HỢP LỆ:
//                                    {
//                                      "keywork": ["React", "Spring Boot", "E-commerce", "AI Integration"]
//                                    }
//                                    """.formatted(
//                                    request.getTitle(),
//                                    request.getRequirementDescription(),
//                                    request.getBusinessGoal(),
//                                    request.getMainFeatures(),
//                                    request.getRequiredSkills(),
//                                    request.getBudgets(),
//                                    request.getTimeLine() );
//    };

    private String buildPrompt(JobPostRequest request, List<AiSkillResult> aiSkillResults) {
        String skillContext = buildSkillResult(aiSkillResults);
        return """
                Bạn là AI Assistant trong marketplace giống Upwork.
                Nhiệm vụ của bạn là kiểm tra, chuẩn hóa và viết lại JobPost cho chuyên nghiệp hơn.
                
                DỮ LIỆU CLIENT ĐÃ NHẬP:
                title: "%s"
                requirementDescription: "%s"
                businessGoal: "%s"
                mainFeatures: "%s"
                requiredSkills client nhập: "%s"
                budgets: "%s"
                timeLine: "%s"
                
                ĐÂY LÀ DANH SÁCH SKILL RESULT ĐƯỢC BACKEND ĐÃ TÌM TỪ DATABASE:
                %s
                
                QUY TẮC BẮT BUỘC:
                - Chỉ trả về JSON hợp lệ.
                - Không markdown.
                - Không giải thích thêm.
                - Không thêm field ngoài format.
                - Không trả về jobPostId.
                - Không trả về clientId.
                - Không trả về jobPostStatus.
                - Không trả về createAt.
                - Không trả về updateAt.
                - Giữ đúng ý định ban đầu của client.
                - Được phép sửa lỗi chính tả, chỉnh câu rõ ràng hơn, chuyên nghiệp hơn.
                - Không bịa thêm tính năng quá xa yêu cầu client.
                - Field budgets phải là số decimal.
                - Field targetUsers chỉ được chọn một trong:
                  CUSTOMERS, BUSINESS_USERS, INTERNAL_STAFF, STUDENTS, PATIENTS, GENERAL_USERS.
                - Field requiredSkills là String, các skill cách nhau bằng dấu phẩy.
                - Nếu SKILL RESULT có dữ liệu, requiredSkills chỉ được chọn skill từ danh sách đó.
                - Nếu SKILL RESULT rỗng, hãy format lại requiredSkills client nhập.
                
                FORMAT JSON BẮT BUỘC:
                {
                  "title": "Tiêu đề đã được chỉnh chuyên nghiệp",
                  "requirementDescription": "Mô tả yêu cầu đã được viết lại rõ ràng",
                  "businessGoal": "Mục tiêu kinh doanh rõ ràng",
                  "mainFeatures": "Các chức năng chính được mô tả trong một đoạn văn",
                  "requiredSkills": "React, Spring Boot, AI Integration",
                  "budgets": 1500.00,
                  "timeLine": "4 - 6 tuần"
                }
                """.formatted(
                request.getTitle(),
                request.getRequirementDescription(),
                request.getBusinessGoal(),
                request.getMainFeatures(),
                request.getRequiredSkills(),
                request.getBudgets(),
                request.getTimeLine(),
                skillContext
        );
    }

    private String buildSkillResult(List<AiSkillResult> aiSkillResultsList) {
        // kiểm tra có trong database khoonh
        if (aiSkillResultsList == null || aiSkillResultsList.isEmpty()) {
            throw new GlobalException("Không tìm thấy skill nào phù hợp với yêu cầu của bạn");
        }
        // duyệt qua từng kết quả mà database trả ra -> chuyển qua string
        StringBuilder builder = new StringBuilder();
        for (AiSkillResult aiSkillResultList : aiSkillResultsList) {
            builder.append("- ID: ")
                    .append(aiSkillResultList.getSkillId())
                    .append(", Skill Name: ")
                    .append(aiSkillResultList.getSkillName())
                    .append("\n");
        }
        return builder.toString();
    }


    private String clearJson(String aicontext) {
        if (aicontext == null) {
            return "{}";
        }

        return aicontext.replace("```json", "").
                replace("```", "").
                trim();
    }
}
