package com.example.AiTaster.controller;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.DeliverableResponse;
import com.example.AiTaster.dto.response.ProjectMilestoneResponse;
import com.example.AiTaster.service.ProjectMilestoneService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
=======
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
<<<<<<< HEAD
=======
import java.nio.charset.StandardCharsets;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
import java.util.List;
@RestController
@RequestMapping("/api/projects")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class ProjectMilestoneController {
    private final ProjectMilestoneService projectMilestoneService;

    // lấy chi tiết trạng thái
    @GetMapping("/{projectId}/milestone")
    public ResponseEntity<APIResponse<ProjectMilestoneResponse>> getMilestone(@PathVariable Long projectId) {
        return ResponseEntity.ok(APIResponse.response(200, "Get milestone successfully",
                projectMilestoneService.getMilestone(projectId)));
    }
    //nộp file
    @PostMapping(value = "/{projectId}/milestone/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<ProjectMilestoneResponse>> submit(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(APIResponse.response(200, "Submit deliverable successfully",
                projectMilestoneService.submit(projectId, file)));
    }
    // từ chối làm lại
    @PostMapping("/{projectId}/milestone/request-revision")
    public ResponseEntity<APIResponse<ProjectMilestoneResponse>> requestRevision(@PathVariable Long projectId) {
        return ResponseEntity.ok(APIResponse.response(200, "Request revision successfully",
                projectMilestoneService.requestRevision(projectId)));
    }
    //chấp nhận
    @PostMapping("/{projectId}/milestone/approve")
    public ResponseEntity<APIResponse<ProjectMilestoneResponse>> approve(@PathVariable Long projectId) {
        return ResponseEntity.ok(APIResponse.response(200, "Approve milestone successfully",
                projectMilestoneService.approve(projectId)));
    }
    @GetMapping("/{projectId}/milestone/current-deliverable")
    public ResponseEntity<APIResponse<DeliverableResponse>> getCurrentDeliverable(@PathVariable Long projectId) {
        return ResponseEntity.ok(APIResponse.response(200, "Get current deliverable successfully",
                projectMilestoneService.getDetailDeliverable(projectId)));
    }
    @GetMapping("/{projectId}/deliverables")
    public ResponseEntity<APIResponse<List<DeliverableResponse>>> getDeliverables(@PathVariable Long projectId) {
        return ResponseEntity.ok(APIResponse.response(200, "Get deliverables successfully",
                projectMilestoneService.findDeliverables(projectId)));
    }
<<<<<<< HEAD
=======

    @GetMapping("/{projectId}/deliverables/files/{serviceFileId}/download")
    public ResponseEntity<Resource> downloadDeliverableFile(
            @PathVariable Long projectId,
            @PathVariable Long serviceFileId
    ) {
        ProjectMilestoneService.DeliverableFileDownload download =
                projectMilestoneService.downloadDeliverableFile(projectId, serviceFileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.contentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.fileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(download.resource());
    }
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
}
