package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.RatingFilterRequest;
import com.example.AiTaster.dto.request.RatingRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.dto.response.RatingResponse;
import com.example.AiTaster.service.RatingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/expert-services/{serviceId}")
    public ResponseEntity<APIResponse<RatingResponse>> createExpertServiceRating(
            @PathVariable Long serviceId,
            @RequestBody @Valid RatingRequest request
    ) {
        RatingResponse response = ratingService.createExpertServiceRating(serviceId, request);

        return ResponseEntity.status(201).body(
                APIResponse.response(201, "Create AI service rating successfully", response)
        );
    }

    @PostMapping("/projects/{projectId}/expert")
    public ResponseEntity<APIResponse<RatingResponse>> createProjectExpertRating(
            @PathVariable Long projectId,
            @RequestBody @Valid RatingRequest request
    ) {
        RatingResponse response = ratingService.createProjectExpertRating(projectId, request);

        return ResponseEntity.status(201).body(
                APIResponse.response(201, "Create project expert rating successfully", response)
        );
    }

    @GetMapping("/{ratingId}")
    public ResponseEntity<APIResponse<RatingResponse>> getRating(
            @PathVariable Long ratingId
    ) {
        RatingResponse response = ratingService.getRating(ratingId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get rating successfully", response)
        );
    }

    @GetMapping("/expert-services/{serviceId}/my")
    public ResponseEntity<APIResponse<RatingResponse>> getMyExpertServiceRating(
            @PathVariable Long serviceId
    ) {
        RatingResponse response = ratingService.getMyExpertServiceRating(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get my AI service rating successfully", response)
        );
    }

    @GetMapping("/projects/{projectId}/my")
    public ResponseEntity<APIResponse<RatingResponse>> getMyProjectRating(
            @PathVariable Long projectId
    ) {
        RatingResponse response = ratingService.getMyProjectRating(projectId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get my project rating successfully", response)
        );
    }

    @PatchMapping("/{ratingId}")
    public ResponseEntity<APIResponse<RatingResponse>> updateRating(
            @PathVariable Long ratingId,
            @RequestBody @Valid RatingRequest request
    ) {
        RatingResponse response = ratingService.updateRating(ratingId, request);

        return ResponseEntity.ok(
                APIResponse.response(200, "Update rating successfully", response)
        );
    }

    @DeleteMapping("/{ratingId}")
    public ResponseEntity<APIResponse<Void>> deleteRating(
            @PathVariable Long ratingId
    ) {
        ratingService.deleteRating(ratingId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Delete rating successfully", null)
        );
    }

    @PostMapping("/filter")
    public ResponseEntity<APIResponse<PageResponse<RatingResponse>>> filterRatings(
            @RequestBody @Valid RatingFilterRequest request
    ) {
        PageResponse<RatingResponse> response = ratingService.filterRatings(request);

        return ResponseEntity.ok(
                APIResponse.response(200, "Filter ratings successfully", response)
        );
    }
}
