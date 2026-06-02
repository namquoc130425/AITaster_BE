package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.JobPostAiRequest;
import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.service.JobPostAiService;
import com.example.AiTaster.service.JobPostService;
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

    @PostMapping("/ai/draft")
    public ResponseEntity<APIResponse<JobPostResponse>> createJobPostByAI(@RequestBody @Valid JobPostAiRequest jobPostAiRequest) throws JsonProcessingException {
        try {
            JobPostResponse jobPostResponse = jobPostAiService.CreatJobPostByAi(jobPostAiRequest);
            return ResponseEntity.ok(APIResponse.response(201, "Create job post with AI successfully", jobPostResponse));
        } catch (Exception e) {
            // Trả về lỗi dạng JSON, không throw
            return ResponseEntity.badRequest().body(APIResponse.response(400, e.getMessage(), null));
        }

    }

    @PostMapping("/client/draft")
    public ResponseEntity<APIResponse<JobPostResponse>> createJobPostByUser(@RequestBody @Valid JobPostRequest jobPostRequest) {
          JobPostResponse jobPostResponse = jobPostService.createJobPost(jobPostRequest);
          return ResponseEntity.ok(APIResponse.response(201, "Create job post successfully", jobPostResponse));
    }

   @PutMapping("/updateJobPost/{id}")
    public ResponseEntity<APIResponse <JobPostResponse>> updateJobPost(@PathVariable @Valid long id ,@RequestBody @Valid JobPostRequest jobPostRequest) {
       JobPostResponse jobPostResponse = jobPostService.UpdateJobPost(id, jobPostRequest);
        return ResponseEntity.ok(APIResponse.response(200, "Update job post successfully", jobPostResponse));
   }

   @GetMapping("/{id}")
    public ResponseEntity<APIResponse<JobPostResponse>> getJobPostById(@RequestBody @Valid @PathVariable long id) {
        JobPost jobPost = jobPostRepo.findById(id).orElseThrow(() -> new GlobalException("Job Post Not Found"));
        JobPostResponse jobPostResponse = jobPostService.GetJobPostById(jobPost.getJobPostId());
        return ResponseEntity.ok(APIResponse.response(200, "Job Post successfully", jobPostResponse));
   }
    @GetMapping("/lastJobpost/")
public ResponseEntity<APIResponse<List<JobPostResponse>>> getLastMyJobPostByClient() {
 List<JobPostResponse> jobPostResponses = jobPostService.GetMyJobPostByClient();
 return ResponseEntity.ok(APIResponse.response(200, "Get My Job Post Last By Client successfully", jobPostResponses));
}
    @GetMapping("/lastjobpost/")
public ResponseEntity<APIResponse<List<JobPostResponse>>> getAllJobPostPublic() {
        List<JobPostResponse> jobPostResponses = jobPostService.GetAllJobPostPublic();
        return ResponseEntity.ok(APIResponse.response(200, "Get All Job Post Public successfully", jobPostResponses));
}
    @PutMapping("/uploadjobpost/{id}")
public ResponseEntity<APIResponse<JobPostResponse>> UploadJobPost(@PathVariable @Valid Long id) {
        JobPostResponse jobPostResponse = jobPostService.publishJobPost(id);
        return ResponseEntity.ok(APIResponse.response(200, "Upload Job Post successfully", jobPostResponse));
}

}
