package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.JobPostAiRequest;
import com.example.AiTaster.dto.response.Ai.GeminiJobPostResponse;
import com.example.AiTaster.dto.response.Ai.VectorSkillResult;
import com.example.AiTaster.exception.GlobalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
//service này nhận dữ liệu của qDRANT Và đưa Ai xữ lý
public class GeminiClientService {
    private final ChatClient.Builder chatClientBuilder; // BUILDER CỦA SPRINGAI
    private final ObjectMapper objectMapper;  // bieens  json thanh dto



    public GeminiJobPostResponse generateJobPost(JobPostAiRequest jobPostAiRequest , List<VectorSkillResult> vectorSkillResult) {
        try {

            String prompt = buildPrompt(jobPostAiRequest, vectorSkillResult);
            String aicontext = chatClientBuilder.build().prompt().user(prompt).call().content();
            String clearJsonContext = clearJson(aicontext);
            return objectMapper.readValue(clearJsonContext, GeminiJobPostResponse.class); // chuyển json sang
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ErrorCode.CALL_AI_FAILED);
        }

    }


    private String buildPrompt(JobPostAiRequest jobPostAiRequest, List<VectorSkillResult> vectorSkillResult) {
        String vectorSkillResultText = buildVectorSkillText(vectorSkillResult);
        String prompt = """
            Bạn là trợ lý AI cho một nền tảng freelance marketplace chuyên về dịch vụ AI.

            NHIỆM VỤ CỦA BẠN:
            - Viết lại thông tin Job Post cho rõ ràng, chuyên nghiệp và dễ hiểu hơn.
            - Chuẩn hóa nội dung người dùng nhập.
            - Gợi ý danh sách kỹ năng cuối cùng phù hợp với Job Post.
            - Tất cả nội dung trả về phải viết bằng TIẾNG VIỆT.

            QUY TẮC RẤT QUAN TRỌNG:
            1. Chỉ trả về JSON hợp lệ.
            2. Không trả markdown.
            3. Không giải thích bên ngoài JSON.
            4. Không dùng tiếng Anh trong nội dung title, description, requirementDescription, businessGoal, mainFeatures, deliverables.
            5. Không được bịa kỹ năng.
            6. finalSkillIds chỉ được lấy từ danh sách Candidate skills from Qdrant.
            7. finalSkillIds tối đa 5 kỹ năng.
            8. Không trả skillId trùng nhau.
            9. Ưu tiên kỹ năng có vectorScore cao hơn nếu kỹ năng đó phù hợp với nội dung Job Post.
            10. Nếu kỹ năng không liên quan đến Job Post thì không chọn, dù vectorScore cao.
            11. Nếu thông tin người dùng nhập còn mơ hồ, hãy suy luận ở mức tổng quát dựa trên title và candidate skills.
            12. Không được bịa các yêu cầu quá cụ thể nếu người dùng chưa cung cấp.
            13. Nếu người dùng nhập các câu như "chưa biết", "không biết", "chưa rõ", "không rõ", "chưa tìm hiểu", hãy viết lại bằng tiếng Việt theo hướng an toàn, ví dụ:
                - "Khách hàng chưa cung cấp yêu cầu chi tiết, cần trao đổi thêm để làm rõ phạm vi công việc."
                - "Mục tiêu kinh doanh chưa được mô tả cụ thể, cần làm rõ thêm trong bước trao đổi."
                - "Các tính năng chính chưa được xác định rõ, có thể bắt đầu từ các chức năng chatbot cơ bản."
            14. Nếu title đủ rõ, bạn được phép tạo bản nháp hợp lý từ title.
            15. Không để field rỗng.
            16. budgets phải giữ theo số tiền người dùng nhập.
            17. timeLine phải giữ theo thời gian người dùng nhập.

            CÁCH XỬ LÝ KHI INPUT MƠ HỒ:
            - Nếu requirementDescription là "chưa biết", hãy viết yêu cầu tổng quát, không quá chi tiết.
            - Nếu businessGoal là "chưa biết", hãy viết mục tiêu chung dựa trên title.
            - Nếu mainFeatures là "chưa biết", hãy đề xuất tính năng cơ bản ở mức an toàn.
            - Không được nói chắc chắn rằng hệ thống có những tính năng phức tạp nếu người dùng chưa yêu cầu.
            - Có thể dùng các cụm như "cần trao đổi thêm", "có thể bao gồm", "dự kiến", "ở mức cơ bản".

            Candidate skills from Qdrant:
            %s

            User job post input:
            title: %s
            requirementDescription: %s
            businessGoal: %s
            mainFeatures: %s
            budgets: %s
            timeLine: %s

            Trả về đúng cấu trúc JSON sau:
            {
              "title": "string",
              "description": "string",
              "requirementDescription": "string",
              "businessGoal": "string",
              "mainFeatures": "string",
              "deliverables": "string",
              "budgets": 1000,
              "timeLine": "string",
              "finalSkillIds": [1, 2, 3]
            }
            """.formatted(
                vectorSkillResultText,
                jobPostAiRequest.getTitle(),
                jobPostAiRequest.getRequirementDescription(),
                jobPostAiRequest.getBusinessGoal(),
                jobPostAiRequest.getMainFeatures(),
                jobPostAiRequest.getBudgets(),
                jobPostAiRequest.getTimeLine()
        );

        return prompt;
    }
  // vì Qdrant trả về List nên từ List sẽ chuyển thành String để Ai đọc :)))
    private String buildVectorSkillText(List<VectorSkillResult> vectorSkillResult) {
        if (vectorSkillResult == null || vectorSkillResult.isEmpty()) {
            return "[]";
        }

        return vectorSkillResult.stream()
                .map(skill -> """
                       
                        {
                          "skillId": %d,
                          "skillName": "%s",
                          "vectorScore": %.4f,
                        }
                        
                        """.formatted(
                        skill.getSkillId(),
                        skill.getSkillName(),
                        skill.getScore()
                ))
                .collect(Collectors.joining(",\n"));
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
