package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.JobPostAiRequest;
import com.example.AiTaster.dto.request.JobPost.JobPostFilterRequest;
import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.service.JobPostAiService;
import com.example.AiTaster.service.JobPostService;
import com.example.AiTaster.constant.JobpostStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-posts")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class JobPostController {
    private final JobPostAiService jobPostAiService;
    private final JobPostService jobPostService;
    private final JobPostRepo jobPostRepo;
    private final JobPostMapper jobPostMapper;

    @PostMapping("/public/filter")
    public ResponseEntity<APIResponse<PageResponse<JobPostResponse>>> getAllPublicJobPostsPage(
            @RequestBody @Valid JobPostFilterRequest jobPostFilterRequest
    ) {
        PageResponse<JobPostResponse> jobPostResponses = jobPostService.getAllPublicJobPostsPage(jobPostFilterRequest);
        return ResponseEntity.ok(APIResponse.response(200, "Lấy và lọc tin tuyển dụng thành công", jobPostResponses));
    }

    @PostMapping("/ai/draft")
    public ResponseEntity<APIResponse<JobPostResponse>> createJobPostByAI(@RequestBody @Valid JobPostAiRequest jobPostAiRequest) throws JsonProcessingException {
        try {
            JobPostResponse jobPostResponse = jobPostAiService.creatJobPostByAi(jobPostAiRequest);
            return ResponseEntity.ok(APIResponse.response(201, "Tạo nháp tin tuyển dụng bằng AI thành công", jobPostResponse));

        } catch (Exception e) {
            // Trả về lỗi dạng JSON, không throw
            return ResponseEntity.badRequest().body(APIResponse.response(400, e.getMessage(), null));
        }

    }

    @PostMapping("/client/draft")
    public ResponseEntity<APIResponse<JobPostResponse>> createJobPostByUser(@RequestBody @Valid JobPostRequest jobPostRequest) {
        JobPostResponse jobPostResponse = jobPostService.createJobPost(jobPostRequest);
        return ResponseEntity.ok(APIResponse.response(201, "Tạo tin tuyển dụng thành công", jobPostResponse));
    }

    @PutMapping("/updateJobPost/{id}")
    public ResponseEntity<APIResponse<JobPostResponse>> updateJobPost(@PathVariable @Valid long id, @RequestBody @Valid JobPostRequest jobPostRequest) {
        JobPostResponse jobPostResponse = jobPostService.UpdateJobPost(id, jobPostRequest);
        return ResponseEntity.ok(APIResponse.response(200, "Cập nhật tin tuyển dụng thành công", jobPostResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<JobPostResponse>> getJobPostById(@Valid @PathVariable long id) {
        JobPost jobPost = jobPostRepo.findById(id).orElseThrow(() -> new GlobalException("Không tìm thấy tin tuyển dụng"));
        JobPostResponse jobPostResponse = jobPostService.GetJobPostById(jobPost.getJobPostId());
        return ResponseEntity.ok(APIResponse.response(200, "Lấy tin tuyển dụng thành công", jobPostResponse));
    }

    @GetMapping("/lastJobpost/myJobPost")
    public ResponseEntity<APIResponse<List<JobPostResponse>>> getLastMyJobPostByClient() {
        List<JobPostResponse> jobPostResponses = jobPostService.GetMyJobPostByClient();
        return ResponseEntity.ok(APIResponse.response(200, "Lấy tin tuyển dụng gần đây của tôi thành công", jobPostResponses));
    }

    @GetMapping("/lastjobpost")
    public ResponseEntity<APIResponse<List<JobPostResponse>>> getAllJobPostPublic() {
        List<JobPostResponse> jobPostResponses = jobPostService.GetAllJobPostPublic();
        return ResponseEntity.ok(APIResponse.response(200, "Lấy tin tuyển dụng công khai thành công", jobPostResponses));
    }

    @PutMapping("/hiddenjobpost/{id}")
    public ResponseEntity<APIResponse<JobPostResponse>> hiddenJobPost(@PathVariable @Valid Long id) {
        JobPostResponse jobPostResponse = jobPostService.hideJobPost(id);
        return ResponseEntity.ok(APIResponse.response(200, "Ẩn tin tuyển dụng thành công", jobPostResponse));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<APIResponse<JobPostResponse>> changeJobPostStatus(
            @PathVariable Long id,
            @RequestParam JobpostStatus jobPostStatus
    ) {
        JobPostResponse jobPostResponse = jobPostService.changeJobPostStatus(id, jobPostStatus);
        return ResponseEntity.ok(APIResponse.response(200, "Đổi trạng thái tin tuyển dụng thành công", jobPostResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteJobPost(@PathVariable @Valid Long id) {
        jobPostService.DeleteJobPost(id);
        return ResponseEntity.ok(APIResponse.response(200, "Xóa tin tuyển dụng thành công", null));
    }

}
