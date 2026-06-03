package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.dto.response.GeminiJobPostResponse;
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

    public GeminiJobPostResponse generateJobPost(JobPostRequest request , List<String> SkillNames) throws JsonProcessingException {
        try {

            String prompt = buildPrompt(request,SkillNames);
            // gữi câu hỏi và lụm câu trả lời mlem mlem
            // promt chuẩn đồ chơi để hỏi
            //user(prompt) : gữi câu hỏi với vai trò người dùng user không phai hệ thống : tk em trả lời dùm anh
            //call : gọi CHO nó tk em và bảo nó trả lời
            // content : nhận câu trả lời ( LẤY PHẦN NỌI DUNG , KHONG LẤY META LINH TINH )
            String aicontext = chatClientBuilder.build().prompt().user(prompt).call().content();
            // làm sạch câu trả lời xóa mấy cái json dư thùa '''json du thừa
            String clearJsonContext = clearJson(aicontext);
            // chuyển json sang object
            return objectMapper.readValue(clearJsonContext, GeminiJobPostResponse.class); // chuyển json sang
        }catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ErrorCode.CALL_AI_FAILED);
        }

    }



    private String buildPrompt(JobPostRequest request , List<String> SkillNames) {
        return """
                Bạn là AI Assistant trong marketplace .
                               Nhiệm vụ của bạn là kiểm tra, chuẩn hóa và viết lại JobPost cho chuyên nghiệp hơn.
               
                               DỮ LIỆU CLIENT ĐÃ NHẬP:
                               title: "%s"
                               requirementDescription: "%s"
                               businessGoal: "%s"
                               mainFeatures: "%s"
                               requiredSkills: "%s"
                               budgets: "%s"
                               timeLine: "%s"
               
                               DANH SÁCH SKILL ĐANG CÓ TRONG DATABASE:
                               %s
               
                               QUY TẮC BẮT BUỘC:
                               - Chỉ trả về JSON hợp lệ.
                               - Không markdown.
                               - Không giải thích thêm.
                               - Không trả về list/array.
                               - Không thêm field ngoài format.
                               - Không trả về jobPostId.
                               - Không trả về clientId.
                               - Không trả về jobPostStatus.
                               - Không trả về createAt.
                               - Không trả về updateAt.
                               - Giữ đúng ý định ban đầu của client.
                               - Được phép sửa lỗi chính tả, làm câu rõ ràng hơn, chuyên nghiệp hơn.
                               - Không bịa thêm tính năng quá xa yêu cầu client.
                               - Field budgets phải là số decimal, ví dụ 1500.00.
                              - Field requiredSkills là String, các skill cách nhau bằng dấu phẩy.
                               - Nếu danh sách skill database có dữ liệu, requiredSkills chỉ được chọn skill từ danh sách database.
                               - Nếu không có skill phù hợp trong database, giữ lại skill client nhập nhưng format lại cho dễ đọc.              
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
                               """.formatted(request.getTitle(),
                                             request.getBusinessGoal(),
                                             request.getMainFeatures(), // Gắn mainFeatures// Gắn targetUsers
                                             request.getRequiredSkills(), // Gắn requiredSkills client nhập
                                             request.getBudgets(), // Gắn budget
                                             request.getTimeLine(), // Gắn timeline
                                             SkillNames);// Gắn keyword vào prompt
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
