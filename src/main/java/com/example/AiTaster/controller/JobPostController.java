package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.JobPostAiRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.service.JobPostAiService;
import com.example.AiTaster.service.JobPostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job-posts")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class JobPostController {
    private final JobPostAiService jobPostAiService;
    private final JobPostService jobPostService;
    @PostMapping("/ai/draft")
    public ResponseEntity<APIResponse<JobPostResponse>> createJobPostByAI(@RequestBody @Valid JobPostAiRequest jobPostAiRequest) throws JsonProcessingException {
        try {
            JobPostResponse jobPostResponse = jobPostAiService.CreatJobPostByAi(jobPostAiRequest);
            return ResponseEntity.ok(APIResponse.response(201, "Create job post successfully", jobPostResponse));
        } catch (Exception e) {
            // Trả về lỗi dạng JSON, không throw
            return ResponseEntity.badRequest().body(APIResponse.response(400, e.getMessage(), null));
        }

    }
}
