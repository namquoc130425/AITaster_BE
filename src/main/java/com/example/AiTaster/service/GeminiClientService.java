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
        16. Nếu người dùng đã nhập budgets là một số lớn hơn 0, phải giữ đúng số tiền đó.
            Nếu người dùng chưa nhập budgets hoặc budgets là "chưa xác định", phải đề xuất một ngân sách hợp lý bằng VND
            dựa trên title, phạm vi yêu cầu và candidate skills. Không trả 0, không trả null, không trả text.
            Với yêu cầu mơ hồ/nhỏ, đề xuất trong khoảng 3.000.000 đến 10.000.000 VND.
            Với yêu cầu trung bình, đề xuất trong khoảng 10.000.000 đến 30.000.000 VND.
            Với yêu cầu phức tạp, đề xuất từ 30.000.000 VND trở lên.
        17. Nếu người dùng đã nhập timeLine, phải giữ theo thời gian người dùng nhập.
            Nếu người dùng chưa nhập timeLine hoặc timeLine là "chưa biết", hãy đề xuất thời gian thực hiện hợp lý.
        18. Nếu người dùng đã cung cấp thông tin cụ thể ở field nào, PHẢI giữ đúng ý chính người dùng
            đã nêu ở field đó — chỉ chỉnh câu chữ cho rõ ràng/chuyên nghiệp hơn, KHÔNG được thay thế
            bằng nội dung chung chung tự tạo.

        CÁCH XỬ LÝ KHI INPUT MƠ HỒ (field trống hoặc ghi "chưa biết"):
        - Nếu requirementDescription là "chưa biết", hãy viết yêu cầu tổng quát, không quá chi tiết.
- Nếu businessGoal là "chưa biết", hãy viết mục tiêu chung dựa trên title.
        - Nếu mainFeatures là "chưa biết", hãy đề xuất tính năng cơ bản ở mức an toàn.
        - Không được nói chắc chắn rằng hệ thống có những tính năng phức tạp nếu người dùng chưa yêu cầu.
        - Có thể dùng các cụm như "cần trao đổi thêm", "có thể bao gồm", "dự kiến", "ở mức cơ bản".

        CÁCH XỬ LÝ KHI INPUT ĐÃ CÓ DỮ LIỆU THẬT (áp dụng rule 18):
        - Nếu người dùng đã viết cụ thể (ví dụ: "tích hợp thanh toán qua Momo", "xử lý 1000 đơn hàng/ngày"),
          PHẢI giữ nguyên các chi tiết này trong nội dung viết lại.
        - Chỉ được chuẩn hóa ngữ pháp, câu chữ, cách trình bày — KHÔNG được lược bỏ, khái quát hóa,
          hoặc thay thế chi tiết thật bằng câu chung chung.
        - Nếu một field có dữ liệu thật còn field khác lại "chưa biết", chỉ áp dụng cách xử lý mơ hồ
          cho riêng field đang thiếu, không ảnh hưởng đến field đã có dữ liệu thật.

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
          "budgets": 5000000,
          "timeLine": "string",
          "finalSkillIds": [1, 2, 3]
        }
        """.formatted(
                vectorSkillResultText,
                jobPostAiRequest.getTitle(), // title luôn có giá trị thật, không cần default
                valueOrDefault(jobPostAiRequest.getRequirementDescription()),
                valueOrDefault(jobPostAiRequest.getBusinessGoal()),
                valueOrDefault(jobPostAiRequest.getMainFeatures()),
                jobPostAiRequest.getBudgets() != null ? jobPostAiRequest.getBudgets() : "chưa xác định",
                valueOrDefault(jobPostAiRequest.getTimeLine())
        );

        return prompt;
    }

    // field trống thành "chưa biết"
// nếu người dùng ko nhập gì cả thì sẽ xét là "chưa biêt" để thêm vào prompt khi field ko có dữ liệu, tránh null
    private String valueOrDefault(String value) {
        return (value == null || value.isBlank()) ? "chưa biết" : value;
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
