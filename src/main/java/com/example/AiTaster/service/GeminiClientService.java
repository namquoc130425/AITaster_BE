package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.response.GeminiJobPostResponse;
import com.example.AiTaster.exception.GlobalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiClientService {
    private final ChatClient.Builder chatClientBuilder; // BUILDER CỦA SPRINGAI
    private final ObjectMapper objectMapper;  // bieens  json thanh object java

    public GeminiJobPostResponse generateJobPost(String keyword) throws JsonProcessingException {
        try {
            // tạo prompt
            String prompt = buildPrompt(keyword);
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
            throw new GlobalException(ErrorCode.CALL_AI_FAIL); // Báo lỗi
        }

    }



    private String buildPrompt(String keyword) {
        return """
                Client nhập keyword dự án: "%s".

                Hãy đóng vai chuyên gia tư vấn dự án phần mềm trên nền tảng marketplace giống Upwork.
                Từ keyword trên, hãy tạo dữ liệu JobPost đầy đủ để backend lưu vào database.

                BẮT BUỘC:
                - Chỉ trả về JSON hợp lệ.
                - Không markdown.
                - Không giải thích thêm.
                - Không trả về list/array.
                - Không trả về jobPostId.
                - Không trả về clientId.
                - Không trả về jobPostStatus.
                - Không trả về createAt.
                - Không trả về updateAt.
                - Field budgets phải là số decimal, ví dụ 1500.00.
                - Field requiredSkills là String, các skill cách nhau bằng dấu phẩy.
                - Field targetUsers chỉ được chọn một trong các giá trị:
                  CUSTOMERS, BUSINESS_USERS, INTERNAL_STAFF, STUDENTS, PATIENTS, GENERAL_USERS.

                FORMAT JSON BẮT BUỘC:
                {
                  "title": "Tiêu đề job post ngắn gọn",
                  "requirementDescription": "Mô tả yêu cầu dự án trong 1-2 câu",
                  "businessGoal": "Mục tiêu kinh doanh của dự án",
                  "mainFeatures": "Các chức năng chính của hệ thống trong một đoạn văn ngắn",
                  "targetUsers": "CUSTOMERS",
                  "requiredSkills": "React, Spring Boot, E-commerce, AI Integration",
                  "budgets": 1500.00,
                  "timeLine": "4 - 6 tuần"
                }
                """.formatted(keyword); // Gắn keyword vào prompt
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
