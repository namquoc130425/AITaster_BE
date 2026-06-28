package com.example.AiTaster.controller;

import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ProjectCardResponse;
import com.example.AiTaster.service.ProjectService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<ProjectCardResponse>>> getMyProjects(
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get my projects successfully",
                        projectService.getMyProjects(search)
                )
        );
    }
}
