package com.example.AiTaster.controller;

import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.service.vector.SkillVectorSyncService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/vector")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SkillVectorAdminController {
    private final SkillVectorSyncService skillVectorSyncService;
    @PostMapping("/skill/sync")
    public APIResponse<Integer> syncSkillVector() {
        int syncedCount = skillVectorSyncService.syncSkillVector();

        return  APIResponse.response(
                200,
                "Đồng bộ kỹ năng lên Qdrant thành công",
                syncedCount
        );
    }
}
